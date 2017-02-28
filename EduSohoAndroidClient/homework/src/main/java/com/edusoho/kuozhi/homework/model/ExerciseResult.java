package com.edusoho.kuozhi.homework.model;

import java.util.List;

/**
 * Created by Melomelon on 2015/10/26.
 */
public class ExerciseResult {
    public int id;
    public int exerciseId;
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

    public List<HomeWorkItemResult> getItems() {
        return items;
    }
}
