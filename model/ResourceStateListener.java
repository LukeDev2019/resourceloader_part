package com.detech.universalpay.resourceloader.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.detech.androidutils.PackageUtils;
import com.detech.universalpay.common.AppActionManager;
import com.detech.universalpay.common.SystemManager;
import com.detech.universalpay.gamelogic.AppBusiness;
import com.detech.universalpay.resourceloader.presenter.ILoader;
import com.detech.universalpay.resourceloader.presenter.ResourceViewHelper;
import com.detech.universalpay.resourceloader.utils.ApkUtil;
import com.detech.universalpay.resourceloader.view.IResourceView;
import com.detech.universalpay.resourceloader.view.ResourceView;
import com.detech.universalpay.utils.MyToast;
import com.detech.universalpay.utils.UnZipUtil;
import com.detect.androidutils.custom.LogUtil;
import com.detect.androidutils.custom.SystemUtil;

import java.io.File;

/**
 * Created by Luke O on 2018/2/12.
 * 接收到资源的处理
 */

public class ResourceStateListener implements IResourceListener {

    private static final String TAG = "ResourceStateListener";

    private static final byte CREATE_RESOURCE_STATE_VIEW = 100;//创建显示resource下载的view
    private static final byte REMOVE_RESOURCE_STATE_VIEW = 101;//移除显示resource下载的view

    private MyHandler handler;

    public ResourceStateListener(){
        handler = new MyHandler();
    }

    @Override
    public void onDownloadCompleted(ILoader loader, ResourceBean bean) {
        LogUtil.i(TAG, "++++++++++++++加载更新数据---------------->" + bean.getResourceName() + "  TYPE: " + bean.getType());
        MyToast.show(bean.getResourceName() + " 下载成功!", Toast.LENGTH_SHORT, false);
        load(loader, bean);
        sendMessage(REMOVE_RESOURCE_STATE_VIEW, bean, -1, -1);
    }

    @Override
    public void onDownloadFailed(ResourceBean bean) {
        LogUtil.i(TAG, "下载资源失败----->" + bean.getResourceName());
//        MyToast.show(bean.getResourceName() + " 下载失败!", Toast.LENGTH_SHORT, false);
        sendMessage(REMOVE_RESOURCE_STATE_VIEW, bean,  -1, -1);
    }

    @Override
    public void onStartDownload(ResourceBean bean) {
        sendMessage(CREATE_RESOURCE_STATE_VIEW, bean,  -1, -1);
    }

    @Override
    public void onLoadLocalCompleted(ILoader loader, ResourceBean bean) {
        LogUtil.i(TAG, "++++++++++++++加载本地数据---------------->" + bean.getResourceName() + "  TYPE: " + bean.getType());
        load(loader, bean);
    }

    @Override
    public void onDownloadStatus(ResourceBean bean, int current, int total) {
        //取得这个bean的view
        IResourceView resourceView = ResourceViewHelper.getInst().getResourceView(bean);
        if(resourceView != null) {
            resourceView.setProgress(bean, current, total);
        }
    }

    private void load(final ILoader loader, final ResourceBean bean){
        switch (bean.getType()){
            case TYPE_APK:
                checkApk(bean, loader);
                break;
            case TYPE_FRAME_ANIMATION:
            case TYPE_SOUND_EFFECT_SET:
            case TYPE_H5_RESOURCE:
            case TYPE_SKIN:
                checkZipFile(bean, loader);
                break;
            case TYPE_MP3:
            case TYPE_MP4:
                checkSound(bean, loader);
                break;
        }
    }

    private void checkSound(ResourceBean bean, ILoader loader){
        if(loader != null) loader.updateLocalVersion(bean);     //checkSound
    }

