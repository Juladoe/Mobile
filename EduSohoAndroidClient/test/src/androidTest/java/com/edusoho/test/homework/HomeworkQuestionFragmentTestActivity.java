package com.edusoho.test.homework;

import com.edusoho.kuozhi.homework.listener.IHomeworkQuestionResult;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.test.FragmentTestActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Melomelon on 2015/11/17.
 */
public class HomeworkQuestionFragmentTestActivity extends FragmentTestActivity implements IHomeworkQuestionResult {


    @Override
    public List<HomeWorkQuestion> getQuestionList() {
        return new ArrayList<HomeWorkQuestion>();
    }

    @Override
    public int getCurrentQuestionIndex() {
        return 0;
    }

    @Override
    public void setCurrentQuestionIndex(int index) {
    }

    @Override
    public String getType() {
        return "homework";
    }
}
