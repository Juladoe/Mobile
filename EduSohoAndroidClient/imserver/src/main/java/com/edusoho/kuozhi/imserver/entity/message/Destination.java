package com.edusoho.kuozhi.imserver.entity.message;

/**
 * Created by 菊 on 2016/5/13.
 */
public class Destination {

    /**
     * course/classroom/user
     */

    public static final String COURSE = "course";
    public static final String GROUP = "group";
    public static final String LESSON = "lesson";
    public static final String CLASSROOM = "classroom";
    public static final String USER = "user";
    public static final String NOTIFY = "notify";
    public static final String ARTICLE = "news";
    public static final String LIST = "list";
    public static final String GLOBAL = "global";

    public String type;

    public String nickname;

    public int id;

    public Destination() {
    }

    public Destination(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
