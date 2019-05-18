package com.detech.universalpay.resourceloader.presenter;

import com.detech.universalpay.resourceloader.IRequireCallback;
import com.detech.universalpay.resourceloader.model.ResourceBean;

/**
 * Created by Luke O on 2018/2/8.
 * 加载资源
 */

public interface ILoader {

    /**
     * 取得新的version.txt, 需要根据后台返回当前机器的version版本号
     * @param url
     */
    void requireNewResourceVersion(String url);

    /**
     * 外部直接获取资源
     *  int SUCCESS = 0;//成功返回才拿到资源
     *  int FAILED  = 1;
     *  int TIMEOUT = 2;
     *  int WAITING = 3;
     * @param id
     * @param callback
     */
    void getResource(String id, IRequireCallback callback);

    /**
     * 更新本地版本version.txt
     */
    void updateLocalVersion(ResourceBean bean);

}
