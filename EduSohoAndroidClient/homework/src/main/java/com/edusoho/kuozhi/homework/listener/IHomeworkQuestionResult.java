package com.edusoho.kuozhi.homework.listener;

import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;

import java.util.List;

/**
 * Created by howzhi on 15/10/16.
 */
public interface IHomeworkQuestionResult {

    public List<HomeWorkQuestion> getQuestionList();

    public int getCurrentQuestionIndex();

    public void setCurrentQuestionIndex(int index);

    public String getType();
}
