package com.edusoho.kuozhi.homework.view.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;

/**
 * Created by howzhi on 14-9-29.
 */
public class HomeworkQuestionWidget {

    private Context mContext;
    private HomeWorkQuestion mQuestionSeq;
    private int mIndex;
    protected String mType;
    private BaseHomeworkQuestionWidget mWidget;

    public HomeworkQuestionWidget(
            String type, Context context, HomeWorkQuestion question, int index) {
        mIndex = index;
        mType = type;
        mContext = context;
        mQuestionSeq = question;
        init();
    }

    private void init() {
        int layoutId = 0;
        QuestionType type = QuestionType.value(mQuestionSeq.getType());
        switch (type) {
            case choice:
            case uncertain_choice:
                layoutId = R.layout.homework_choice_item;
                break;
            case single_choice:
                layoutId = R.layout.hw_singlechoice_item;
                break;
            case essay:
                layoutId = R.layout.hw_essay_item;
                break;
            case determine:
                layoutId = R.layout.hw_determine_item;
                break;
            case fill:
                layoutId = R.layout.hw_fill_item;
        }
        mWidget = (BaseHomeworkQuestionWidget) LayoutInflater.from(mContext).inflate(
                layoutId, null);
        mWidget.setType(mType);
        mWidget.setData(mQuestionSeq, mIndex);
    }

    public View getView() {
        return mWidget;
    }
}
