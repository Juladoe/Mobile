package com.edusoho.kuozhi.v3.model.bal.push;


/**
 * Created by JesseHuang on 15/7/7.
 */
public class Bulletin {

    public int id;
    public String title;
    public String type;
    public long createTime;

    private String avatar;

    public Bulletin() {
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
