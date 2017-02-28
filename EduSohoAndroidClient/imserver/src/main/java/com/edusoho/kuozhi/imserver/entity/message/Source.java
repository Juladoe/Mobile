package com.edusoho.kuozhi.imserver.entity.message;

/**
 * Created by 菊 on 2016/5/13.
 */
public class Source {

    private int id;
    private String nickname;
    private String image;
    private String type;

    public Source() {
    }

    public Source(int id, String type)
    {
        this.id = id;
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getImage() {
        return image;
    }

    public String getType() {
        return type;
    }
}
