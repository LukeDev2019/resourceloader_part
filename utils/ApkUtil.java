package com.detech.universalpay.resourceloader.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.detect.androidutils.custom.LogUtil;
import com.detect.androidutils.custom.MyFunc;


/**
 * Created by Luke O on 2018/2/25.
 * 解析apk工具
 */

public class ApkUtil {

    private static final String TAG = "ApkUtil";

    private ApkUtil(){}

    /**
     * 根据路径获取apk包信息
     * @param context
     * @param path
     * @return
     */
    public static ApplicationInfo getApkInfoFromPath(Context context, String path) {
        if(context == null || MyFunc.isNullOrEmpty(path)) return null;
        String format = path.substring(path.lastIndexOf(".") + 1, path.length());
        if(!format.toLowerCase().equals("apk")){
            LogUtil.w(TAG, "不是apk文件： " + path + " 当前格式： " + format);
            return null;
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            return appInfo;
        }
        return null;
    }
}
