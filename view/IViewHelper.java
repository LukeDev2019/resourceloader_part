package com.detech.universalpay.resourceloader.view;

import com.detech.universalpay.resourceloader.model.ResourceBean;

/**
 * Created by Luke O on 2018/2/13.
 *
 */

public interface IViewHelper {

    /**
     * 添加资源状态的view
     * 关联ResourceBean和ResourceView
     * @param bean
     * @param view
     */
    void addResourceView(ResourceBean bean, IResourceView view);

    /**
     * 移除资源状态的view
     * @param bean
     */
    void removeResourceView(ResourceBean bean);

    IResourceView getResourceView(ResourceBean bean);
}
