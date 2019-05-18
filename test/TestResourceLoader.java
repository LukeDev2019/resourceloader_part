package com.detech.universalpay.resourceloader.test;

import android.app.Activity;

import com.detech.universalpay.common.Define;
import com.detech.universalpay.resourceloader.IRequireCallback;
import com.detech.universalpay.resourceloader.model.ResourceBean;
import com.detech.universalpay.resourceloader.presenter.ILoader;
import com.detech.universalpay.resourceloader.presenter.ResourceLoader;
import com.detech.universalpay.resourceloader.presenter.ResourceViewHelper;
import com.detect.androidutils.custom.LogUtil;

/**
 * Created by Luke O on 2018/2/8.
 */

public class TestResourceLoader {

    private static final String TAG = TestResourceLoader.class.getSimpleName();

    private boolean requireRes = false;

    public TestResourceLoader(Activity a){
        ResourceViewHelper.getInst().init(a);

    }

    public void load(){
        ILoader loader = ResourceLoader.getInst();
        if(loader != null){
            if(!requireRes) {
                loader.requireNewResourceVersion(Define.Url.CHECK_NEW_VERSION + "7035");
                requireRes = true;
            }else {
                loader.getResource("8438", new IRequireCallback() {
                    @Override
                    public void onState(int state, ResourceBean bean) {
                        switch (state) {
                            case IRequireCallback.FAILED:
                                LogUtil.w(TAG, "获取资源失败");
                                break;
                            case IRequireCallback.SUCCESS:
                                LogUtil.i(TAG, "获取资源成功： " + bean.getLocalFilePath());
//                                UnZipUtil.upZipFile(new File(bean.getLocalFilePath()));
                                break;
                            case IRequireCallback.TIMEOUT:
                                LogUtil.w(TAG, "获取资源超时");
                                break;
                            case IRequireCallback.WAITING:
                                LogUtil.w(TAG, "等待资源中。。。");
                                break;
                        }
                    }
                });
            }
        }
    }
}
