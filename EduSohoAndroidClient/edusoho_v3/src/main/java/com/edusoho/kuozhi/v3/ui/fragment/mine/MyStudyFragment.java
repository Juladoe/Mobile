package com.edusoho.kuozhi.v3.ui.fragment.mine;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.MyClassroomAdapter;
import com.edusoho.kuozhi.v3.adapter.MyCourseStudyAdapter;
import com.edusoho.kuozhi.v3.entity.course.CourseProgress;
import com.edusoho.kuozhi.v3.entity.course.LearningClassroom;
import com.edusoho.kuozhi.v3.entity.course.LearningCourse;
import com.edusoho.kuozhi.v3.entity.course.Study;
import com.edusoho.kuozhi.v3.entity.lesson.Lesson;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailModel;
import com.edusoho.kuozhi.v3.model.bal.course.CourseResult;
import com.edusoho.kuozhi.v3.model.provider.CourseProvider;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.view.EduSohoNewIconView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by JesseHuang on 2017/2/7.
 */

public class MyStudyFragment extends BaseFragment implements MineFragment.RefreshFragment {

    public static final int LATEST_COURSE = 1;
    public static final int NORMAL_COURSE = 2;
    public static final int LIVE_COURSE = 3;
    public static final int CLASSROOM = 4;

    private int mCurrent_TYPE = LATEST_COURSE;

    private SwipeRefreshLayout srlContent;
    private RecyclerView rvContent;
    private View rlayoutFilterType;
    private View llayoutFilterQuestionTypeList;
    private View viewCoverScreen;
    private TextView tvFilterName;
    private EduSohoNewIconView esivFilterArrow;

    private TextView tvLatestCourse;
    private TextView tvNormalCourse;
    private TextView tvLiveCourse;
    private TextView tvClassroom;

