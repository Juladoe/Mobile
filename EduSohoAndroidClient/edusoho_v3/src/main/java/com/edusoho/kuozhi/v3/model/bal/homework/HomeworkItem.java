package com.edusoho.kuozhi.v3.model.bal.homework;

/**
 * Created by Melomelon on 2015/10/14.
 */
public class HomeworkItem {
    public String id;
    public String Type;
    public String stem;
    public String[] answer;
    public String analysis;
    public Metas metas;
    public String difficulty;

    public class Metas {
        public String[] choices;
    }


}
