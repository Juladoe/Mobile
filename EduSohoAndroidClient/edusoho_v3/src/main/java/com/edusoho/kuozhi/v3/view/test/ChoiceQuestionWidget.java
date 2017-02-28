package com.edusoho.kuozhi.v3.view.test;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.test.MaterialQuestionTypeSeq;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionTypeSeq;
import com.edusoho.kuozhi.v3.ui.test.TestpaperActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;

import java.util.ArrayList;


/**
 * Created by howzhi on 14-9-29.
 */
public class ChoiceQuestionWidget extends BaseQuestionWidget {

    protected RadioGroup radioGroup;

    public ChoiceQuestionWidget(Context context) {
        super(context);
    }

    public ChoiceQuestionWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private CompoundButton.OnCheckedChangeListener checkedChangeListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    sendMsgToTestpaper();
                }
            };

    protected void sendMsgToTestpaper() {
        Bundle bundle = new Bundle();
        bundle.putInt("index", mIndex - 1);
        if (mQuestionSeq instanceof MaterialQuestionTypeSeq) {
            bundle.putString("QuestionType", QuestionType.material.name());
        } else {
            bundle.putString("QuestionType", mQuestionSeq.question.type.name());
        }

        int count = radioGroup.getChildCount();
        ArrayList<String> data = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            CompoundButton radioButton = (CompoundButton) radioGroup.getChildAt(i);
            if (radioButton.isChecked()) {
                data.add(i + "");
            }
        }

        bundle.putStringArrayList("data", data);
        EdusohoApp.app.sendMsgToTarget(
                TestpaperActivity.CHANGE_ANSWER, bundle, TestpaperActivity.class);
    }

    @Override
    protected void restoreResult(ArrayList resultData) {
        int count = radioGroup.getChildCount();
        for (int i=0; i < count; i++) {
            CheckBox child = (CheckBox) radioGroup.getChildAt(i);
            for (Object result : resultData) {
                if (result.equals(String.valueOf(i))) {
                    child.setChecked(true);
                    break;
                }
            }
        }
    }

    @Override
    protected void invalidateData() {
        radioGroup = (RadioGroup) this.findViewById(R.id.quetion_choice_group);

        ArrayList<String> metas = mQuestion.metas;
        if (metas != null) {
            int size = metas.size();
            for (int i = 0; i < size; i++) {
                CheckBox checkBox = initCheckBox(AppUtil.removeHtmlSpan(metas.get(i)), i + 1);
                checkBox.setOnCheckedChangeListener(checkedChangeListener);
                radioGroup.addView(checkBox);
            }
        }

        if (mQuestion.testResult != null) {
            enable(radioGroup, false);
            mAnalysisVS = (ViewStub) this.findViewById(R.id.quetion_choice_analysis);
            mAnalysisVS.setOnInflateListener(new ViewStub.OnInflateListener() {
                @Override
                public void onInflate(ViewStub viewStub, View view) {
                    initResultAnalysis(view);
                    initQuestionResult();
                }
            });
            mAnalysisVS.inflate();
        }
        super.invalidateData();
    }

    private void initQuestionResult() {
        int count = radioGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = radioGroup.getChildAt(i);
            for (String answer : mQuestion.answer) {
                if (answer.equals(String.valueOf(i))) {
                    child.setSelected(true);
                    break;
                }
            }
        }
    }

    @Override
    protected void initView(AttributeSet attrs) {
    }

    private CheckBox initCheckBox(String text, int index) {
        CheckBox checkBox = (CheckBox) LayoutInflater.from(mContext).inflate(
                R.layout.question_checkbox, null);
        checkBox.setText(text);
        Resources resources = mContext.getResources();
        checkBox.setTextColor(resources.getColorStateList(R.color.question_choice_btn_color));
        int id = resources.getIdentifier(
                "question_choice_" + index, "drawable", mContext.getPackageName());
        checkBox.setButtonDrawable(id);

        return checkBox;
    }

    @Override
    public void setData(QuestionTypeSeq questionSeq, int index) {
        super.setData(questionSeq, index);
        invalidateData();
    }
}
