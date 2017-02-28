package com.edusoho.kuozhi.v3.ui.fragment.test;

import android.os.Bundle;
import android.view.View;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.test.MaterialQuestionAdapter;
import com.edusoho.kuozhi.v3.model.bal.test.MaterialQuestionTypeSeq;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionTypeSeq;

import java.util.ArrayList;

/**
 * Created by howzhi on 14-9-24.
 */
public class MaterialFragment extends SelectQuestionFragment {

    private QuestionType type = QuestionType.material;

    @Override
    public String getTitle() {
        return "材料题";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.material_fragment_layout);
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

        ArrayList<MaterialQuestionTypeSeq> typeSeqs = coverQuestions(questionTypeSeqs);
        mQuestionCount = typeSeqs.size();
        setQuestionTitle(type.title(), questionTypeSeqs);
        setQuestionNumber(mCurrentIndex);

        MaterialQuestionAdapter adapter = new MaterialQuestionAdapter(
                mContext, typeSeqs
        );

        mQuestionPager.setAdapter(adapter);
        mQuestionPager.setOffscreenPageLimit(mQuestionCount);
    }

    private ArrayList<MaterialQuestionTypeSeq> coverQuestions(
            ArrayList<QuestionTypeSeq> questionTypeSeqs) {
        ArrayList<MaterialQuestionTypeSeq> list = new ArrayList<MaterialQuestionTypeSeq>();
        for (QuestionTypeSeq seq : questionTypeSeqs) {
            for (QuestionTypeSeq itemSeq : seq.items) {
                MaterialQuestionTypeSeq question = new MaterialQuestionTypeSeq(itemSeq, seq);
                list.add(question);
            }
        }

        return list;
    }

}
