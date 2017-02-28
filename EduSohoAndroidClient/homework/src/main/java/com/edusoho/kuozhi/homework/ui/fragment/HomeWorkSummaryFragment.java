package com.edusoho.kuozhi.homework.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.homework.ExerciseActivity;
import com.edusoho.kuozhi.homework.HomeworkActivity;
import com.edusoho.kuozhi.homework.HomeworkSummaryActivity;
import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.model.ExerciseModel;
import com.edusoho.kuozhi.homework.model.ExerciseProvider;
import com.edusoho.kuozhi.homework.model.HomeWorkModel;
import com.edusoho.kuozhi.homework.model.HomeworkProvider;
import com.edusoho.kuozhi.homework.util.HomeWorkLearnConfig;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;

import static com.edusoho.kuozhi.homework.util.HomeWorkLearnConfig.saveHomeworkLocalLearnConfig;

/**
 * Created by howzhi on 15/10/20.
 */
public class HomeWorkSummaryFragment extends BaseFragment {

    private int mLessonId;
    private TextView tvCourseTitle;
    private TextView homeworkName;
    private TextView homeworkNameContent;
    private TextView homeworkInfo;
    private TextView homeworkInfoContent;
    private Button startBtn;

    private View mEmptyNotice;
    private View mLoadLayout;
    private HomeworkProvider mHomeworkProvider;
    private ExerciseProvider mExerciseProvider;
    private String mType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.homework_summary_layout);
        ModelProvider.init(getActivity().getBaseContext(), this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mLessonId = getArguments().getInt(Const.LESSON_ID, 0);
        mType = getArguments().getString("type");
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        tvCourseTitle = (TextView) view.findViewById(R.id.homework_belong_content);
        mLoadLayout = view.findViewById(R.id.load_layout);
        homeworkName = (TextView) view.findViewById(R.id.homework_name);
        homeworkNameContent = (TextView) view.findViewById(R.id.homework_name_content);
        homeworkInfo = (TextView) view.findViewById(R.id.homework_info);
        homeworkInfoContent = (TextView) view.findViewById(R.id.homework_info_content);
        mEmptyNotice = view.findViewById(R.id.empty_data_page);
        mEmptyNotice.setVisibility(View.GONE);
        startBtn = (Button) view.findViewById(R.id.start_homework_btn);
        if (HomeworkSummaryActivity.HOMEWORK.equals(mType)) {
            homeworkName.setText("作业名称");
            homeworkInfo.setText("作业说明");
            initHomeworkSummary();
        } else {
            homeworkName.setText("练习名称");
            homeworkInfo.setText("练习说明");
            initExerciseSummary();
        }

    }

    private void initHomeworkSummary() {
        String url = new StringBuilder()
                .append(String.format(Const.HOMEWORK_CONTENT, mLessonId))
                .append("?_idType=lesson")
                .toString();
        RequestUrl requestUrl = app.bindNewUrl(url, true);
        mHomeworkProvider.getHomeWork(requestUrl).success(new NormalCallback<HomeWorkModel>() {
            @Override
            public void success(HomeWorkModel homeWorkModel) {
                mLoadLayout.setVisibility(View.GONE);
                if (homeWorkModel == null){
                    mEmptyNotice.setVisibility(View.VISIBLE);
                    return;
                }
                renderHomeworkView(homeWorkModel);
                HomeWorkLearnConfig.saveHomeworkLocalLearnConfig(mContext, "homework", homeWorkModel.getId(), true);
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                mLoadLayout.setVisibility(View.GONE);
            }
        });
    }

    private void initExerciseSummary(){
        String url = new StringBuilder()
                .append(String.format(Const.EXERCISE_CONTENT, mLessonId))
                .append("?_idType=lesson")
                .toString();
        RequestUrl requestUrl = app.bindNewUrl(url, true);
        mExerciseProvider.getExercise(requestUrl).success(new NormalCallback<ExerciseModel>() {
            @Override
            public void success(ExerciseModel exerciseModel) {
                mLoadLayout.setVisibility(View.GONE);
                if (exerciseModel == null){
                    mEmptyNotice.setVisibility(View.VISIBLE);
                    return;
                }
                renderExerciseView(exerciseModel);
                HomeWorkLearnConfig.saveHomeworkLocalLearnConfig(mContext, "exercise", exerciseModel.getId(), true);
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                mLoadLayout.setVisibility(View.GONE);
            }
        });
    }

    private void renderHomeworkView(final HomeWorkModel homeWorkModel) {
        tvCourseTitle.setText(homeWorkModel.getCourseTitle());
        homeworkNameContent.setText(homeWorkModel.getLessonTitle());
        if (homeWorkModel.getDescription() != null) {
            homeworkInfoContent.setText(AppUtil.coverCourseAbout(homeWorkModel.getDescription()));
        }
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getBaseContext(), HomeworkActivity.class);
                intent.putExtra(HomeworkActivity.HOMEWORK_ID, homeWorkModel.getId());
                intent.putExtra(HomeworkSummaryActivity.TYPE, mType);
                getActivity().startActivityForResult(intent, HomeworkSummaryActivity.REQUEST_DO);
            }
        });
    }

    private void renderExerciseView(final ExerciseModel exerciseModel){
        tvCourseTitle.setText(exerciseModel.getCourseTitle());
        homeworkNameContent.setText(exerciseModel.getLessonTitle());
        if (exerciseModel.getDescription() != null) {
            homeworkInfoContent.setText(AppUtil.coverCourseAbout(exerciseModel.getDescription()));
        }
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getBaseContext(), ExerciseActivity.class);
                intent.putExtra(ExerciseActivity.EXERCISE_ID, exerciseModel.getId());
                intent.putExtra(HomeworkSummaryActivity.TYPE, mType);
                getActivity().startActivityForResult(intent, HomeworkSummaryActivity.REQUEST_DO);
            }
        });

    }
}
