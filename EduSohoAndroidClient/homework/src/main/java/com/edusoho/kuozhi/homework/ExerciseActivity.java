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
import com.edusoho.kuozhi.homework.model.ExerciseModel;
import com.edusoho.kuozhi.homework.model.ExerciseProvider;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Melomelon on 2015/10/26.
 */
public class ExerciseActivity extends ActionBarBaseActivity implements IHomeworkQuestionResult, NormalCallback<VolleyError> {

    public static final String EXERCISE_ID = "exerciseId";
    public static final int CHANGE_ANSWER = 0100;
    public static final int SUBMIT_EXERCISE = 0200;

    public static final int RESULT_DO = 0300;

    protected int mExerciseId;
    protected String mType;
    protected int mCurrentQuesitonIndex;
    protected List<HomeWorkQuestion> mExerciseQuestionList;
    protected ExerciseProvider mExerciseProvider;

    private FrameLayout mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_activity_layout);
        mLoading = (FrameLayout) findViewById(R.id.load_layout);
        initIntentData();
        mExerciseProvider = ModelProvider.initProvider(getBaseContext(), ExerciseProvider.class);
        setBackMode(BACK, "练习");
        initView();
        app.registMsgSource(this);
    }

    protected Intent initIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            CommonUtil.longToast(
                    getBaseContext(),
                    "获取练习数据错误"
            );
            throw new RuntimeException("获取数据失败");
        }

        mType = intent.getStringExtra("type");
        mExerciseId = intent.getIntExtra(EXERCISE_ID, 0);
        return intent;
    }

    protected void initView() {
        RequestUrl requestUrl = getRequestUrl();
        mLoading.setVisibility(View.VISIBLE);
        mExerciseProvider.getExercise(requestUrl).success(new NormalCallback<ExerciseModel>() {
            @Override
            public void success(ExerciseModel exerciseModel) {
                coverQuestionList(exerciseModel);
                Bundle bundle = new Bundle();
                bundle.putString(Const.ACTIONBAR_TITLE, "练习题目");
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

    private void coverQuestionList(ExerciseModel exerciseModel) {
        mExerciseQuestionList = new ArrayList<HomeWorkQuestion>();
        for (HomeWorkQuestion question : exerciseModel.getItems()) {
            QuestionType type = QuestionType.value(question.getType());
            if (QuestionType.material == type) {
                List<HomeWorkQuestion> items = question.getItems();
                for (HomeWorkQuestion itemQuestion : items) {
                    itemQuestion.setParent(question);
                    mExerciseQuestionList.add(itemQuestion);
                }
                continue;
            }
            mExerciseQuestionList.add(question);
        }
    }

    protected void loadFragment(Bundle bundle) {
        try {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            Fragment fragment = Fragment.instantiate(getBaseContext(), "com.edusoho.kuozhi.homework.ui.fragment.HomeWorkQuestionFragment");
            bundle.putString(HomeworkSummaryActivity.TYPE, mType);
            fragment.setArguments(bundle);
            fragmentTransaction.replace(android.R.id.content, fragment);
            fragmentTransaction.commit();
        } catch (Exception ex) {
            Log.d("ExerciseActivity", ex.toString());
        }
    }

    private void submitExercise() {
        final RequestUrl requestUrl = app.bindNewUrl(
                String.format(Const.EXERCISE_RESULT, mExerciseId), true);
        IdentityHashMap<String, String> params = requestUrl.initKeysMap();

        for (HomeWorkQuestion question : mExerciseQuestionList) {
            List<String> answers = question.getAnswer();
            if (answers == null) {
                continue;
            }
            params.put(String.format("data[%d][questionId]", question.getId()), question.getId() + "");
            for (String answer : answers) {
                params.put(String.format("data[%d][answer][]", question.getId()), answer);
            }
        }

        mExerciseProvider.postExerciseResult(requestUrl)
                .success(new NormalCallback<LinkedHashMap<String, String>>() {
                    @Override
                    public void success(LinkedHashMap<String, String> result) {
                        if (result == null || "".equals(result.get("id"))) {
                            CommonUtil.longToast(getBaseContext(), "服务器忙，提交失败,请重新提交!");
                            return;
                        }
                        Intent intent = new Intent();
                        intent.putExtra(EXERCISE_ID, mExerciseId);
                        setResult(RESULT_DO, intent);
                        finish();
                    }
                }).fail(this);
    }

    protected RequestUrl getRequestUrl() {
        return app.bindNewUrl(String.format(Const.EXERCISE_CONTENT, mExerciseId), true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (getSupportFragmentManager().getFragments().size() > 0) {
            getSupportFragmentManager().getFragments().get(0).onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                HomeWorkQuestion question = mExerciseQuestionList.get(index);
                question.setAnswer(data);
                break;
            case SUBMIT_EXERCISE:
                submitExercise();
                break;
        }
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

    protected void showHomeWorkCard() {
        HomeWorkCardFragment cardFragment = new HomeWorkCardFragment();
        cardFragment.setTitle("答题卡");
        Bundle args = new Bundle();
        args.putString("type", mType);
        cardFragment.setArguments(args);
        cardFragment.show(mFragmentManager, "cardDialog");
    }

    @Override
    public List<HomeWorkQuestion> getQuestionList() {
        return mExerciseQuestionList;
    }

    @Override
    public int getCurrentQuestionIndex() {
        return mCurrentQuesitonIndex;
    }

    @Override
    public void setCurrentQuestionIndex(int index) {
        this.mCurrentQuesitonIndex = index;
        app.sendMsgToTarget(HomeWorkQuestionFragment.SELECT_QUESTION, null, HomeWorkQuestionFragment.class);

    }

    @Override
    public MessageType[] getMsgTypes() {
        String source = getClass().getSimpleName();
        return new MessageType[]{
                new MessageType(CHANGE_ANSWER, source),
                new MessageType(SUBMIT_EXERCISE, source)
        };
    }

    @Override
    public void success(VolleyError obj) {
        CommonUtil.longToast(getBaseContext(), "服务器忙，提交失败,请重新提交!");
    }

    public void setProvider(ExerciseProvider provider) {
        mExerciseProvider = provider;
    }

    @Override
    public String getType() {
        return "exercise";
    }
}
