package com.detech.universalpay.resourceloader.view;

import android.app.Activity;

import com.detech.universalpay.resourceloader.model.ResourceBean;

/**
 * Created by Luke O on 2018/2/13.
 * 资源状态的显示
 */

public interface IResourceView {

    int getId();

    void init(Activity activity);

    void reset();

    /**
     * 显示进度
     * @param bean
     * @param current
     * @param total
     */
    void setProgress(ResourceBean bean, int current, int total);

    void dispose();
}
