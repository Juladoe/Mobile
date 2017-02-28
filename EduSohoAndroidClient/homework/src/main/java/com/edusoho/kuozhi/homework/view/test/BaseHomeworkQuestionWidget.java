package com.edusoho.kuozhi.homework.view.test;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.homework.HomeworkSummaryActivity;
import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.model.HomeWorkItemResult;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.html.EduHtml;
import com.edusoho.kuozhi.v3.util.html.EduImageGetterHandler;
import com.edusoho.kuozhi.v3.util.html.EduTagHandler;
import com.edusoho.kuozhi.v3.util.html.ImageClickSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by howzhi on 14-9-29.
 */
public abstract class BaseHomeworkQuestionWidget extends LinearLayout implements IHomeworkQuestionWidget {

    protected TextView stemView;
    protected TextView mMaterialStem;
    protected Context mContext;
    protected HomeWorkQuestion mQuestion;
    protected ViewStub mAnalysisVS;

    protected int mIndex;
    protected int mWorkMode;

    protected String mType;

    public static final int PARSE = 0001;
    public static final int WORK = 0002;

    public static final String[] CHOICE_ANSWER = {
            "A", "B", "C",
            "D", "E", "F",
            "G", "H", "I",
            "J", "K", "L"
    };

    public static final String[] DETERMINE_ANSWER = {"错误", "正确"};

    public BaseHomeworkQuestionWidget(Context context) {
        super(context);
        mContext = context;
        initView(null);
    }

    public BaseHomeworkQuestionWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(attrs);
    }

    protected abstract void initView(AttributeSet attrs);
    protected abstract void parseQuestionAnswer();
    protected abstract void restoreResult(List<String> resultData);

    protected void invalidateData() {
        mWorkMode = WORK;
        stemView = (TextView) this.findViewById(R.id.homework_question_stem);

        SpannableStringBuilder spanned = (SpannableStringBuilder) getQuestionStem();
        spanned = EduHtml.addImageClickListener(spanned, stemView, mContext);
        stemView.setText(spanned);
        initMaterialStem();
    }

    @Override
    public void setData(HomeWorkQuestion question, int index) {
        mIndex = index;
        mQuestion = question;
    }

    public void setType(String type) {
        this.mType = type;
    }

    protected void initMaterialStem() {
        HomeWorkQuestion parent = mQuestion.getParent();
        if (parent != null) {
            ViewStub viewStub = (ViewStub) findViewById(R.id.homework_material_stem);
            mMaterialStem = (TextView) viewStub.inflate();

            SpannableStringBuilder spanned = new SpannableStringBuilder();
            Spanned stemBody = Html.fromHtml(
                    parent.getStem(),
                    new EduImageGetterHandler(mContext, mMaterialStem),
                    new EduTagHandler()
            );

            spanned.append("(材料题) ")
                    .append(stemBody);
            mMaterialStem.setText(EduHtml.addImageClickListener(spanned, mMaterialStem, mContext));
        }

    }

    protected String getPlainText(String text) {
        return AppUtil.removeHtmlSpace(text).replace("\r\n", "");
    }

    /**
     * 获取题干
     */
    protected Spanned getQuestionStem() {
        String stem = String.format("%d 、", mIndex);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        SpannableStringBuilder stemSpanned = (SpannableStringBuilder) Html.fromHtml(
                coverHtmlTag(mQuestion.getStem()),
                new EduImageGetterHandler(mContext, stemView),
                new EduTagHandler()
        );

        spannableStringBuilder
                .append(stem)
                .append(stemSpanned);

        return spannableStringBuilder;
    }

    protected String coverHtmlTag(String stem) {
        return stem.replace("p>", "ep>").replace("div>", "ediv>");
    }

    private SpannableStringBuilder handleImageClick(SpannableStringBuilder spanned) {

        CharacterStyle[] characterStyles = spanned.getSpans(0, spanned.length(), CharacterStyle.class);
        int index = 0;
        for (CharacterStyle characterStyle : characterStyles) {
            if (characterStyle instanceof ImageSpan) {
                ImageSpan imageSpan = (ImageSpan) characterStyle;
                String src = imageSpan.getSource();
                ImageClickSpan clickSpan = new ImageClickSpan(mContext, src, index);
                int start = spanned.getSpanStart(characterStyle);
                int end = spanned.getSpanEnd(characterStyle);
                index++;
                spanned.setSpan(clickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return spanned;
    }

    protected void enable(ViewGroup viewGroup, boolean isEnable) {
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(isEnable);
        }
    }

    protected String listToStr(List<String> arrayList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String answer : arrayList) {
            if (TextUtils.isEmpty(answer)) {
                continue;
            }
            QuestionType type = QuestionType.value(mQuestion.getType());
            stringBuilder.append(getAnswerByType(type, answer));
            stringBuilder.append(",");
        }
        int length = stringBuilder.length();
        if (length > 0) {
            stringBuilder.delete(length - 1, length);
        }
        return stringBuilder.toString();
    }

    protected String getAnswerByType(QuestionType qt, String answer) {
        switch (qt) {
            case choice:
            case single_choice:
            case uncertain_choice:
                return parseAnswer(answer);
            case determine:
                return parseDetermineAnswer(answer);
            case essay:
            case fill:
                return answer;
        }

        return "";
    }

    private String parseDetermineAnswer(String answer) {
        int index = 0;
        try {
            index = Integer.parseInt(answer);
        } catch (Exception e) {
            index = 0;
        }

        return DETERMINE_ANSWER[index];
    }

    private String parseAnswer(String answer) {
        int index = 0;
        try {
            index = Integer.parseInt(answer);
        } catch (Exception e) {
            index = 0;
        }
        return CHOICE_ANSWER[index];
    }

    protected void initResultAnalysis(View view) {
        ImageView answerStatusView = (ImageView) view.findViewById(R.id.hw_question_answer_icon);
        TextView myAnswerText = (TextView) view.findViewById(R.id.hw_question_my_anwer);
        TextView myRightText = (TextView) view.findViewById(R.id.hw_question_right_anwer);
        TextView AnalysisText = (TextView) view.findViewById(R.id.hw_question_analysis);

        HomeWorkItemResult testResult = mQuestion.getResult();
        String myAnswer = null;
        if ("noAnswer".equals(testResult.status)) {
            myAnswer = "未答题";
        } else {
            myAnswer = listToStr(testResult.answer);
        }

        setAnswerStatusIcon(answerStatusView, testResult.status);
        int rightColor = mContext.getResources().getColor(R.color.primary);
        SpannableString rightText = AppUtil.getColorTextAfter(
                "正确答案:", listToStr(mQuestion.getAnswer()), rightColor);
        myAnswerText.setText("你的答案:" + myAnswer);
        myRightText.setText(rightText);

        AnalysisText.setText(
                TextUtils.isEmpty(mQuestion.getAnalysis()) ? "暂无解析" : Html.fromHtml(mQuestion.getAnalysis()));
    }

    private void setAnswerStatusIcon(ImageView answerStatusView, String status) {
        switch (status) {
            case "wrong":
            case "noAnswer":
                answerStatusView.setImageResource(R.drawable.hw_question_answer_wrong);
                answerStatusView.setBackgroundResource(R.drawable.hw_question_answer_bg_wrong);
                break;
            case "partright":
                answerStatusView.setImageResource(R.drawable.hw_question_answer_partright);
                answerStatusView.setBackgroundResource(R.drawable.hw_question_answer_bg_partright);
                break;
            case "right":
                answerStatusView.setImageResource(R.drawable.hw_question_answer_right);
                answerStatusView.setBackgroundResource(R.drawable.hw_question_answer_bg_right);
        }
    }
}
