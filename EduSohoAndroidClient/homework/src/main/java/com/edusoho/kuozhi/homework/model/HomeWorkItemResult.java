package com.edusoho.kuozhi.homework.model;

import java.util.List;

/**
 * Created by howzhi on 15/10/20.
 */
public class HomeWorkItemResult {
    public int id;
    public int itemId;
    public int homeworkId;
    public int homeworkResultId;
    public int questionId;
    public int questionParentId;
    public String status;
    public String teacherSay;
    public String questionType;

    public List<HomeWorkItemResult> items;
    public List<String> answer;
}