    private MyCourseStudyAdapter mCourseAdapter;
    private MyClassroomAdapter mClassroomAdapter;
    private CourseProvider mCourseProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.fragment_my_study);
    }

    @Override
    protected void initView(View view) {
        srlContent = (SwipeRefreshLayout) view.findViewById(R.id.srl_content);
        srlContent.setColorSchemeResources(R.color.primary_color);

        rvContent = (RecyclerView) view.findViewById(R.id.rv_content);
        rvContent.setLayoutManager(new LinearLayoutManager(getActivity()));

        rlayoutFilterType = view.findViewById(R.id.rlayout_filter_type);
        rlayoutFilterType.setVisibility(View.VISIBLE);
        rlayoutFilterType.setOnClickListener(getShowTypeLayoutClickListener());

        llayoutFilterQuestionTypeList = view.findViewById(R.id.llayout_filter_question_type_list);
        llayoutFilterQuestionTypeList.setVisibility(View.GONE);
        llayoutFilterQuestionTypeList.bringToFront();

        viewCoverScreen = view.findViewById(R.id.view_cover_screen);
        viewCoverScreen.setOnClickListener(getCoverScreenClickListener());

        tvFilterName = (TextView) view.findViewById(R.id.tv_filter_name);

        view.findViewById(R.id.v_breakline).bringToFront();

        esivFilterArrow = (EduSohoNewIconView) view.findViewById(R.id.tv_filter_arrow);

        tvLatestCourse = (TextView) view.findViewById(R.id.tv_latest_course);
        tvNormalCourse = (TextView) view.findViewById(R.id.tv_normal_course);
        tvLiveCourse = (TextView) view.findViewById(R.id.tv_live_course);
        tvClassroom = (TextView) view.findViewById(R.id.tv_classroom);
        tvLatestCourse.setOnClickListener(getTypeClickListener());
        tvNormalCourse.setOnClickListener(getTypeClickListener());
        tvLiveCourse.setOnClickListener(getTypeClickListener());
        tvClassroom.setOnClickListener(getTypeClickListener());
        initData();
        loadData();
        srlContent.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
    }

    private void initData() {
        mCourseProvider = new CourseProvider(mContext);
        mCourseAdapter = new MyCourseStudyAdapter(getActivity());
        mClassroomAdapter = new MyClassroomAdapter(getActivity());
        rvContent.setAdapter(mCourseAdapter);
    }

    private void loadData() {
        switchType(mCurrent_TYPE);
    }

    /**
     * 筛选显示数据类型事件
     *
     * @param type
     */
    private void switchType(int type) {
        tvLatestCourse.setTextColor(getResources().getColor(R.color.primary_font_color));
        tvNormalCourse.setTextColor(getResources().getColor(R.color.primary_font_color));
        tvLiveCourse.setTextColor(getResources().getColor(R.color.primary_font_color));
        tvClassroom.setTextColor(getResources().getColor(R.color.primary_font_color));
        switch (type) {
            case LATEST_COURSE:
                loadLatestCourse();
                tvLatestCourse.setTextColor(getResources().getColor(R.color.primary_color));
                tvFilterName.setText(getString(R.string.filter_type_latest));
                break;
            case NORMAL_COURSE:
                loadNormalCourse();
                tvNormalCourse.setTextColor(getResources().getColor(R.color.primary_color));
                tvFilterName.setText(getString(R.string.filter_type_course));
                break;
            case LIVE_COURSE:
                loadLiveCourse();
                tvLiveCourse.setTextColor(getResources().getColor(R.color.primary_color));
                tvFilterName.setText(getString(R.string.filter_type_live));
                break;
            case CLASSROOM:
                loadClassroom();
                tvClassroom.setTextColor(getResources().getColor(R.color.primary_color));
                tvFilterName.setText(getString(R.string.filter_type_classroom));
                break;
        }
        llayoutFilterQuestionTypeList.setVisibility(View.GONE);
        esivFilterArrow.setText(getString(R.string.new_font_unfold));
        mCurrent_TYPE = type;
    }

    private void loadLatestCourse() {
        showLoadingView();
        CourseDetailModel.getStudy(new ResponseCallbackListener<Study>() {
            @Override
            public void onSuccess(Study data) {
                disabledLoadingView();
                mCourseAdapter.setLatestCourses(data.getResources());
                rvContent.setAdapter(mCourseAdapter);
                List<Integer> ids = new ArrayList<>();
                for (Study.Resource study : data.getResources()) {
                    ids.add(study.getId());
                }
                if (ids.size() != 0) {
                    getCourseProgresses(ids, LATEST_COURSE);
                }
                getLiveLesson(LATEST_COURSE);
            }

            @Override
            public void onFailure(String code, String message) {
                disabledLoadingView();
            }
        });
    }

    private void loadNormalCourse() {
        showLoadingView();
        mCourseProvider.getLearnCourses().success(new NormalCallback<CourseResult>() {
            @Override
            public void success(CourseResult courseResult) {
                disabledLoadingView();
                List<Course> list = new LinkedList<>(Arrays.asList(courseResult.resources));
                mCourseAdapter.setNormalCourses(list);
                rvContent.setAdapter(mCourseAdapter);
                List<Integer> ids = new ArrayList<>();
                for (Course course : courseResult.resources) {
                    ids.add(course.id);
                }
                if (ids.size() != 0) {
                    getCourseProgresses(ids, NORMAL_COURSE);
                }
                getLiveLesson(NORMAL_COURSE);
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError error) {
                disabledLoadingView();
            }
        });
    }

    private void loadLiveCourse() {
        showLoadingView();
        CourseDetailModel.getLiveCourses(1000, 0, new ResponseCallbackListener<LearningCourse>() {
            @Override
            public void onSuccess(LearningCourse liveCourses) {
                disabledLoadingView();
                mCourseAdapter.setLiveCourses(liveCourses.data);
                rvContent.setAdapter(mCourseAdapter);
                List<Integer> ids = new ArrayList<>();
                for (Course course : liveCourses.data) {
                    ids.add(course.id);
                }
                if (ids.size() != 0) {
                    getCourseProgresses(ids, LIVE_COURSE);
                }
                getLiveLesson(LIVE_COURSE);
            }

            @Override
            public void onFailure(String code, String message) {
                disabledLoadingView();
            }
        });
    }

    private void loadClassroom() {
        showLoadingView();
        CourseDetailModel.getAllUserClassroom(1000, 0, new ResponseCallbackListener<LearningClassroom>() {
            @Override
            public void onSuccess(LearningClassroom data) {
                disabledLoadingView();
                mClassroomAdapter.setClassrooms(data.getData());
                rvContent.setAdapter(mClassroomAdapter);
            }

            @Override
            public void onFailure(String code, String message) {
                disabledLoadingView();
            }
        });
    }

    private void getCourseProgresses(List<Integer> ids, final int type) {
        CourseDetailModel.getCourseProgress(ids, new ResponseCallbackListener<CourseProgress>() {
            @Override
            public void onSuccess(CourseProgress data) {
                MyCourseStudyAdapter myCourseStudyAdapter = (MyCourseStudyAdapter) rvContent.getAdapter();
                switch (type) {
                    case LATEST_COURSE:
                        List<Study.Resource> latestCourses = myCourseStudyAdapter.getLatestCourses();
                        int latestCourseSize = data.resources.size();
                        for (int i = 0; i < latestCourseSize; i++) {
                            CourseProgress.Progress progress = data.resources.get(i);
                            for (int j = 0; j < latestCourses.size(); j++) {
                                if (progress.courseId == latestCourses.get(j).getId()) {
                                    latestCourses.get(j).setLearnedNum(progress.learnedNum);
                                    latestCourses.get(j).setTotalLesson(progress.totalLesson);
                                    break;
                                }
                            }
                        }
                        break;
                    case NORMAL_COURSE:
                        List<Course> normalCourses = myCourseStudyAdapter.getNormalCourses();
                        int normalCourseSize = data.resources.size();
                        for (int i = 0; i < normalCourseSize; i++) {
                            CourseProgress.Progress progress = data.resources.get(i);
                            for (int j = 0; j < normalCourses.size(); j++) {
                                if (progress.courseId == normalCourses.get(j).id) {
                                    normalCourses.get(j).learnedNum = progress.learnedNum;
                                    normalCourses.get(j).totalLesson = progress.totalLesson;
                                    break;
                                }
                            }
                        }
                        break;
                    case LIVE_COURSE:
                        List<Course> liveCourses = myCourseStudyAdapter.getLiveCourses();
                        int liveCoursesSize = data.resources.size();
                        for (int i = 0; i < liveCoursesSize; i++) {
                            CourseProgress.Progress progress = data.resources.get(i);
                            for (int j = 0; j < liveCourses.size(); j++) {
                                if (progress.courseId == liveCourses.get(j).id) {
                                    liveCourses.get(j).learnedNum = progress.learnedNum;
                                    liveCourses.get(j).totalLesson = progress.totalLesson;
                                    break;
                                }
                            }
                        }
                        break;
                }
                rvContent.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onFailure(String code, String message) {
            }
        });
    }

    private void getLiveLesson(int type) {
        final MyCourseStudyAdapter myCourseStudyAdapter = (MyCourseStudyAdapter) rvContent.getAdapter();
        switch (type) {
            case LATEST_COURSE:
                List<Study.Resource> latestCourses = myCourseStudyAdapter.getLatestCourses();
                final int latestCourseSize = latestCourses.size();
                for (int i = 0; i < latestCourseSize; i++) {
                    final Study.Resource study = latestCourses.get(i);
                    final int finalI = i;
                    CourseDetailModel.getLiveLesson(study.getId(), new NormalCallback<List<Lesson>>() {
                        @Override
                        public void success(List<Lesson> lessons) {
                            disabledLoadingView();
                            if (lessons != null) {
                                for (Lesson lesson : lessons) {
                                    long currentTime = System.currentTimeMillis();
                                    if (lesson.startTime * 1000 < currentTime && lesson.endTime * 1000 > currentTime) {
                                        study.liveState = 1;
                                        break;
                                    }
                                }
                                if (finalI == latestCourseSize - 1) {
                                    myCourseStudyAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                }
                break;
            case NORMAL_COURSE:
                List<Course> normalCourses = myCourseStudyAdapter.getNormalCourses();
                final int normalCoursesSize = normalCourses.size();
                for (int i = 0; i < normalCoursesSize; i++) {
                    final Course course = normalCourses.get(i);
                    final int finalI = i;
                    CourseDetailModel.getLiveLesson(course.id, new NormalCallback<List<Lesson>>() {
                        @Override
                        public void success(List<Lesson> lessons) {
                            disabledLoadingView();
                            if (lessons != null) {
                                for (Lesson lesson : lessons) {
                                    long currentTime = System.currentTimeMillis();
                                    if (lesson.startTime * 1000 < currentTime && lesson.endTime * 1000 > currentTime) {
                                        course.liveState = 1;
                                        myCourseStudyAdapter.notifyDataSetChanged();
                                        break;
                                    }
                                }
                                if (finalI == normalCoursesSize - 1) {
                                    myCourseStudyAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                }
                break;
            case LIVE_COURSE:
                List<Course> liveCourses = myCourseStudyAdapter.getLiveCourses();
                final int liveCoursesSize = liveCourses.size();
                for (int i = 0; i < liveCoursesSize; i++) {
                    final Course course = liveCourses.get(i);
                    final int finalI = i;
                    CourseDetailModel.getLiveLesson(course.id, new NormalCallback<List<Lesson>>() {
                        @Override
                        public void success(List<Lesson> lessons) {
                            disabledLoadingView();
                            if (lessons != null) {
                                for (Lesson lesson : lessons) {
                                    long currentTime = System.currentTimeMillis();
                                    if (lesson.startTime * 1000 < currentTime && lesson.endTime * 1000 > currentTime) {
                                        course.liveState = 1;
                                        myCourseStudyAdapter.notifyDataSetChanged();
                                        break;
                                    }
                                }
                                if (finalI == liveCoursesSize - 1) {
                                    myCourseStudyAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                }
                break;
        }
    }

    private View.OnClickListener getTypeClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.tv_latest_course) {
                    switchType(LATEST_COURSE);
                } else if (v.getId() == R.id.tv_normal_course) {
                    switchType(NORMAL_COURSE);
                } else if (v.getId() == R.id.tv_live_course) {
                    switchType(LIVE_COURSE);
                } else {
                    switchType(CLASSROOM);
                }
            }
        };
    }

    private View.OnClickListener getShowTypeLayoutClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llayoutFilterQuestionTypeList.getVisibility() == View.VISIBLE) {
                    llayoutFilterQuestionTypeList.setVisibility(View.GONE);
                    esivFilterArrow.setText(getString(R.string.new_font_unfold));
                } else {
                    llayoutFilterQuestionTypeList.setVisibility(View.VISIBLE);
                    esivFilterArrow.setText(getString(R.string.new_font_fold));
                }
            }
        };
    }

    private View.OnClickListener getCoverScreenClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llayoutFilterQuestionTypeList.setVisibility(View.GONE);
                esivFilterArrow.setText(getString(R.string.new_font_unfold));
            }
        };
    }

    @Override
    public void refreshData() {
        loadData();
    }

    @Override
    public void setSwipeEnabled(int i) {
        srlContent.setEnabled(i == 0);
    }

    private void showLoadingView() {
        srlContent.post(new Runnable() {
            @Override
            public void run() {
                srlContent.setRefreshing(true);
            }
        });
    }

    private void disabledLoadingView() {
        srlContent.setRefreshing(false);
    }

    public static class CourseStudyViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivPic;
        public View layoutLive;
        public TextView tvLiveIcon;
        public TextView tvLive;
        public TextView tvTitle;
        public TextView tvStudyState;
        public TextView tvMore;
        public View layoutClass;
        public TextView tvClassName;
        public View rLayoutItem;

        public CourseStudyViewHolder(View view) {
            super(view);
            ivPic = (ImageView) view.findViewById(R.id.iv_pic);
            layoutLive = view.findViewById(R.id.layout_live);
            tvLiveIcon = (TextView) view.findViewById(R.id.tv_live_icon);
            tvLive = (TextView) view.findViewById(R.id.tv_live);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvStudyState = (TextView) view.findViewById(R.id.tv_study_state);
            tvMore = (TextView) view.findViewById(R.id.tv_more);
            layoutClass = view.findViewById(R.id.layout_class);
            tvClassName = (TextView) view.findViewById(R.id.tv_class_name);
            rLayoutItem = view.findViewById(R.id.rlayout_item);
        }
    }

    public static class ClassroomViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivPic;
        public TextView tvTitle;
        public TextView tvMore;
        public View rLayoutItem;

        public ClassroomViewHolder(View view) {
            super(view);
            ivPic = (ImageView) view.findViewById(R.id.iv_pic);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvMore = (TextView) view.findViewById(R.id.tv_more);
            rLayoutItem = view.findViewById(R.id.rlayout_item);
        }
    }
}
