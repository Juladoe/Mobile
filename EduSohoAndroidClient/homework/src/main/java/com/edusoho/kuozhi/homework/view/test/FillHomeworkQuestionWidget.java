package com.edusoho.kuozhi.homework.view.test;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.homework.ExerciseActivity;
import com.edusoho.kuozhi.homework.HomeworkActivity;
import com.edusoho.kuozhi.homework.HomeworkSummaryActivity;
import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.model.bal.test.MaterialQuestionTypeSeq;
import com.edusoho.kuozhi.v3.model.bal.test.Question;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionTypeSeq;
import com.edusoho.kuozhi.v3.model.bal.test.TestResult;
import com.edusoho.kuozhi.v3.ui.test.TestpaperActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.html.EduImageGetterHandler;
import com.edusoho.kuozhi.v3.util.html.EduTagHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.trinea.android.common.util.ImageUtils;


/**
 * Created by howzhi on 14-9-29.
 */
public class FillHomeworkQuestionWidget extends BaseHomeworkQuestionWidget {

    protected LinearLayout fillLayout;
    private int mSpaceCount;

    public FillHomeworkQuestionWidget(Context context) {
        super(context);
    }

    public FillHomeworkQuestionWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private TextWatcher onTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int index, int i2, int i3) {
            updateAnswerData();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    @Override
    protected void parseQuestionAnswer() {
        mWorkMode = PARSE;
        if (mQuestion.getResult() != null) {
            enable(fillLayout, false);
            mAnalysisVS = (ViewStub) this.findViewById(R.id.hw_quetion_analysis);
            mAnalysisVS.setOnInflateListener(new ViewStub.OnInflateListener() {
                @Override
                public void onInflate(ViewStub viewStub, View view) {
                    initResultAnalysis(view);
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
        int count = fillLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            EditText edtView = (EditText) fillLayout.getChildAt(i);
            edtView.setText(resultData.get(i));
        }
    }

    private void updateAnswerData() {
        Bundle bundle = new Bundle();
        bundle.putInt("index", mIndex - 1);
        bundle.putString("QuestionType", QuestionType.material.name());
        ArrayList<String> data = new ArrayList<String>();
        int count = fillLayout.getChildCount();
        for (int i=0; i < count; i++) {
            EditText editText = (EditText) fillLayout.getChildAt(i);
            data.add(editText.getText().toString());
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

    @Override
    protected Spanned getQuestionStem() {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        Spanned spanned = Html.fromHtml(
                coverHtmlTag(mQuestion.getStem()),
                new EduImageGetterHandler(mContext, stemView),
                new EduTagHandler()
        );

        spannableStringBuilder
                .append(mIndex + " 、 ")
                .append(spanned);
        return parseStem(spannableStringBuilder);
    }

    @Override
    protected void invalidateData() {
        super.invalidateData();
        fillLayout = (LinearLayout) this.findViewById(R.id.hw_question_fill_layout);
        fillLayout.removeAllViews();

        Resources resources = mContext.getResources();
        for (int i=1; i <= mSpaceCount; i++) {
            EditText editText = new EditText(mContext);
            editText.setSingleLine();
            int padding = AppUtil.dp2px(mContext, 8);
            editText.setPadding(padding, padding, padding, padding);
            editText.setHint("答案" + i);
            editText.setTextColor(resources.getColor(R.color.homework_fill_edt_text));
            editText.setHintTextColor(resources.getColor(R.color.homework_fill_edt_hit));
            editText.setBackgroundDrawable(resources.getDrawable(R.drawable.homework_fill_edt_bg));
            editText.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.question_fill));
            editText.addTextChangedListener(onTextChangedListener);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.topMargin = AppUtil.dp2px(mContext, 16);
            fillLayout.addView(editText, layoutParams);
        }

        restoreResult(mQuestion.getAnswer());
        parseQuestionAnswer();
    }

    private Spanned parseStem(SpannableStringBuilder stem)
    {
        Pattern stemPattern = Pattern.compile("(\\[\\[[^\\[\\]]+\\]\\])", Pattern.DOTALL);
        Matcher matcher = stemPattern.matcher(stem);
        Paint paint = new Paint();
        paint.setTextSize(20);
        paint.setColor(mContext.getResources().getColor(R.color.base_black_87));
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

        while (matcher.find()) {
            mSpaceCount ++;
            CharSequence countStr = stem.subSequence(matcher.start(), matcher.end());
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.homework_fill_span_bg);
            drawable.setBounds(0, 0, 80, 40);

            Bitmap bitmap = Bitmap.createBitmap(80, 40, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);
            canvas.drawText("(" + mSpaceCount + ")", 25, 25, paint);
            ImageSpan imageSpan = new ImageSpan(mContext, bitmap);
            SpannableString spannableString = new SpannableString(countStr);
            spannableString.setSpan(imageSpan, 0, countStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            stem.delete(matcher.start(), matcher.end());
            stem.insert(matcher.start(), spannableString);
        }

        return stem;
    }
    @Override
    protected void initView(AttributeSet attrs) {
    }

    @Override
     public void setData(HomeWorkQuestion questionSeq, int index) {
        super.setData(questionSeq, index);
        invalidateData();
    }

    @Override
    protected String listToStr(List<String> arrayList)
    {
        int index = 1;
        StringBuilder stringBuilder = new StringBuilder("\n\n");
        for (String answer : arrayList) {
            if (TextUtils.isEmpty(answer)) {
                continue;
            }
            stringBuilder.append(String.format("%d:", index++));
            stringBuilder.append(answer);
            stringBuilder.append("\n\n");
        }
        int length = stringBuilder.length();
        if (length > 0) {
            stringBuilder.delete(length - 2, length);
        }
        return stringBuilder.toString();
    }
}
