package com.edusoho.kuozhi.v3.view.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.model.bal.test.Question;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionTypeSeq;

/**
 * Created by howzhi on 14-9-29.
 */
public class QuestionWidget {

    private Context mContext;
    private QuestionTypeSeq mQuestionSeq;
    private int mIndex;
    private BaseQuestionWidget mWidget;

    public QuestionWidget(
            Context context, QuestionTypeSeq questionSeq, int index) {
        mIndex = index;
        mContext = context;
        mQuestionSeq = questionSeq;
        init();
    }

    private void init() {
        int layoutId = 0;
        Question mQuestion = mQuestionSeq.question;
        switch (mQuestion.type) {
            case choice:
            case uncertain_choice:
                layoutId = R.layout.choice_viewpager_item;
                break;
            case single_choice:
                layoutId = R.layout.singlechoice_viewpager_item;
                break;
            case essay:
                layoutId = R.layout.essay_viewpager_item;
                break;
            case determine:
                layoutId = R.layout.determine_list_item;
                break;
            case fill:
                layoutId = R.layout.fill_list_item;
        }
        mWidget = (BaseQuestionWidget) LayoutInflater.from(mContext).inflate(
                layoutId, null);
        mWidget.setData(mQuestionSeq, mIndex);
    }

    public View getView() {
        return mWidget;
    }
}
