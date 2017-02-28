package com.edusoho.kuozhi.v3.view.test;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.Answer;
import com.edusoho.kuozhi.v3.model.bal.test.MaterialQuestionTypeSeq;
import com.edusoho.kuozhi.v3.model.bal.test.Question;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionTypeSeq;
import com.edusoho.kuozhi.v3.model.bal.test.TestResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.test.TestpaperActivity;
import com.edusoho.kuozhi.v3.ui.test.TestpaperParseActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.html.EduHtml;
import com.edusoho.kuozhi.v3.util.html.EduImageGetterHandler;
import com.edusoho.kuozhi.v3.util.html.EduTagHandler;
import com.edusoho.kuozhi.v3.view.EduSohoTextBtn;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by howzhi on 14-9-29.
 */
public abstract class BaseQuestionWidget extends RelativeLayout implements IQuestionWidget {

    protected TextView stemView;
    protected Context mContext;
    protected QuestionTypeSeq mQuestionSeq;
    protected Question mQuestion;
    protected ViewStub mAnalysisVS;

    protected int mIndex;

    public static final String[] CHOICE_ANSWER = {
            "A", "B", "C",
            "D", "E", "F",
            "G", "H", "I",
            "J", "K", "L"
    };

    public static final String[] DETERMINE_ANSWER = {"错误", "正确"};

    public BaseQuestionWidget(Context context) {
        super(context);
        mContext = context;
        initView(null);
    }

