package com.edusoho.kuozhi.homework;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.homework.listener.IHomeworkQuestionResult;
import com.edusoho.kuozhi.homework.model.HomeWorkModel;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.homework.model.HomeworkProvider;
import com.edusoho.kuozhi.homework.ui.fragment.HomeWorkCardFragment;
import com.edusoho.kuozhi.homework.ui.fragment.HomeWorkQuestionFragment;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Melomelon on 2015/10/14.
 */
public class HomeworkActivity extends ActionBarBaseActivity implements IHomeworkQuestionResult, NormalCallback<VolleyError> {

    private static HomeworkActivity homeworkActivity;
    public static final String HOMEWORK_ID = "homeworkId";
    public static final int CHANGE_ANSWER = 0100;
    public static final int SUBMIT_HOMEWORK = 0200;

    public static final int RESULT_DO = 0300;

    protected int mHomeWorkId;
    protected String mType;
    protected int mCurrentQuesitonIndex;
    protected List<HomeWorkQuestion> mHomeWorkQuestionList;
    protected HomeworkProvider mHomeworkProvider;

    private FrameLayout mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initIntentData();
        setBackMode(BACK, HomeworkSummaryActivity.HOMEWORK.equals(mType) ? "作业" : "练习");
        mHomeworkProvider = ModelProvider.initProvider(getBaseContext(), HomeworkProvider.class);
        setContentView(R.layout.homework_activity_layout);
        mLoading = (FrameLayout) findViewById(R.id.load_layout);
        initView();
        app.registMsgSource(this);
    }

    protected Intent initIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            CommonUtil.longToast(
                    getBaseContext(),
                    HomeworkSummaryActivity.HOMEWORK.equals(mType) ? "获取作业数据错误" : "获取练习数据错误"
            );
            throw new RuntimeException("获取数据失败");
        }

        mType = intent.getStringExtra("type");
        mHomeWorkId = intent.getIntExtra(HOMEWORK_ID, 0);
        return intent;
    }

    protected RequestUrl getRequestUrl() {
        return app.bindNewUrl(String.format(Const.HOMEWORK_CONTENT, mHomeWorkId), true);
    }

    protected void initView() {
        RequestUrl requestUrl = getRequestUrl();
        mLoading.setVisibility(View.VISIBLE);
        mHomeworkProvider.getHomeWork(requestUrl).success(new NormalCallback<HomeWorkModel>() {
            @Override
            public void success(HomeWorkModel homeWorkModel) {
                coverQuestionList(homeWorkModel);
                Bundle bundle = new Bundle();
                bundle.putString(Const.ACTIONBAR_TITLE, HomeworkSummaryActivity.HOMEWORK.equals(mType) ? "作业题目" : "练习题目");
                loadFragment(bundle);
                mLoading.setVisibility(View.GONE);
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                mLoading.setVisibility(View.GONE);
            }
        });
    }

    private void coverQuestionList(HomeWorkModel homeWorkModel) {
        mHomeWorkQuestionList = new ArrayList<HomeWorkQuestion>();
        for (HomeWorkQuestion question : homeWorkModel.getItems()) {
            QuestionType type = QuestionType.value(question.getType());
            if (QuestionType.material == type) {
                List<HomeWorkQuestion> items = question.getItems();
                for (HomeWorkQuestion itemQuestion : items) {
                    itemQuestion.setParent(question);
                    mHomeWorkQuestionList.add(itemQuestion);
                }
                continue;
            }
            mHomeWorkQuestionList.add(question);
        }
    }

    @Override
    public List<HomeWorkQuestion> getQuestionList() {
        return mHomeWorkQuestionList;
    }

    protected void loadFragment(Bundle bundle) {
        try {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            Fragment fragment = Fragment.instantiate(getBaseContext(), "com.edusoho.kuozhi.homework.ui.fragment.HomeWorkQuestionFragment");
            bundle.putString(HomeworkSummaryActivity.TYPE,mType);
            fragment.setArguments(bundle);
            fragmentTransaction.replace(android.R.id.content, fragment);
            fragmentTransaction.commit();
        } catch (Exception ex) {
            Log.d("HomeworkActivity", ex.toString());
        }
    }

    private void submitHomework() {
        final RequestUrl requestUrl = app.bindNewUrl(
                String.format(Const.HOMEWORK_RESULT, mHomeWorkId), true);
        IdentityHashMap<String, String> params = requestUrl.initKeysMap();

        for (HomeWorkQuestion question : mHomeWorkQuestionList) {
            List<String> answers = question.getAnswer();
            if (answers == null) {
                continue;
            }
            for (String answer : answers) {
                params.put(String.format("data[%d][]", question.getId()), answer);
            }
        }

        mHomeworkProvider.postHomeWorkResult(requestUrl)
                .success(new NormalCallback<LinkedHashMap<String, String>>() {
                    @Override
                    public void success(LinkedHashMap<String, String> result) {
                        if (result == null || "".equals(result.get("id"))) {
                            CommonUtil.longToast(getBaseContext(), "服务器忙，提交失败,请重新提交!");
                            return;
                        }

                        setResult(RESULT_DO);
                        finish();
                    }
                }).fail(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (getSupportFragmentManager().getFragments().size() > 0) {
            getSupportFragmentManager().getFragments().get(0).onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homework_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.homework_menu_card) {
            showHomeWorkCard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static HomeworkActivity getInstance() {
        return homeworkActivity;
    }

    protected void showHomeWorkCard() {
        HomeWorkCardFragment cardFragment = new HomeWorkCardFragment();
        cardFragment.setTitle("答题卡");
        Bundle args = new Bundle();
        args.putString("type",mType);
        cardFragment.setArguments(args);
        cardFragment.show(mFragmentManager, "cardDialog");
    }

    @Override
    public void setCurrentQuestionIndex(int index) {
        this.mCurrentQuesitonIndex = index;
        app.sendMsgToTarget(HomeWorkQuestionFragment.SELECT_QUESTION, null, HomeWorkQuestionFragment.class);
    }

    @Override
    public int getCurrentQuestionIndex() {
        return mCurrentQuesitonIndex;
    }

    @Override
    public void invoke(WidgetMessage message) {
        super.invoke(message);
        int code = message.type.code;
        switch (code) {
            case CHANGE_ANSWER:
                Bundle bundle = message.data;
                int index = bundle.getInt("index", 0);
                ArrayList<String> data = bundle.getStringArrayList("data");
                HomeWorkQuestion question = mHomeWorkQuestionList.get(index);
                question.setAnswer(data);
                break;
            case SUBMIT_HOMEWORK:
                submitHomework();
                break;
        }
    }

    @Override
    public MessageType[] getMsgTypes() {
        String source = getClass().getSimpleName();
        return new MessageType[]{
                new MessageType(CHANGE_ANSWER, source),
                new MessageType(SUBMIT_HOMEWORK, source)
        };
    }

    @Override
    public void success(VolleyError volleyError) {
        CommonUtil.longToast(getBaseContext(), "服务器忙，提交失败,请重新提交!");
    }

    @Override
    public String getType() {
        return "homework";
    }
}
