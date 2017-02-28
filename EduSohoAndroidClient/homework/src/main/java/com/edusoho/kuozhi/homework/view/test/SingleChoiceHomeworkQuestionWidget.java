package com.edusoho.kuozhi.homework.view.test;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.edusoho.kuozhi.homework.ExerciseActivity;
import com.edusoho.kuozhi.homework.HomeworkActivity;
import com.edusoho.kuozhi.homework.HomeworkSummaryActivity;
import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.util.AppUtil;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by howzhi on 14-9-29.
 */
public class

SingleChoiceHomeworkQuestionWidget extends BaseHomeworkQuestionWidget {

    protected RadioGroup radioGroup;

    public SingleChoiceHomeworkQuestionWidget(Context context) {
        super(context);
    }

    public SingleChoiceHomeworkQuestionWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initView(AttributeSet attrs) {
    }

    @Override
    protected void invalidateData() {
        super.invalidateData();
        radioGroup = (RadioGroup) this.findViewById(R.id.hw_quetion_choice_group);

        List<String> metas = mQuestion.getMetas();
        if (metas != null) {
            int size = metas.size();
            for (int i = 0; i < size; i++) {
                RadioButton radioButton = initRadioButton(AppUtil.removeHtmlSpan(metas.get(i)), i + 1);
                if (i == 0) {
                    radioGroup.check(radioButton.getId());
                }
                RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.bottomMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.homework_question_choice_padding);
                radioGroup.addView(radioButton, layoutParams);
            }
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int index) {
                sendMsgToTestpaper();
            }
        });

        restoreResult(mQuestion.getAnswer());
        parseQuestionAnswer();
    }

    @Override
    protected void parseQuestionAnswer() {
        mWorkMode = PARSE;
        if (mQuestion.getResult() != null) {
            enable(radioGroup, false);
            mAnalysisVS = (ViewStub) this.findViewById(R.id.hw_quetion_analysis);
            mAnalysisVS.setOnInflateListener(new ViewStub.OnInflateListener() {
                @Override
                public void onInflate(ViewStub viewStub, View view) {
                    initResultAnalysis(view);
                    initQuestionResult(mQuestion.getAnswer());
                }
            });
            mAnalysisVS.inflate();
        }
    }

    @Override
    protected void restoreResult(List<String> resultData) {
        if (resultData == null) {
            return;
        }
        int count = radioGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            CompoundButton child = (CompoundButton) radioGroup.getChildAt(i);
            for (String answer : resultData) {
                if (answer.equals(String.valueOf(i))) {
                    child.setChecked(true);
                    break;
                }
            }
        }
    }

    private void initQuestionResult(List<String> resultData) {
        int count = radioGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = radioGroup.getChildAt(i);
            child.setSelected(true);
            for (String answer : resultData) {
                if (answer.equals(String.valueOf(i))) {
                    child.setActivated(true);
                    break;
                }
            }
        }
    }

    protected void sendMsgToTestpaper() {
        Bundle bundle = new Bundle();
        bundle.putInt("index", mIndex - 1);
        bundle.putString("QuestionType", QuestionType.material.name());
        int count = radioGroup.getChildCount();
        ArrayList<String> data = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            CompoundButton radioButton = (CompoundButton) radioGroup.getChildAt(i);
            if (radioButton.isChecked()) {
                data.add(i + "");
            }
        }

        bundle.putStringArrayList("data", data);
        if (HomeworkSummaryActivity.HOMEWORK.equals(mType)){
            MessageEngine.getInstance().sendMsgToTaget(
                    HomeworkActivity.CHANGE_ANSWER, bundle, HomeworkActivity.class);
        }else {
            MessageEngine.getInstance().sendMsgToTaget(
                    ExerciseActivity.CHANGE_ANSWER, bundle, ExerciseActivity.class);
        }

    }

    private RadioButton initRadioButton(String text, int index) {
        RadioButton radioButton = (RadioButton) LayoutInflater.from(mContext).inflate(
                R.layout.question_radiobox, null);
        radioButton.setText(getPlainText(text));
        Resources resources = mContext.getResources();
        radioButton.setTextColor(resources.getColor(R.color.assist));
        int id = resources.getIdentifier(
                "hw_question_choice_" + index, "drawable", mContext.getPackageName());
        radioButton.setButtonDrawable(id);

        return radioButton;
    }

    @Override
    public void setData(HomeWorkQuestion questionSeq, int index) {
        super.setData(questionSeq, index);
        invalidateData();
    }
}