    private void checkApk(ResourceBean bean, ILoader loader){
        //检查这个apk的状态
        File file = new File(bean.getLocalFilePath());
        Context context = AppBusiness.getContext();
        ApplicationInfo apkInfo = ApkUtil.getApkInfoFromPath(context, bean.getLocalFilePath());
        if(apkInfo == null) return;
        String packageName = apkInfo.packageName;
        LogUtil.w(TAG, "APK STATE: " + bean.getState());
        if(bean.getState().equals(ResourceBean.STATE_ACTIVE)) {
            //检查本地有没有这个apk
            //发现版本号不一致或者本地不存在，直接安装
            if (file.exists()) {
                if(loader != null) loader.updateLocalVersion(bean);     //checkApk
                boolean haveApp = SystemUtil.getInst().haveApp(packageName);
                LogUtil.i(TAG, "本地apk的version: " + AppActionManager.getVersion(context, packageName));
                if(haveApp && bean.getVersionCode().equals(AppActionManager.getVersion(context, packageName))){
                    LogUtil.w(TAG, "已更新apk: " + packageName + " version_code: " + bean.getVersionCode());
                }else {
                    LogUtil.w(TAG, "开始安装： " + packageName + "  version_code: " + bean.getVersionCode());
                    PackageUtils.install(context, bean.getLocalFilePath());
                }
            } else {
                LogUtil.w(TAG, "没有找到这个apk");
            }
        }else {
            //删除apk
            boolean haveApp = SystemManager.getInst().haveApp(packageName);
            if(!haveApp){
                LogUtil.i(TAG, packageName + " 已卸载！！");
                return;
            }
            LogUtil.w(TAG, "开始删除apk： " + packageName);
            PackageUtils.uninstall(context, packageName);
        }
    }

    private void checkZipFile(final ResourceBean bean, final ILoader loader){
        //检查本地是否已经解压文件
        //没有就解压文件
        File folderFile = new File(bean.getLocalFolderPath());
        if(folderFile.exists()){
            LogUtil.i(TAG, "本地已经存在对应压缩文件夹： " + bean.getLocalFolderPath() + "  文件夹长度: " + folderFile.listFiles().length + "  是否已解压： " + bean.isZipped());
            if(folderFile.listFiles().length > 0 && bean.isZipped()) {
                if (loader != null) loader.updateLocalVersion(bean);        //checkZipFile
                return;
            }
        }
        LogUtil.w(TAG, bean.getResourceOriginName() + " 没有发现对应的解压文件，尝试解压资源： " + bean.getLocalFilePath());
        File file = new File(bean.getLocalFilePath());
        final String localFolder = file.getParent();
        LogUtil.w(TAG, "本地目录： " + localFolder);

        new Thread(new Runnable() {
            @Override
            public void run() {
                //解压文件
                UnZipUtil.startUnZip2ZipFolder(localFolder, bean.getResourceName(), new UnZipUtil.IOnUnzipCallback() {
                    @Override
                    public void onBegin(String fileName) {
                        LogUtil.w(TAG, "开始解压： " + fileName);
                        bean.unZipping(true);
                    }

                    @Override
                    public void onFinish(int fileLength) {
                        LogUtil.w(TAG, bean.getResourceOriginName() + "解压完成： " + fileLength);
                        bean.unZipping(false);
                        bean.zipped(true);
                        if(loader != null) loader.updateLocalVersion(bean); //checkZipFile
                    }
                });
            }
        }).start();

    }

    private void sendMessage(int what, Object obj, int arg1, int arg2) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = obj;
        message.arg1 = arg1;
        message.arg2 = arg2;
        if (handler != null) {
            handler.sendMessage(message);
        }
    }

    private static class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            ResourceBean bean = (ResourceBean) msg.obj;
            switch (msg.what){
                case CREATE_RESOURCE_STATE_VIEW:
                    ResourceViewHelper.getInst().addResourceView(bean, new ResourceView());
                    break;
                case REMOVE_RESOURCE_STATE_VIEW:
                    ResourceViewHelper.getInst().removeResourceView(bean);
                    break;
            }
        }
    }
}
