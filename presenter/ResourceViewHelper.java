package com.detech.universalpay.resourceloader.presenter;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.beiing.flikerprogressbar.FlikerProgressBar;
import com.detech.universalpay.R;
import com.detech.universalpay.resourceloader.model.ResourceBean;
import com.detech.universalpay.resourceloader.view.IResourceView;
import com.detech.universalpay.resourceloader.view.IViewHelper;
import com.detect.androidutils.custom.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Luke O on 2018/2/13.
 * 管理所有下载状态显示的view
 * 如果需要显示view， 需要配置此处的逻辑
 */

public class ResourceViewHelper implements IPresenter, IViewHelper {

    private static final String TAG = "ResourceViewHelper";

    private static ResourceViewHelper _instance;

    private Map<ResourceBean, IResourceView> resourceViewMap;
    private ViewGroup resourceViewLayout;
    private Activity activity;

    public static ResourceViewHelper getInst() {
        if (_instance == null) {
            synchronized (ResourceViewHelper.class) {
                if (_instance == null) {
                    _instance = new ResourceViewHelper();
                }
            }
        }
        return _instance;
    }

    private ResourceViewHelper(){
        resourceViewMap = new HashMap<>();
    }

    public void init(Activity activity){
        if(activity == null) return;
        this.activity = activity;
        resourceViewMap = new HashMap<>();
        resourceViewLayout = activity.findViewById(R.id.resource_view_layout);
    }

    @Override
    public void addResourceView(final ResourceBean bean, final IResourceView view){
        if(!resourceViewMap.containsKey(view.getId())){
            //创建一个新的view
            LogUtil.d(TAG, "创建一个新的显示的view： " + view.getId());
            if(activity == null){
                LogUtil.w(TAG, "NULL ACTIVITY, RETURNING");
                return;
            }
            FlikerProgressBar progressBar = new FlikerProgressBar(activity);
            progressBar.setId(view.getId());
            progressBar.setTextSize(12);
            progressBar.setLoadingColor(Color.parseColor("#40c4ff"));
            progressBar.setStopColor(Color.parseColor("#ff9800"));

            LinearLayout.LayoutParams parentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            if(resourceViewLayout != null) {
                LogUtil.w(TAG, "增加VIEW: " + bean.getResourceOriginName());
                resourceViewLayout.addView(progressBar, parentParams);
                view.init(activity);
                resourceViewMap.put(bean, view);
            }
        }
    }

    @Override
    public void removeResourceView(ResourceBean bean){
        LogUtil.w(TAG, "VIEW SIZE: " + resourceViewMap.size());
        if(resourceViewMap.containsKey(bean)){
            IResourceView view = resourceViewMap.remove(bean);
            LogUtil.i(TAG, "移除VIEW: " + view.getId());
            view.dispose();
            View v = activity.findViewById(view.getId());
            if(resourceViewLayout != null) {
                resourceViewLayout.removeView(v);
            }
        }else {
            LogUtil.e(TAG, "不存在的VIEW: " + bean.getResourceName());
        }
    }

    @Override
    public IResourceView getResourceView(ResourceBean bean) {
        if(resourceViewMap.containsKey(bean)){
            return resourceViewMap.get(bean);
        }
        return null;
    }

    @Override
    public void dispose() {
        if(resourceViewMap != null){
            resourceViewMap.clear();
            resourceViewMap = null;
        }
        _instance = null;
    }
}
