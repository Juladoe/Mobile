package com.edusoho.kuozhi.v3.model.bal.test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by howzhi on 14-9-23.
 */
public class Testpaper {
    public int id;
    public int limitedTime;
    public int itemCount;
    public int createdUserId;
    public int updatedUserId;
    public String name;
    public String pattern;
    public String target;
    public String status;
    public double score;
    public String description;
    public String createdTime;
    public String updatedTime;
    public String passedScore;

    public Metas metas;

    public static class Metas
    {
        public HashMap<QuestionType, String> missScore;
        public ArrayList<QuestionType> question_type_seq;
    }
}
