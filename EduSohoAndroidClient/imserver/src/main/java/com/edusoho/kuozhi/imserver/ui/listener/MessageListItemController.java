package com.edusoho.kuozhi.imserver.ui.listener;

/**
 * Created by suju on 16/8/28.
 */
public interface MessageListItemController {

    void onAudioClick(int position, String audioFile, AudioPlayStatusListener listener);

    void onImageClick(String imageUrl);

    void onErrorClick(int position);

    void onAvatarClick(int userId);

    void onContentClick(int position);

    void onUpdateRole(String type, int rid);

}
