package com.detech.universalpay.resourceloader.utils;

import android.os.Environment;

import com.detech.universalpay.resourceloader.model.ResourceBean;
import com.detech.universalpay.utils.JsonUtils;
import com.detect.androidutils.custom.FileUtil;
import com.detect.androidutils.custom.LogUtil;
import com.detect.androidutils.custom.MyFunc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Luke O on 2018/2/9.
 * 解析version.txt的数据
 */

public class LoaderUtil {

    private static final String TAG = "LoaderUtil";

    private static File localVersionFile;

    private LoaderUtil(){}

    /**
     * 修改本地version.txt 中的某个数据
     * @param beanMap
     * @return
     */
    public static boolean modifyLocalBean(Map<String, ResourceBean> beanMap, String path){
        //查找本地有没有这个version.txt文件， 没有就创建一个
        //查找有没有这个节点，没有就创建一个
        //假如节点存在，直接更新节点
        //保存数据
        if(beanMap == null) return false;
        if(localVersionFile == null) localVersionFile = getFile(path);
        synchronized (localVersionFile) {
            if(!localVersionFile.exists()){
                boolean success = FileUtil.createFile(localVersionFile);
                if(success) LogUtil.w(TAG, "本地不存在， 创建文件： " + localVersionFile.getName() + " 成功！");
            }
            String jsonStr = parse2Json(beanMap);
            try {
                return FileUtil.writeFileContent(path, jsonStr, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * 解析version里的数据
     * @param versionRes 文件里的内容
     * @return
     */
    public static Map<String, ResourceBean> parse(String versionRes){
//        if(MyFunc.isNullOrEmpty(versionRes)) return null;
//        String value = JsonUtils.getValue(versionRes, ResourceBean.KEY_HEAD);
        if(MyFunc.isNullOrEmpty(versionRes)) return null;
        String[] values = JsonUtils.getValues(versionRes);
        if(values == null) return null;

        Map<String, ResourceBean> resourceBeanMap = new ConcurrentHashMap<>();
        for(String s : values){
            String downloadPath = JsonUtils.getValue(s, ResourceBean.KEY_DOWNLOAD_PATH);
            ResourceBean bean = new ResourceBean()
                    .format(JsonUtils.getValue(s, ResourceBean.KEY_FORMAT).toLowerCase())//转成小写
                    .type(JsonUtils.getValue(s, ResourceBean.KEY_TYPE).toLowerCase())//转成小写
                    .versionCode(JsonUtils.getValue(s, ResourceBean.KEY_VERSION_CODE))
                    .downloadPath(downloadPath)
                    .resourceName(getFileNameFromUrl(downloadPath))
                    .resourceOriginName(JsonUtils.getValue(s, ResourceBean.KEY_RESOURCE_ORIGIN_NAME))
                    .resourceId(JsonUtils.getValue(s, ResourceBean.KEY_RESOURCE_ID))
                    .state(JsonUtils.getValue(s, ResourceBean.KEY_STATE))
                    .zipped(Boolean.parseBoolean(JsonUtils.getValue(s, ResourceBean.KEY_ZIPPED)))
                    .md5(JsonUtils.getValue(s, ResourceBean.KEY_MD5).toLowerCase());//转成小写

            String localFilePath = Environment.getExternalStoragePublicDirectory(ResourceDefine.LOCAL_FOLDER) + File.separator + bean.getType() + File.separator + bean.getResourceName();
            String localFolderPath = localFilePath.substring(0, localFilePath.lastIndexOf("."));
            bean.localFilePath(localFilePath).localFolderPath(localFolderPath);
            LogUtil.w(TAG, "取得RESOURCE BEAN: " + bean.toString());
            if(!resourceBeanMap.containsKey(bean.getResourceId())){
                resourceBeanMap.put(bean.getResourceId(), bean);
            }
        }

        return resourceBeanMap;
    }

    public static File getFile(String path){
        if(MyFunc.isNullOrEmpty(path)) return null;
        return new File(path);
    }

    /**
     * 读取服务器文件
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String readServerFile(String fileName) throws IOException{
        String read;
        String readStr ="";
        try{
            URL url =new URL(fileName);
            HttpURLConnection urlCon = (HttpURLConnection)url.openConnection();
            urlCon.setConnectTimeout(5000);
            urlCon.setReadTimeout(5000);
            BufferedReader br =new BufferedReader(new InputStreamReader( urlCon.getInputStream()));
            while ((read = br.readLine()) !=null) {
                readStr = readStr + read;
            }
            br.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            readStr ="";
        }
        return readStr;
    }

    /**
     * 读取本地文件
     * @param fileName
     * @param charset
     * @return
     */
    public static String readLocalFile(String fileName, String charset) throws IOException {
        //设置默认编码
        if(charset == null){
            charset = "UTF-8";
        }
        File file = new File(fileName);
        if(file.isFile() && file.exists()){
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, charset);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer sb = new StringBuffer();
            String text;
            while((text = bufferedReader.readLine()) != null){
                sb.append(text);
            }
            return sb.toString();
        }
        return "";
    }

    private static String parse2Json(Map<String, ResourceBean> beanMap) {
        if(beanMap == null) beanMap = new ConcurrentHashMap<>();
        try {
            JSONArray beanArray = new JSONArray();
            for (Map.Entry<String, ResourceBean> map : beanMap.entrySet()) {
                JSONObject beanJson = new JSONObject();
                ResourceBean bean = map.getValue();
                beanJson.put(ResourceBean.KEY_RESOURCE_ORIGIN_NAME, bean.getResourceOriginName());
                beanJson.put(ResourceBean.KEY_RESOURCE_ID, bean.getResourceId());
                beanJson.put(ResourceBean.KEY_FORMAT, bean.getFormat());
                beanJson.put(ResourceBean.KEY_MD5, bean.getMd5());
                beanJson.put(ResourceBean.KEY_DOWNLOAD_PATH, bean.getDownloadPath());
                beanJson.put(ResourceBean.KEY_STATE, bean.getState());
                beanJson.put(ResourceBean.KEY_TYPE, bean.getType());
                beanJson.put(ResourceBean.KEY_VERSION_CODE, bean.getVersionCode());
                beanJson.put(ResourceBean.KEY_ZIPPED, bean.isZipped());

                beanArray.put(beanJson);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ResourceBean.KEY_HEAD, beanArray);

//            LogUtil.i(TAG, "JSON VALUE: " + jsonObject.toString());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getFileNameFromUrl(String url) {
        String fName = url.trim();
        String fileName = fName.substring(fName.lastIndexOf("/") + 1);
        return fileName;
    }
}
