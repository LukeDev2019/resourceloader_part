package com.detech.universalpay.resourceloader.model;

/**
 * Created by Luke O on 2018/3/9.
 * 监听资源是否加载完成
 */

public interface IResourceOnCompletionListener {

    /**
     * 加载资源完成
     * @param bean
     */
    void finished(ResourceBean bean);
}
