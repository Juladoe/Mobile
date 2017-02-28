package com.edusoho.kuozhi.v3.model.bal.test;

import java.util.ArrayList;

/**
 * Created by howzhi on 14-9-23.
 */
public class Question {

    public int id;
    public QuestionType type;
    public String stem;
    public double score;

    public int categoryId;
    public String difficulty;
    public String target;
    public int parentId;
    public int subCount;
    public int finishedTimes;
    public int passedTimes;
    public int userId;
    public String updatedTime;
    public String createdTime;
    public String analysis;

    public ArrayList<String> answer;
    public ArrayList<String> metas;
    public TestResult testResult;
}
