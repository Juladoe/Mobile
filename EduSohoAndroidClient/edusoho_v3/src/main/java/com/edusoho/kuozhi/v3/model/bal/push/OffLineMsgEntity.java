package com.edusoho.kuozhi.v3.model.bal.push;

import java.io.Serializable;

/**
 * Created by JesseHuang on 15/10/8.
 */
public class OffLineMsgEntity implements Serializable {

    private String content;
    private String id;
    private String title;
    private String schoolId;
    private V2CustomContent custom;

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public void setCustom(V2CustomContent custom) {
        this.custom = custom;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public V2CustomContent getCustom() {
        return custom;
    }
}
