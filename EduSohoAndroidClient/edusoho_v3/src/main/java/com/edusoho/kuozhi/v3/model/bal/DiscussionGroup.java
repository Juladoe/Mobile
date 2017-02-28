package com.edusoho.kuozhi.v3.model.bal;

/**
 * Created by Melomelon on 2015/9/10.
 */
public class DiscussionGroup extends Friend {

    public String picture;

    public String conversationId;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public String getNickname() {
        return title;
    }

    @Override
    public String getMediumAvatar() {
        return picture;
    }

    @Override
    public String getLargeAvatar() {
        return picture;
    }

    @Override
    public String getSmallAvatar() {
        return picture;
    }
}
