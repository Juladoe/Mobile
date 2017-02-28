package com.edusoho.kuozhi.v3.ui.fragment.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;

import com.android.volley.Response;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.test.TestpaperInfoAdapter;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.bal.test.Testpaper;
import com.edusoho.kuozhi.v3.model.bal.test.TestpaperItem;
import com.edusoho.kuozhi.v3.model.bal.test.TestpaperResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.CourseDetailsTabActivity;
import com.edusoho.kuozhi.v3.ui.FragmentPageActivity;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.ui.test.TestpaperActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.ViewUtil;
import com.edusoho.kuozhi.v3.view.EduSohoAnimWrap;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by howzhi on 14-9-23.
 */
public class TestpaperLessonFragment extends BaseFragment {

    public static final String RESULT_ID = "resultId";

    @ViewUtil("testpaper_listview")
    private ListView mListView;

    @ViewUtil("testpaper_tools_layout")
    private View mToolsLayout;

    @ViewUtil("testpaper_do_btn")
    private View mTestpaperDoBtn;

    @ViewUtil("testpaper_redo_btn")
    private View mTestpaperReDoBtn;

    @ViewUtil("testpaper_show_btn")
    private View mTestpaperShowBtn;

    private int mTestId;
    private int mLessonId;
    private int mResultId;
    private String mStstus;
    private String mTitle;

    private Testpaper mTestpaper;

    @Override
    public String getTitle() {
        return "试卷课程";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setContainerView(R.layout.testpaper_fragment_layout);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTestId = bundle.getInt(Const.MEDIA_ID);
            mLessonId = bundle.getInt(Const.LESSON_ID);
            mResultId = bundle.getInt(RESULT_ID);
            mStstus = bundle.getString(Const.STATUS);
            mTitle = bundle.getString(Const.ACTIONBAR_TITLE);
        }
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        viewInject(view);

        loadTestpaperInfo();
        if ("nodo".equals(mStstus)) {
            //nothing
        } else if ("reviewing".equals(mStstus)) {
            mTestpaperShowBtn.setVisibility(View.VISIBLE);
            mTestpaperDoBtn.setVisibility(View.GONE);
        } else if ("finished".equals(mStstus)) {
            mTestpaperDoBtn.setVisibility(View.GONE);
            mTestpaperReDoBtn.setVisibility(View.VISIBLE);
            mTestpaperShowBtn.setVisibility(View.VISIBLE);
        }
        showToolsByAnim();

        mTestpaperReDoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doTestpaper(TestpaperActivity.REDO);
            }
        });
        mTestpaperDoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doTestpaper(TestpaperActivity.DO);
            }
        });

        mTestpaperShowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.mEngine.runNormalPlugin("FragmentPageActivity", mActivity, new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(FragmentPageActivity.FRAGMENT, "TestpaperResultFragment");
                        startIntent.putExtra(Const.ACTIONBAR_TITLE, mTitle + " 考试结果");
                        startIntent.putExtra(TestpaperResultFragment.RESULT_ID, mResultId);
                        startIntent.putExtra(Const.STATUS, mStstus);
                    }
                });
                mActivity.finish();
            }
        });
    }

    private void doTestpaper(int type) {
        Bundle bundle = new Bundle();
        bundle.putString(Const.ACTIONBAR_TITLE, mTitle);
        bundle.putInt(Const.MEDIA_ID, mTestId);
        bundle.putInt(Const.LESSON_ID, mLessonId);
        bundle.putInt(Const.TESTPAPER_DO_TYPE, type);
        bundle.putStringArray(CourseDetailsTabActivity.TITLES, getTestpaperQSeq());
        bundle.putStringArray(CourseDetailsTabActivity.LISTS, getTestpaperFragments());
        startActivityWithBundle("TestpaperActivity", bundle);
        mActivity.finish();
    }

    private String[] getTestpaperQSeq() {
        ArrayList<QuestionType> questionTypeSeqs = mTestpaper.metas.question_type_seq;
        String[] TESTPAPER_QUESTION_TYPE = new String[questionTypeSeqs.size()];
        for (int i = 0; i < TESTPAPER_QUESTION_TYPE.length; i++) {
            TESTPAPER_QUESTION_TYPE[i] = questionTypeSeqs.get(i).title();
        }

        return TESTPAPER_QUESTION_TYPE;
    }

    private String[] getTestpaperFragments() {
        ArrayList<QuestionType> questionTypeSeqs = mTestpaper.metas.question_type_seq;
        String[] TESTPAPER_QUESTIONS = new String[questionTypeSeqs.size()];
        for (int i = 0; i < TESTPAPER_QUESTIONS.length; i++) {
            switch (questionTypeSeqs.get(i)) {
                case choice:
                    TESTPAPER_QUESTIONS[i] = "ChoiceFragment";
                    break;
                case single_choice:
                    TESTPAPER_QUESTIONS[i] = "SingleChoiceFragment";
                    break;
                case essay:
                    TESTPAPER_QUESTIONS[i] = "EssayFragment";
                    break;
                case uncertain_choice:
                    TESTPAPER_QUESTIONS[i] = "UncertainChoiceFragment";
                    break;
                case fill:
                    TESTPAPER_QUESTIONS[i] = "FillFragment";
                    break;
                case determine:
                    TESTPAPER_QUESTIONS[i] = "DetermineFragment";
                    break;
                case material:
                    TESTPAPER_QUESTIONS[i] = "MaterialFragment";
                    break;
            }
        }

        return TESTPAPER_QUESTIONS;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.removeItem(R.id.lesson_menu_list);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showToolsByAnim() {
        mToolsLayout.setVisibility(View.VISIBLE);
    }

    private void loadTestpaperInfo() {
        RequestUrl requestUrl = app.bindUrl(Const.TESTPAPER_INFO, true);
        requestUrl.setParams(new String[]{
                "testId", mTestId + ""
        });

        mActivity.ajaxPost(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                TestpaperResult testpaperResult = mActivity.parseJsonValue(
                        response, new TypeToken<TestpaperResult>() {
                        });

                if (testpaperResult == null) {
                    return;
                }

                mTestpaper = testpaperResult.testpaper;
                Log.d(null, "mTestpaper->" + mTestpaper);
                initTestPaperItem(testpaperResult);
            }
        }, null);
    }

    private void initTestPaperItem(TestpaperResult testpaperResult) {
        Testpaper testpaper = testpaperResult.testpaper;

        ArrayList<TestpaperItem> contents = new ArrayList<TestpaperItem>();
        contents.add(new TestpaperItem(
                "试卷题目", new String[]{testpaper.name}, false
        ));
        contents.add(new TestpaperItem(
                "试卷说明", new String[]{AppUtil.coverCourseAbout(testpaper.description)}, false
        ));
        contents.add(new TestpaperItem(
                "试卷简介", new String[]{
                getTestpaperInstruction(testpaperResult, testpaper.score),
                String.format("考试时间:%s", testpaper.limitedTime == 0 ? "无限制" : testpaper.limitedTime + "分钟")
        }, true
        ));
        contents.add(new TestpaperItem(
                "考试提醒", new String[]{
                String.format("您即将进行时长为%s的考试，请做好相关准备。",
                        testpaper.limitedTime == 0 ? "无限制" : testpaper.limitedTime + "分钟"),
                "做好相关准备后，点击“进入考试“即可开始考试"
        }, true
        ));

        TestpaperInfoAdapter adapter = new TestpaperInfoAdapter(
                mContext, contents, R.layout.testpaperinfo_item
        );
        mListView.setAdapter(adapter);
    }

    private String getTestpaperInstruction(TestpaperResult testpaperResult, double score) {
        HashMap<QuestionType, Integer> items = testpaperResult.items;
        Testpaper testpaper = testpaperResult.testpaper;
        if (items.isEmpty()) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer("考试题目：");
        String format = "";
        ArrayList<QuestionType> types = testpaper.metas.question_type_seq;
        for (QuestionType type : types) {
            switch (type) {
                case choice:
                    format = "多选题%d道";
                    break;
                case determine:
                    format = "判断题%d道";
                    break;
                case essay:
                    format = "问答题%d道";
                    break;
                case fill:
                    format = "填空题%d道";
                    break;
                case material:
                    format = "材料题%d道";
                    break;
                case single_choice:
                    format = "单选题%d道";
                    break;
                case uncertain_choice:
                    format = "不定选题%d道";
                    break;
            }

            stringBuffer.append(String.format(format, items.get(type)));
            stringBuffer.append(",");
        }


        stringBuffer.append(String.format("总分%s分", new DecimalFormat("#.00").format(score)));
        return stringBuffer.toString();
    }
}
