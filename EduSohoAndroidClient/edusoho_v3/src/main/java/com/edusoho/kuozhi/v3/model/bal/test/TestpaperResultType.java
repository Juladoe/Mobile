package com.edusoho.kuozhi.v3.model.bal.test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by howzhi on 14-10-9.
 */
public class TestpaperResultType {

    public HashMap<QuestionType, ArrayList<QuestionTypeSeq>> items;
    public HashMap<QuestionType, Accuracy> accuracy;
    public PaperResult paperResult;
    public Testpaper testpaper;
    public ArrayList<Integer> favorites;
}
