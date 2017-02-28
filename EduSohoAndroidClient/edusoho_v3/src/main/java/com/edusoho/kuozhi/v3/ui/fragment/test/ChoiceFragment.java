package com.edusoho.kuozhi.v3.ui.fragment.test;

import android.os.Bundle;
import android.view.View;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.test.QuestionAdapter;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionTypeSeq;

import java.util.ArrayList;

/**
 * Created by howzhi on 14-9-24.
 */
public class ChoiceFragment extends SelectQuestionFragment {

    private QuestionType type = QuestionType.choice;

    @Override
    public String getTitle() {
        return "多选题";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.choice_fragment_layout);
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        refreshViewData();
    }

    @Override
    protected void refreshViewData() {
        ArrayList<QuestionTypeSeq> questionTypeSeqs = getQuestion(type);
        if (questionTypeSeqs == null) {
            return;
        }

        mQuestionCount = questionTypeSeqs.size();
        setQuestionTitle(type.title(), questionTypeSeqs);
        setQuestionNumber(mCurrentIndex);

        QuestionAdapter adapter = new QuestionAdapter(
                mContext, questionTypeSeqs
        );

        mQuestionPager.setAdapter(adapter);
    }
}
