package com.edusoho.kuozhi.homework.model;

import java.util.List;

/**
 * Created by howzhi on 15/10/20.
 */
public class HomeWorkResult {

    public int id;
    public int homeworkId;
    public int courseId;
    public int lessonId;
    public int userId;
    public String teacherSay;
    public int rightItemCount;
    public String status;
    public int checkTeacherId;
    public String checkedTime;
    public String usedTime;
    public String updatedTime;
    public String createdTime;
    public String passedStatus;

    public List<HomeWorkItemResult> items;
}
