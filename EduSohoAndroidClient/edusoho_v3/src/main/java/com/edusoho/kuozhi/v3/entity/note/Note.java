package com.edusoho.kuozhi.v3.entity.note;

import java.io.Serializable;

/**
 * Created by JesseHuang on 16/5/9.
 */
public class Note implements Serializable {
    public int id;
    public int userId;
    public int courseId;
    public int lessonId;
    public String content;
    public String length;
    public int likeNum;
    public int status;
    public String createdTime;
    public String updatedTime;
}
