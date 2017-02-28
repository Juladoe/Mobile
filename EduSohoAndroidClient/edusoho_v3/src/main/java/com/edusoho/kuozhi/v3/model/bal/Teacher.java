package com.edusoho.kuozhi.v3.model.bal;

import java.io.Serializable;

/**
 * Created by JesseHuang on 15/6/14.
 */
public class Teacher implements Serializable {
    public int id;
    public String nickname;
    public String title;
    public int following;
    public int follower;
    public String smallAvatar;
    public String avatar;

    public String getAvatar() {
        if(avatar == null){
            return "";
        }
        int schemIndex = avatar.lastIndexOf("http://");
        if (schemIndex != -1) {
            return avatar.substring(schemIndex);
        }
        return avatar;
    }

    public String getSmallAvatar() {
        if(smallAvatar == null){
            return "";
        }
        int schemIndex = smallAvatar.lastIndexOf("http://");
        if (schemIndex != -1) {
            return smallAvatar.substring(schemIndex);
        }
        return smallAvatar;
    }
}
