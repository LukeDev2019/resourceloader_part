package com.detech.universalpay.resourceloader.presenter;

/**
 * Created by Luke O on 2018/2/12.
 */

public interface ILoadCallback {

    void onSuccess(String content);

    void onFailed(String reason);
}
