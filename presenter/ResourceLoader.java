package com.detech.universalpay.resourceloader.presenter;

import android.os.Environment;

import com.detech.androidutils.network.DownloadInfo;
import com.detech.androidutils.network.DownloadUtil;
import com.detech.universalpay.common.Define;
import com.detech.universalpay.common.TimerBehavior;
import com.detech.universalpay.resourceloader.IRequireCallback;
import com.detech.universalpay.resourceloader.model.IResourceListener;
import com.detech.universalpay.resourceloader.model.IResourceOnCompletionListener;
import com.detech.universalpay.resourceloader.model.ResourceBean;
import com.detech.universalpay.resourceloader.model.ResourceStateListener;
import com.detech.universalpay.resourceloader.utils.LoaderUtil;
import com.detech.universalpay.resourceloader.utils.ResourceDefine;
import com.detech.universalpay.utils.FloderManager;
import com.detech.universalpay.utils.JsonUtils;
import com.detech.universalpay.utils.MD5Util;
import com.detect.androidutils.custom.LogUtil;
import com.detect.androidutils.custom.MyFunc;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Luke O on 2018/2/8.
 * 资源加载器
 * 资源更新主要处理类
 */

public class ResourceLoader extends TimerBehavior implements IPresenter, ILoader {

    private static final String TAG                 = "ResourceLoader";
    private static final String VERSION_FILE        = "version.txt";

    private static final int DOWNLOAD_TIMEOUT               = 10;//下载超时
    private static final int MAX_CHECK_VERSION_TIME         = 10;//最多检查新资源版本次数

    private static String LOCAL_VERSION_URL = "";
    private static ResourceLoader _instance;

    private Map<String, ResourceBean> localResMap;//记录本地version.txt的内容
    private Map<String, ResourceBean> serverResMap;//记录服务器version.txt的内容
    private Thread checkUpdateThread;
    private List<ResourceBean> needCheckUpdateRes;//需要检测更新的资源
    private List<IResourceOnCompletionListener> onCompletionListenerList;//所有监听资源的回调都注册进来
    private IResourceListener resourceListener;//资源加载完毕的监听
    private int checkUpdateResourceIndex = -1;//检查需要更新的资源的序号
    private int checkVersionTime = 0;
    private boolean localFolderCreated = false;//是否创建本地资源文件成功 ####有时候创建文件失败，这时候不下载新增的资源包

    public static ResourceLoader getInst() {
        if (_instance == null) {
            synchronized (ResourceLoader.class) {
                if (_instance == null) {
                    _instance = new ResourceLoader();
                }
            }
        }
        return _instance;
    }

    private ResourceLoader(){
        onCompletionListenerList = new CopyOnWriteArrayList<>();

    }

    public void init(){
        LOCAL_VERSION_URL = Environment.getExternalStoragePublicDirectory(Define.Floder.DETECH) + File.separator + VERSION_FILE;
        needCheckUpdateRes = new CopyOnWriteArrayList<>();
        checkUpdateThread = new checkUpdateThread();
        resourceListener = new ResourceStateListener();
        checkUpdateThread.start();

        localFolderCreated = FloderManager.create(ResourceDefine.LOCAL_FOLDER);

        checkLocalVersionRes();
    }

    public void registerOnCompletionListener(IResourceOnCompletionListener listener){
        if(!onCompletionListenerList.contains(listener)){
            onCompletionListenerList.add(listener);
        }
    }

    @Override
    public void onStart() {
        setFrequency(1000);
    }

