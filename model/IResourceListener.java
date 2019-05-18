package com.detech.universalpay.resourceloader.model;

import com.detech.universalpay.resourceloader.presenter.ILoader;

/**
 * Created by Luke O on 2018/2/12.
 * 监听资源的一些状态
 */

public interface IResourceListener {

    String TYPE_APK                     = "app";
    String TYPE_FRAME_ANIMATION         = "frame_animation";
    String TYPE_MP3                     = "mp3";
    String TYPE_SKIN                    = "skin";//皮肤
    String TYPE_SOUND_EFFECT_SET        = "sound_effect_set";//音效集
    String TYPE_H5_RESOURCE             = "h5_resource";//h5资源
    String TYPE_MP4                     = "video";//mp4

    /**
     * 加载到新的资源
     * @param loader
     * @param bean
     */
    void onDownloadCompleted(ILoader loader, ResourceBean bean);

    /**
     * 加载资源失败，移除资源
     * @param bean
     */
    void onDownloadFailed(ResourceBean bean);

    /**
     * 开始下载
     * @param bean
     */
    void onStartDownload(ResourceBean bean);

    /**
     * 加载本地资源成功
     * @param loader
     * @param bean
     */
    void onLoadLocalCompleted(ILoader loader, ResourceBean bean);

    void onDownloadStatus(ResourceBean bean, int current, int total);
}
