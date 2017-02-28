package com.edusoho.kuozhi.homework;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.homework.model.HomeWorkModel;
import com.edusoho.kuozhi.homework.model.HomeWorkResult;
import com.edusoho.kuozhi.homework.model.HomeworkProvider;
import com.edusoho.kuozhi.homework.util.HomeWorkLearnConfig;
import com.edusoho.kuozhi.v3.entity.lesson.PluginViewItem;
import com.edusoho.kuozhi.v3.listener.BaseLessonPluginCallback;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.ActivityUtil;
import com.edusoho.kuozhi.v3.util.ApiTokenUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.ToastUtil;

/**
 * Created by Melomelon on 2015/10/13.
 */
public class HomeworkSummaryActivity extends ActionBarBaseActivity {

    public static final String HOMEWORK = "homework";
    public static final String EXERCISE = "exercise";
    public static final String TYPE = "type";
    public static final int REQUEST_DO = 0010;

    private int mLessonId;
    private String mType;

    private Bundle mBundle;
    private HomeworkProvider mHomeworkProvider;

    private FrameLayout mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mBundle = intent.getExtras();

        mLessonId = mBundle == null ? 0 : mBundle.getInt(Const.LESSON_ID);
        mType = HOMEWORK;
        setContentView(R.layout.homework_summary_layout);
        mLoading = (FrameLayout) findViewById(R.id.load_layout);
        ModelProvider.init(getBaseContext(), this);
        initView();
    }

    public String getType() {
        return mType;
    }

    private void renderHomeworkView(final HomeWorkResult homeWorkResult) {
        String fragmentName = null;
        if (homeWorkResult == null || "doing".equals(homeWorkResult.status)) {
            fragmentName = "com.edusoho.kuozhi.homework.ui.fragment.HomeWorkSummaryFragment";
        } else {
            fragmentName = "com.edusoho.kuozhi.homework.ui.fragment.HomeWorkResultFragment";
        }

        Bundle bundle = getIntent().getExtras();
        bundle.putString("type", mType);
        loadFragment(bundle, fragmentName);
    }

    protected void loadFragment(Bundle bundle, String fragmentName) {
        try {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            Fragment fragment = Fragment.instantiate(getBaseContext(), fragmentName);
            if (bundle.getString(TYPE) == null) {
                bundle.putString(TYPE, "homework");
            }
            fragment.setArguments(bundle);
            fragmentTransaction.replace(android.R.id.content, fragment);
            fragmentTransaction.commit();
        } catch (Exception ex) {
            Log.d("HomeworkSummaryActivity", ex.toString());
        }
    }

    protected void initView() {
        setBackMode(BACK, "作业");
        loadHomeWork();
    }

    private void loadHomeWork() {
        String url = String.format(Const.HOMEWORK_RESULT, mLessonId);
        RequestUrl requestUrl = app.bindNewUrl(url, true);
        mLoading.setVisibility(View.VISIBLE);
        mHomeworkProvider.getHomeWorkResult(requestUrl, false).success(new NormalCallback<HomeWorkResult>() {
            @Override
            public void success(HomeWorkResult homeWorkModel) {
                mLoading.setVisibility(View.GONE);
                renderHomeworkView(homeWorkModel);
                if (homeWorkModel != null) {
                    HomeWorkLearnConfig.saveHomeworkLocalLearnConfig(mContext, "homework", homeWorkModel.homeworkId, true);
                }
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                mLoading.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DO && resultCode == HomeworkActivity.RESULT_DO) {
            loadHomeWork();
        }
    }

    public static class Callback extends BaseLessonPluginCallback {

        private int mHomeworkId;

        public Callback(Context context) {
            super(context);
        }

        @Override
        protected RequestUrl getRequestUrl(int lessonId) {
            String url = new StringBuilder()
                    .append(String.format(Const.HOMEWORK_CONTENT, lessonId))
                    .append("?_idType=lesson")
                    .toString();
            return ApiTokenUtil.bindNewUrl(mContext, url, true);
        }

        @Override
        public boolean click(View view) {
            if (super.click(view)) {
                return true;
            }
            Toast.makeText(mContext, "课程暂无作业", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void initState(PluginViewItem item) {
            super.initState(item);
            boolean isLearn = HomeWorkLearnConfig.getHomeworkLocalLearnConfig(mContext, "homework", mHomeworkId);
            setViewLearnState(isLearn);
        }

        @Override
        protected void loadPlugin(Bundle bundle) {
            int lessonId = bundle.getInt(Const.LESSON_ID, 0);
            RequestUrl requestUrl = getRequestUrl(lessonId);
            HomeworkProvider provider = ModelProvider.initProvider(mContext, HomeworkProvider.class);
            provider.getHomeWork(requestUrl).success(new NormalCallback<HomeWorkModel>() {
                @Override
                public void success(HomeWorkModel homeWorkModel) {
                    if (homeWorkModel == null || homeWorkModel.getId() == 0) {
                        setViewStatus(PluginViewItem.UNENABLE);
                        return;
                    }

                    mHomeworkId = homeWorkModel.getId();
                    boolean isLearn = HomeWorkLearnConfig.getHomeworkLocalLearnConfig(mContext, "homework", homeWorkModel.getId());
                    setViewStatus(isLearn ? PluginViewItem.ENABLE : PluginViewItem.NEW);
                }
            }).fail(this);
        }
    }
}