    @Override
    public void onUpdate() {
        for (ResourceBean bean : needCheckUpdateRes) {
            if (bean.isDownloading()) {
                int checkTime = bean.getCheckDownloadTimeoutIndex();
                bean.checkDownloadTimeoutIndex(++checkTime);
                LogUtil.w(TAG, bean.getResourceOriginName() + "下载标记序号为------->" + bean.getCheckDownloadTimeoutIndex());
                if (bean.getCheckDownloadTimeoutIndex() > DOWNLOAD_TIMEOUT) {
                    LogUtil.i(TAG, bean.getResourceName() + " 下载超时: RESOURCE_ID" + bean.getResourceId()+ "   DOWNLOAD_ID: " + bean.getDownloadId());
                    DownloadUtil.getInst().removeDownloadInfo(bean.getDownloadId());
                    boolean removed = needCheckUpdateRes.remove(bean);
                    if (removed) LogUtil.i(TAG, "移除" + bean.getResourceName() + " 成功!");
                    if (resourceListener != null) resourceListener.onDownloadFailed(bean);
                }
            }
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void dispose() {
        if(localResMap != null){
            localResMap.clear();
            localResMap = null;
        }
        if(serverResMap != null){
            serverResMap.clear();
            serverResMap = null;
        }
        if(onCompletionListenerList != null){
            onCompletionListenerList.clear();
            onCompletionListenerList = null;
        }
        if(needCheckUpdateRes != null){
            needCheckUpdateRes.clear();
            needCheckUpdateRes = null;
        }
        if(checkUpdateThread != null){
            checkUpdateThread.interrupt();
            checkUpdateThread = null;
        }
        _instance = null;
    }

    @Override
    public void requireNewResourceVersion(String url) {
        LogUtil.i(TAG, "请求资源最新更新表version.txt: " + url + "  当前请求次数： " + checkVersionTime + "  超过" + MAX_CHECK_VERSION_TIME + " 次不重复请求");
        if(checkVersionTime > MAX_CHECK_VERSION_TIME) return;
        ++checkVersionTime;
        loadServerVersion(url, new ILoadCallback() {
            @Override
            public void onSuccess(String serverVersionRes) {
                LogUtil.w(TAG, "获取到服务的version.txt: " + serverVersionRes);
                checkServerResMap(serverVersionRes);
            }

            @Override
            public void onFailed(String reason) {
                LogUtil.w(TAG, "获取服务器最新version.txt 失败------>" + reason);
                checkLocalVersionRes();
            }
        });
    }

    @Override
    public void getResource(String id, IRequireCallback callback) {
        LogUtil.w(TAG, "开始加载资源： " + id);
        if(localResMap != null && localResMap.containsKey(id)) {
            ResourceBean bean = localResMap.get(id);
            if (!bean.getState().equals(ResourceBean.STATE_ACTIVE)) {
                if(callback != null) callback.onState(IRequireCallback.FAILED, bean);
                return;
            }
            if(bean.isDownloading() || bean.isUnZipping()){
                if(callback != null) callback.onState(IRequireCallback.WAITING, bean);
                return;
            }
            if(callback != null) callback.onState(IRequireCallback.SUCCESS, bean);
        }else {
            if(callback != null) callback.onState(IRequireCallback.FAILED, new ResourceBean());
        }
    }

    @Override
    public void updateLocalVersion(ResourceBean bean) {
        //更新一下本地的resource map
        if (localResMap == null) localResMap = new ConcurrentHashMap<>();
        localResMap.put(bean.getResourceId(), bean);
        ResourceBean localBean = localResMap.get(bean.getResourceId());
        if(localBean != null && !localBean.isLoaded()){
            localBean.loaded(true);
            for (IResourceOnCompletionListener listener : onCompletionListenerList){
                LogUtil.i(TAG, "资源： " + localBean.getResourceOriginName() + "加载成功");
                listener.finished(localBean);
            }
        }
        if(bean.getFrom() == ResourceBean.FROM_LOCAL) return;

        boolean success = LoaderUtil.modifyLocalBean(localResMap, LOCAL_VERSION_URL);
        if (success) {
            LogUtil.w(TAG, "更新本地version.txt成功----->" + bean.toString());
        }
    }

    /**
     * 检查本地资源信息
     */
    public void checkLocalVersionRes(){
        checkLocalResMap(LOCAL_VERSION_URL);
    }

    private void downloadRes() {
        //子线程处理
        synchronized (needCheckUpdateRes){
            if(needCheckUpdateRes.size() == 0 || !localFolderCreated) return;
            checkUpdateResourceIndex++;
            if(checkUpdateResourceIndex >= needCheckUpdateRes.size() || checkUpdateResourceIndex <= 0) checkUpdateResourceIndex = 0;
            ResourceBean bean = needCheckUpdateRes.get(checkUpdateResourceIndex);//取得需要对比的资源
            if(bean.isDownloading()) return;
            if(bean.getDownloadTime() > 3){
                LogUtil.w(TAG, bean.getResourceName() + "这个资源下载次数超过3次");
                return;
            }
            String localFilePath = bean.getLocalFilePath();
            LogUtil.i(TAG, "资源本地路径： " + localFilePath);
            File beanFile = LoaderUtil.getFile(localFilePath);
            if(beanFile == null || !beanFile.exists()){
                //获取不到文件或者本地文件没有
                //开始下载
                LogUtil.w(TAG, "获取不到文件或者本地文件没有: " + localFilePath);
                startDownload(bean);
            }else {
                //文件存在, 检测文件md5
                String md5 = MD5Util.calcMd5(beanFile);
                LogUtil.i(TAG, "本地文件的MD5： " + md5);
                LogUtil.w(TAG, "服务器文件MD5： " + bean.getMd5());
                if(!md5.equals(bean.getMd5())){
                    //md5验证不通过，需要重新下载
                    //开始下载
                    LogUtil.i(TAG, "文件存在，但MD5验证不通过， 重新下载： " + bean.getDownloadPath());
                    bean.md5(md5);
                    startDownload(bean);
                }else {
                    //本地已经有该文件
                    LogUtil.i(TAG, bean.getResourceId() + " 本地已经存在文件并校验正确， 无需更新： " + bean.getResourceName() + "   " + bean.getState());
                    if(needCheckUpdateRes.contains(bean)){
                        if(needCheckUpdateRes.remove(bean)){
                            LogUtil.i(TAG, "移除当前BEAN成功------>" + bean.getResourceId());
                        }
                    }
                    if(resourceListener != null) resourceListener.onLoadLocalCompleted(this,bean);
                }
            }
        }
    }

    /**
     * 开始下载
     * @param bean
     */
    private void startDownload(final ResourceBean bean) {
        if (bean == null) return;
        if(bean.isDownloading()){
            LogUtil.w(TAG, bean.getResourceOriginName() + "正在下载队列");
            return;
        }
        int time = bean.getDownloadTime();
        bean.downloadTime(++time);
        LogUtil.i(TAG, "需要更新的资源------>" + bean.getResourceName() + "  地址： " + bean.getDownloadPath() + " 下载次数： " + bean.getDownloadTime());
        if(resourceListener != null) resourceListener.onStartDownload(bean);
        DownloadUtil.getInst().setFloder(ResourceDefine.LOCAL_FOLDER + bean.getType(), false);//根据返回的type类型存放资源
        bean.downloading(true);
        DownloadUtil.getInst().start(bean.getDownloadPath(), new DownloadUtil.IDownloadStatusCallback() {
            @Override
            public void onStatus(DownloadInfo downloadInfo, String title, int currentBytes, int totalBytes) {
                LogUtil.d(TAG, bean.getResourceOriginName() + "正在下载： " + title + "   速度：" + currentBytes + "kb/" + totalBytes + "kb   上次速度的大小： " + bean.getLastCurrentByte());

                bean.downloadId(downloadInfo.getId());
                if(bean.getLastCurrentByte() != currentBytes){
                    bean.checkDownloadTimeoutIndex(0);
                    bean.lastCurrentByte(currentBytes);
                }
                if(resourceListener != null) resourceListener.onDownloadStatus(bean, currentBytes, totalBytes);
            }

            @Override
            public void onSuccess(DownloadInfo downloadInfo) {
                LogUtil.i(TAG, bean.getResourceOriginName() + "下载成功");
                bean.downloading(false);
                bean.checkDownloadTimeoutIndex(0);
                if(needCheckUpdateRes.contains(bean)){
                    if(needCheckUpdateRes.remove(bean)){
                        LogUtil.i(TAG, "下载成功， 移除当前BEAN------>" + bean.getResourceId() + "   RESOURCE NAME: " + bean.getResourceOriginName());
                        if(resourceListener != null) resourceListener.onDownloadCompleted(_instance, bean);
                    }
                }
                for (ResourceBean needCheckBean : needCheckUpdateRes){
                    LogUtil.i(TAG, "有待检测或正在检测是否需要下载的资源： " + needCheckBean.getResourceOriginName());
                }
            }

            @Override
            public void onFail(int i) {
                LogUtil.e(TAG, bean.getResourceOriginName() + "下载失败: " + i);
                bean.downloading(false);
                if(resourceListener != null) resourceListener.onDownloadFailed(bean);
                if(needCheckUpdateRes.contains(bean)){
                    if(needCheckUpdateRes.remove(bean)){
                        LogUtil.i(TAG, "下载失败， 移除当前BEAN------>" + bean.getResourceId());
                    }
                }
            }
        });
    }

    /**
     * 加载服务器的version.txt
     * @param url
     * @param callback
     */
    private void loadServerVersion(String url, final ILoadCallback callback){
        //从服务器取得当前机器最新的version.txt
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(callback != null) callback.onFailed("获取最新version.txt失败");
            }
            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                if(response.isSuccessful()){//回调的方法执行在子线程。
                    String responeMes = response.body().string();
                    String resultCode = JsonUtils.getValue(responeMes, Define.Network.HTTP_RESULT_CODE);
                    if(!MyFunc.isNullOrEmpty(resultCode) && resultCode.equals(Define.Network.KEY_SUCCESS)){
                        String resourceVersion = JsonUtils.getValue(responeMes, Define.Network.KEY_VERSION);
                        String resourceInfo = JsonUtils.getValue(responeMes, ResourceBean.KEY_HEAD);
                        LogUtil.i(TAG, "请求资源成功， 版本号： " + resourceVersion + "  RESOURCE INFO: " + resourceInfo);
                        if(MyFunc.isNullOrEmpty(resourceInfo)){
                            if(callback != null) callback.onFailed("NULL RESOURCE INFO");
                        }else {
                            if(callback != null) callback.onSuccess(resourceInfo);
                        }
                    }else {
                        if(callback != null) callback.onFailed("RESOURCE CODE IS NULL OR EMPTY, RESULT CODE:    " + resultCode);
                    }

                }else {
                    if(callback != null) callback.onFailed("REQUIRE FAILED");
                }
            }
        });
    }

    /**
     * 加载本地的version.txt
     * @param path
     * @return
     */
    private String loadLocalVersion(String path){
        //加载本地的version.txt文件
        try {
            return LoaderUtil.readLocalFile(path, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 检查服务器返回的资源数据
     * @param serverVersionRes
     */
    private void checkServerResMap(String serverVersionRes){

        serverResMap = LoaderUtil.parse(serverVersionRes);
        if(serverResMap == null || serverResMap.size() == 0) return;//获取不到服务的最新version列表

        removeUnusedBeanFromLocal();

        for (Map.Entry<String, ResourceBean> map : serverResMap.entrySet()){
            ResourceBean serverBean = map.getValue();
            serverBean.from(ResourceBean.FROM_SERVER);
            add2NeedCheckUpdateRes(serverBean);
        }
    }

    private void removeUnusedBeanFromLocal(){
        if(localResMap == null) return;
        synchronized (localResMap) {
            for (Map.Entry<String, ResourceBean> map : localResMap.entrySet()){
                if(!serverResMap.containsKey(map.getKey())){
                    ResourceBean unusedBean = map.getValue();
                    localResMap.remove(map.getKey());
                    LogUtil.w(TAG, "移除不需要检查的RESOURCE NAME: " + unusedBean.getResourceOriginName() + "  ID: " + unusedBean.getResourceId());
                }else {
                    if(map.getValue().isZipped()){
                        serverResMap.get(map.getKey()).zipped(true);
                    }
                    String state = serverResMap.get(map.getKey()).getState();
                    map.getValue().state(state);
                }
            }
            boolean success = LoaderUtil.modifyLocalBean(localResMap, LOCAL_VERSION_URL);
            if(success) LogUtil.i(TAG, "更新本地数据成功");
        }
    }

    private void checkLocalResMap(String localPath){
        String localVersionMes = loadLocalVersion(localPath);
        String localVersionRes = JsonUtils.getValue(localVersionMes, ResourceBean.KEY_HEAD);
        LogUtil.i(TAG, "LOCAL VERSION: " + localVersionRes);
        localResMap = LoaderUtil.parse(localVersionRes);
        if(localResMap == null) {
            LogUtil.e(TAG, "本地资源获取失败： " + localPath + "  LOCAL VERSION: " + localVersionRes);
            return;
        }
        for (Map.Entry<String, ResourceBean> map : localResMap.entrySet()) {
            ResourceBean localBean = map.getValue();
            localBean.from(ResourceBean.FROM_LOCAL);
            add2NeedCheckUpdateRes(localBean);
        }
    }

    /**
     * 加入到需要检测更新的列表
     * @param bean
     */
    private void add2NeedCheckUpdateRes(ResourceBean bean){
        //检测已经加入的队列有没有重复的
        if(needCheckUpdateRes == null) return;
        for (ResourceBean b : needCheckUpdateRes){
            if(b.equals(bean)){
                LogUtil.d(TAG, "存在重复更新资源： " + bean.getResourceId() + "   name: " + bean.getResourceOriginName() + ", 不往下执行");
                return;
            }
        }

        if(!needCheckUpdateRes.contains(bean)){
            LogUtil.i(TAG, "加入到需要检测更新资源的ID： " + bean.getResourceId() + "  本地目录" + ResourceDefine.LOCAL_FOLDER + "是否创建成功： " + localFolderCreated);
            needCheckUpdateRes.add(bean);
        }
    }

    private class checkUpdateThread extends Thread {
        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    Thread.sleep(1000);
                    downloadRes();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
