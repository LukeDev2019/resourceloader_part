package com.detech.universalpay.resourceloader.view;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.beiing.flikerprogressbar.FlikerProgressBar;
import com.detech.universalpay.resourceloader.model.ResourceBean;
import com.detect.androidutils.custom.LogUtil;

/**
 * Created by Luke O on 2018/2/13.
 */

public class ResourceView implements IResourceView {

    private static final String TAG = "ResourceView";

    private static final byte UPDATE_PROGRESS = 100;

    private FlikerProgressBar flikerProgressBar;
    private MyHandler handler;
    private int viewId;

    private class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    float  current   =  (float)(Math.round((100 * msg.arg1/(1024*1024))*100) * 100)/1000000;
                    float total = (float)(Math.round(( 100 * msg.arg2/(1024*1024))*100) * 100)/1000000;
                    if(total <= 0) total = -1f;
                    if(current < 0) current = 0;
                    float progress = (float)(Math.round((100 * current/total)*100))/100;
                    ResourceBean bean = (ResourceBean) msg.obj;
                    if (flikerProgressBar != null) {
                        String text = "正在下载: " + bean.getResourceOriginName() + "  当前速度：" + current +"m/" + total +"m  " + progress + "%";
                        if(progress >= 100f) {
                            if(bean.getFormat().equals("zip")) {
                                text = bean.getResourceOriginName() + " 下载完成，正在解压...";
                            }else {
                                text = bean.getResourceOriginName() + " 下载完成!";
                            }
                        }
                        LogUtil.i(TAG, text + "  文件格式： " + bean.getFormat());
                        flikerProgressBar.setProgressText(text);
                        flikerProgressBar.setProgress(progress);
                    }
                    break;
            }
        }
    };

    public ResourceView(){
        viewId = View.generateViewId();
        handler = new MyHandler();
    }

    @Override
    public int getId() {
        return viewId;
    }

    @Override
    public void init(Activity activity) {
        if (activity == null) return;
        flikerProgressBar = activity.findViewById(viewId);
    }

    @Override
    public void reset() {
        // 重新加载
        if (flikerProgressBar != null) {
            flikerProgressBar.reset();
        }
    }

    @Override
    public void setProgress(ResourceBean bean, int current, int total) {
        sendMessage(UPDATE_PROGRESS, bean, current, total);
    }

    @Override
    public void dispose() {
        flikerProgressBar = null;
        handler = null;
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = obj;
        message.arg1 = arg1;
        message.arg2 = arg2;
        if (handler != null) {
            handler.sendMessage(message);
        }
    }
}