    public BaseQuestionWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(attrs);
    }

    protected abstract void initView(AttributeSet attrs);

    protected void invalidateData() {
        stemView = (TextView) this.findViewById(R.id.question_stem);

        SpannableStringBuilder spanned = (SpannableStringBuilder) getQuestionStem();
        spanned = EduHtml.addImageClickListener(spanned, stemView, mContext);
        stemView.setText(spanned);

        TestpaperActivity testpaperActivity = TestpaperActivity.getInstance();
        if (testpaperActivity == null) {
            return;
        }
        QuestionType questionType = null;
        if (mQuestionSeq instanceof MaterialQuestionTypeSeq) {
            questionType = QuestionType.material;
        } else {
            questionType = mQuestionSeq.questionType;
        }
        ArrayList<Answer> answerArrayList = testpaperActivity.getAnswer().get(questionType);
        Answer answer = answerArrayList.get(mIndex - 1);
        if (answer != null && answer.data != null) {
            restoreResult(answer.data);
        }
    }

    protected abstract void restoreResult(ArrayList resultData);

    @Override
    public void setData(QuestionTypeSeq questionSeq, int index) {
        mIndex = index;
        mQuestionSeq = questionSeq;
        mQuestion = mQuestionSeq.question;
    }

    /**
     * 获取题干
     */
    protected Spanned getQuestionStem() {
        String stem = "";
        Question mQuestion = mQuestionSeq.question;
        switch (mQuestion.type) {
            case choice:
            case uncertain_choice:
            case single_choice:
            case essay:
            case material:
            case determine:
            case fill:
                stem = String.format(
                        "%d, (<font color='#ffca4a'>%.2f分</font>) %s",
                        mIndex,
                        mQuestion.score,
                        mQuestion.stem
                );
        }

        return Html.fromHtml(
                stem,
                new EduImageGetterHandler(mContext, stemView),
                new EduTagHandler()
        );
    }

    protected void enable(ViewGroup viewGroup, boolean isEnable) {
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(isEnable);
        }
    }


    protected String listToStr(ArrayList<String> arrayList) {
        StringBuilder stringBuilder = new StringBuilder();
        Collections.sort(arrayList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                int l = AppUtil.parseInt(lhs);
                int r = AppUtil.parseInt(rhs);
                return l - r;
            }
        });
        for (String answer : arrayList) {
            if (TextUtils.isEmpty(answer)) {
                continue;
            }
            stringBuilder.append(getAnswerByType(mQuestion.type, answer));
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

    protected void initFavoriteBtn(View view) {
        TestpaperParseActivity testpaperParseActivity = TestpaperParseActivity.getInstance();
        if (testpaperParseActivity == null) {
            return;
        }

        final EduSohoTextBtn favoriteBtn = (EduSohoTextBtn) view.findViewById(R.id.question_favorite);
        ArrayList<Integer> favorites = testpaperParseActivity.getFavorites();
        if (favorites.contains(mQuestion.id)) {
            favoriteBtn.setTag(true);
            favoriteBtn.setIcon(R.string.font_favorited);
            favoriteBtn.setTextColor(getResources().getColor(R.color.course_favorited));
        }

        favoriteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isFavorite;
                Object tag = favoriteBtn.getTag();
                if (tag == null) {
                    isFavorite = false;
                } else {
                    isFavorite = (Boolean) tag;
                }

                favoriteQuestion(mQuestion.id, mQuestionSeq.testId, !isFavorite, favoriteBtn);
            }
        });
    }

    private void favoriteQuestion(
            int questionId, int targetId, final boolean isFavorite, final EduSohoTextBtn btn) {
        final TestpaperParseActivity testpaperParseActivity = TestpaperParseActivity.getInstance();
        if (testpaperParseActivity == null) {
            return;
        }

        EdusohoApp app = testpaperParseActivity.app;
        RequestUrl requestUrl = app.bindUrl(Const.FAVORITE_QUESTION, true);
        requestUrl.setParams(new String[]{
                "targetType", "testpaper",
                "targetId", targetId + "",
                "id", questionId + "",
                "action", isFavorite ? "favorite" : "unFavorite"
        });

        btn.setEnabled(false);
        testpaperParseActivity.setProgressBarIndeterminateVisibility(true);

        testpaperParseActivity.ajaxPost(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                btn.setEnabled(true);
                testpaperParseActivity.setProgressBarIndeterminateVisibility(false);
                Boolean result = testpaperParseActivity.parseJsonValue(
                        response, new TypeToken<Boolean>() {
                        });
                if (result == null) {
                    return;
                }
                Toast.makeText(
                        mContext,
                        isFavorite ? "收藏成功!" : "取消收藏成功!",
                        Toast.LENGTH_SHORT
                ).show();

                btn.setTag(isFavorite);
                if (isFavorite) {
                    btn.setIcon(R.string.font_favorited);
                    btn.setTextColor(getResources().getColor(R.color.course_favorited));
                } else {
                    btn.setIcon(R.string.font_favorite);
                    btn.setTextColor(getResources().getColor(R.color.base_black_normal));
                }
            }
        }, null);
    }

    protected ArrayList<String> coverResultAnswer(Object answer) {
        ArrayList<String> answerList = null;
        if (answer == null) {
            return new ArrayList<>();
        }
        if (answer instanceof LinkedHashMap) {
            LinkedHashMap answerMap = (LinkedHashMap) answer;
            answerList = new ArrayList<>();
            List<String> keyList = new ArrayList<>(answerMap.keySet());
            Collections.sort(keyList);
            for (String key : keyList) {
                answerList.add(answerMap.get(key).toString());
            }
            return answerList;
        } else if (answer instanceof ArrayList){
            return  (ArrayList<String>) answer;
        }

        return new ArrayList<>();
    }

    protected void initResultAnalysis(View view) {
        TextView myAnswerText = (TextView) view.findViewById(R.id.question_my_anwer);
        TextView myRightText = (TextView) view.findViewById(R.id.question_right_anwer);
        TextView AnalysisText = (TextView) view.findViewById(R.id.question_analysis);

        TestResult testResult = mQuestion.testResult;
        String myAnswer = null;
        if ("noAnswer".equals(testResult.status)) {
            myAnswer = "未答题";
        } else {
            myAnswer = listToStr(coverResultAnswer(testResult.answer));
        }

        int rightColor = mContext.getResources().getColor(R.color.testpaper_result_right);
        SpannableString rightText = AppUtil.getColorTextAfter(
                "正确答案:", listToStr(mQuestion.answer), rightColor);
        myAnswerText.setText("你的答案:" + myAnswer);
        myRightText.setText(rightText);

        AnalysisText.setText(
                TextUtils.isEmpty(mQuestion.analysis) ? "暂无解析" : Html.fromHtml(mQuestion.analysis));
        initFavoriteBtn(view);
    }
}
