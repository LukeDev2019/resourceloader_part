package com.detech.universalpay.resourceloader;

import com.detech.universalpay.resourceloader.model.ResourceBean;

/**
 * Created by Luke O on 2018/2/8.
 * 获取资源的回调
 */

public interface IRequireCallback {

    int SUCCESS = 0;
    int FAILED  = 1;
    int TIMEOUT = 2;
    int WAITING = 3;

    void onState(int state, ResourceBean bean);
}
