package com.edusoho.kuozhi.v3.view.video;


import com.edusoho.kuozhi.v3.listener.NormalCallback;

/**
 * Created by howzhi on 14-7-11.
 */
public interface VideoPlayerCallback {
    void clear(NormalCallback normalCallback);

    boolean isFullScreen();

    void exitFullScreen();
}
