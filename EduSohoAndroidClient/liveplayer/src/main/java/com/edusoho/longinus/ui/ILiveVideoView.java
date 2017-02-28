package com.edusoho.longinus.ui;

/**
 * Created by suju on 16/10/18.
 */
public interface ILiveVideoView {

    void showNoticeView();

    void hideNoticeView();

    void setNotice(String notice);

    void setLivePlayStatus(String playStatus);

    void checkLivePlayStatus();

    void onLeaveRoom();

    void setRoomPrepareStatus(int status);

    void showChatRoomLoadView(String title);

    void hideChatRoomLoadView();

    void addChatRoomView();
}
