package com.edusoho.kuozhi.v3.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.CourseCatalogueAdapter;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.entity.CourseSetting;
import com.edusoho.kuozhi.v3.entity.lesson.CourseCatalogue;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.handler.CourseStateCallback;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.provider.CourseProvider;
import com.edusoho.kuozhi.v3.model.provider.LessonProvider;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.CourseActivity;
import com.edusoho.kuozhi.v3.ui.LessonActivity;
import com.edusoho.kuozhi.v3.ui.LessonDownloadingActivity;
import com.edusoho.kuozhi.v3.ui.course.ICourseStateListener;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by DF on 2016/12/13.
 */
public class CourseCatalogFragment extends Fragment implements ICourseStateListener {

    private static final int ISMEMBER = 1;
    private static final int VISITOR = 2;

    public boolean isJoin;
    public int mMemberStatus;
    public int mCourseId;
    public CourseCatalogueAdapter mAdapter;
    private RelativeLayout mRlSpace;
    private RecyclerView mLvCatalog;
    private CourseCatalogue mCourseCatalogue;
    private TextView tvSpace;
    private View mLoadView;
    private View mLessonEmpytView;
    private LoadDialog mProcessDialog;
    private List<CourseCatalogue.LessonsBean> lessonsBeanList;
    private CourseCatalogue.LessonsBean lessonsBean;
    private CourseStateCallback mCourseStateCallback;

    public CourseCatalogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCourseId = getArguments().getInt(Const.COURSE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_course_catalog, null);
        initView(contentView);
        ViewGroup parent = (ViewGroup) contentView.getParent();
        if (parent != null) {
            parent.removeView(contentView);
        }
        return contentView;
    }

    private void initView(View view) {
        mRlSpace = (RelativeLayout) view.findViewById(R.id.rl_space);
        mLvCatalog = (RecyclerView) view.findViewById(R.id.lv_catalog);
        mLoadView = view.findViewById(R.id.ll_frame_load);
        tvSpace = (TextView) view.findViewById(R.id.tv_space);
        mLessonEmpytView = view.findViewById(R.id.ll_course_catalog_empty);
        tvSpace.setOnClickListener(getCacheCourse());
        tvSpace.setText(getString(R.string.course_catalog_space) + getRomAvailableSize());
        view.findViewById(R.id.tv_course).setOnClickListener(getCacheCourse());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLessonStatuses();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCourseStateCallback = (CourseStateCallback) activity;
    }

    protected void setLoadViewStatus(int visibility) {
        if (mLoadView != null) {
            mLoadView.setVisibility(visibility);
        }
    }

    private void initCatalogue() {
        User user = getAppSettingProvider().getCurrentUser();
        mRlSpace.setVisibility(mMemberStatus == ISMEMBER && user != null ? View.VISIBLE : View.GONE);
        setLoadViewStatus(View.VISIBLE);
        setLessonEmptyViewVisibility(View.GONE);

        new LessonProvider(getActivity()).getCourseLessons(mCourseId)
        .success(new NormalCallback<CourseCatalogue>() {
            @Override
            public void success(CourseCatalogue courseCatalogue) {
                if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
                    Log.d("CourseCatalogFragment", "activity is finish");
                    return;
                }
                mCourseCatalogue = courseCatalogue;
                mAdapter = new CourseCatalogueAdapter(getActivity());
                mLvCatalog.setLayoutManager(new LinearLayoutManager(getContext()));
                mLvCatalog.setAdapter(mAdapter);
                if (mCourseCatalogue.getLessons().size() != 0) {
                    initFirstLearnLesson();
                    initCustomChapterSetting();
                } else {
                    setLessonEmptyViewVisibility(View.VISIBLE);
                    setLoadViewStatus(View.GONE);
                }
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                setLoadViewStatus(View.GONE);
            }
        });
    }

    private void initCustomChapterSetting() {
        new CourseProvider(getContext()).getSetting()
        .success(new NormalCallback<CourseSetting>() {
            @Override
            public void success(CourseSetting courseSetting) {
                if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
                    Log.d("CourseCatalogFragment", "activity is finish");
                    return;
                }
                setLoadViewStatus(View.GONE);
                if (courseSetting != null && "1".equals(courseSetting.getCustomChapterEnable())) {
                    initLessonCatalog(courseSetting.getChapterName(), courseSetting.getPartName());
                }else {
                    initLessonCatalog(null, null);
                }
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                setLoadViewStatus(View.GONE);
                initLessonCatalog(null, null);
            }
        });
    }

    private void setLessonEmptyViewVisibility(int visibility) {
        mLessonEmpytView.setVisibility(visibility);
    }

    private void updateLessonStatuses() {
        new LessonProvider(getContext()).getCourseLessonLearnStatus(mCourseId)
        .success(new NormalCallback<Map<String, String>>() {
            @Override
            public void success(Map<String, String> learnes) {
                if (mAdapter != null) {
                    mAdapter.setLearnStatuses(learnes);
                }
            }
        });
    }

    public void initLessonCatalog(String chapter, String unit) {
        hideProcesDialog();
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        mAdapter.setData(mCourseCatalogue, isJoin, chapter, unit);
        reFreshColor();
        mAdapter.setOnItemClickListener(new CourseCatalogueAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, CourseCatalogue.LessonsBean lessonsBean) {
                if (mCourseStateCallback.isExpired()) {
                    mCourseStateCallback.handlerCourseExpired();
                    return;
                }
                if ("flash".equals(lessonsBean.getType())) {
                    CommonUtil.shortCenterToast(getActivity(), "暂不支持该类型课时");
                    return;
                }

                if ("chapter".equals(lessonsBean.getType())
                        || "unit".equals(lessonsBean.getType())) {
                    return;
                }
                User user = getAppSettingProvider().getCurrentUser();
                if (user == null) {
                    CoreEngine.create(getContext()).runNormalPlugin("LoginActivity", getContext(), null);
                    return;
                }
                //判断归属于班级的课程有没有加入相关班级
                if ( getActivity().getIntent().getBooleanExtra(CourseActivity.IS_CHILD_COURSE, false)
                        && mMemberStatus != ISMEMBER && "0".equals(lessonsBean.getFree())) {
                    CommonUtil.shortCenterToast(getActivity(), getString(R.string.unjoin_class_course_hint));
                    return;
                }
                if (mMemberStatus != ISMEMBER && "0".equals(lessonsBean.getFree())) {
                    CommonUtil.shortCenterToast(getActivity(), getString(R.string.unjoin_course_hint));
                    return;
                }
                if ("flash".equals(lessonsBean.getType())) {
                    CommonUtil.shortCenterToast(getActivity(), "暂不支持该类型课时");
                    return;
                }
                perpareStartLearnLesson(lessonsBean);
            }
        });
    }

    private void initFirstLearnLesson() {
        if (mCourseCatalogue != null) {
            lessonsBeanList = mCourseCatalogue.getLessons();
            if (lessonsBeanList == null || lessonsBeanList.isEmpty() || (mMemberStatus != ISMEMBER && findFreeLessonInList() == null)) {
                return;
            }
            final Bundle bundle = new Bundle();
            lessonsBean = null;
            Map<String, String> learnStatuses = mCourseCatalogue.getLearnStatuses();
            //没加入
            if (mMemberStatus != ISMEMBER) {
                lessonsBean = findFreeLessonInList();
                bundle.putString(Const.COURSE_CHANGE_STATE, Const.COURSE_CHANGE_STATE_NONE);
            } else if (learnStatuses.containsKey("-1")) {
                //还没开始学,学第一个
                lessonsBean = findFirstLessonInList();
                bundle.putString(Const.COURSE_CHANGE_STATE, Const.COURSE_CHANGE_STATE_NONE);
            } else if (!learnStatuses.containsValue("learning") && (learnStatuses.size() == lessonSize())) {
                //所有课时学完
                lessonsBean = findFirstLessonInList();
                bundle.putString(Const.COURSE_CHANGE_STATE, Const.COURSE_CHANGE_STATE_FINISH);
            }else {
                lessonsBean = findFirseLearnLessonWithStatus(mCourseCatalogue);
                bundle.putString(Const.COURSE_CHANGE_STATE, Const.COURSE_CHANGE_STATE_STARTED);
            }
            reFreshColor();
            new LessonProvider(getContext()).getLesson(AppUtil.parseInt(lessonsBean.getId()))
                    .success(new NormalCallback<LessonItem>() {
                        @Override
                        public void success(LessonItem lessonItem) {
                            bundle.putSerializable(Const.COURSE_CHANGE_OBJECT, lessonItem);
                            MessageEngine.getInstance().sendMsg(Const.COURSE_HASTRIAL, bundle);
                        }});
        }
    }

    private CourseCatalogue.LessonsBean findFirseLearnLessonWithStatus(CourseCatalogue courseCatalogue) {
        Map<String, String> learnStatuses = courseCatalogue.getLearnStatuses();
        List<CourseCatalogue.LessonsBean> lessonsBeanList = courseCatalogue.getLessons();

        int size = lessonsBeanList.size();
        CourseCatalogue.LessonsBean currentFinishedLessonsBean = null;
        for (int i = 0; i < size; i++) {
            CourseCatalogue.LessonsBean lessonsBean = lessonsBeanList.get(i);
            String status = learnStatuses.get(lessonsBean.getId());
            if ("learning".equals(status)) {
                return lessonsBean;
            }
            if ("finished".equals(status)) {
                currentFinishedLessonsBean = lessonsBean;
            }
        }
        if ("finished".equals(learnStatuses.get(currentFinishedLessonsBean.getId())) && lessonsBeanList.indexOf(currentFinishedLessonsBean) + 1 < size) {
            currentFinishedLessonsBean = lessonsBeanList.get(lessonsBeanList.indexOf(currentFinishedLessonsBean) + 1);
        }
        return currentFinishedLessonsBean;
    }

    private CourseCatalogue.LessonsBean findFirstLessonInList() {
        List<CourseCatalogue.LessonsBean> lessonsBeanList = mCourseCatalogue.getLessons();
        for (int i = 0; i < lessonsBeanList.size(); i++) {
            CourseCatalogue.LessonsBean lessonsBean = lessonsBeanList.get(i);
            if ("lesson".equals(lessonsBean.getItemType())) {
                return lessonsBean;
            }
        }
        return null;
    }

    private CourseCatalogue.LessonsBean findFreeLessonInList() {
        List<CourseCatalogue.LessonsBean> lessonsBeanList = mCourseCatalogue.getLessons();
        for (int i = 0; i < lessonsBeanList.size(); i++) {
            CourseCatalogue.LessonsBean lessonsBean = lessonsBeanList.get(i);
            if ("lesson".equals(lessonsBean.getItemType()) && "1".equals(lessonsBean.getFree())) {
                return lessonsBean;
            }
        }
        return null;
    }

    private int lessonSize() {
        int count = 0;
        for (CourseCatalogue.LessonsBean bean : mCourseCatalogue.getLessons()) {
            if ("lesson".equals(bean.getItemType())) {
                count++;
            }
        }
        return count;
    }

    protected void showProcessDialog() {
        if (mProcessDialog == null) {
            mProcessDialog = LoadDialog.create(getActivity());
        }
        mProcessDialog.show();
    }

    protected void hideProcesDialog() {
        if (mProcessDialog == null) {
            return;
        }
        if (mProcessDialog.isShowing()) {
            mProcessDialog.dismiss();
        }
    }

    private void sendMessageToCourse(LessonItem lessonItem) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Const.COURSE_CHANGE_OBJECT, lessonItem);
        MessageEngine.getInstance().sendMsg(Const.COURSE_CHANGE, bundle);
        bundle.putString(Const.COURSE_CHANGE_STATE, Const.COURSE_CHANGE_STATE_STARTED);
        MessageEngine.getInstance().sendMsg(Const.COURSE_HASTRIAL, bundle);
    }

    private void getFullLessonFromServer(CourseCatalogue.LessonsBean lessonsBean) {

        new LessonProvider(getContext()).getLesson(AppUtil.parseInt(lessonsBean.getId()))
                .success(new NormalCallback<LessonItem>() {
                    @Override
                    public void success(LessonItem lessonItem) {
                    if ("waiting".equals(lessonItem.mediaConvertStatus)) {
                        CommonUtil.shortCenterToast(getActivity(), getString(R.string.lesson_loading));
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Const.COURSE_CHANGE_OBJECT, lessonItem);
                    MessageEngine.getInstance().sendMsg(Const.COURSE_CHANGE, bundle);
                    }
                }).fail(new NormalCallback<VolleyError>() {
                    @Override
                    public void success(VolleyError obj) {
                        CommonUtil.shortCenterToast(getActivity(), getString(R.string.course_loading_fail));
                    }
                });
    }

    public void perpareStartLearnLesson(CourseCatalogue.LessonsBean lessonsBean) {
        if ("self".equals(lessonsBean.getMediaSource())) {
            if ("audio".equals(lessonsBean.getType()) || "video".equals(lessonsBean.getType())) {
                getFullLessonFromServer(lessonsBean);
                return;
            }
        }

        if ("live".equals(lessonsBean.getType())) {
            School school = getAppSettingProvider().getCurrentSchool();
            final String url = String.format(school.host + Const.WEB_LESSON, mCourseId, lessonsBean.getId() );
            CoreEngine.create(getContext()).runNormalPlugin("WebViewActivity", getContext(), new PluginRunCallback() {
                @Override
                public void setIntentDate(Intent startIntent) {
                    startIntent.putExtra(Const.WEB_URL, url);
                }
            });
            return;
        }
        sendMessageToCourse(lessonsBean.toLessonItem());
    }

    public void startLessonActivity(String type, int lessonId, int courseId, int memberState) {
        if (mCourseStateCallback != null && mCourseStateCallback.isExpired()) {
            mCourseStateCallback.handlerCourseExpired();
            return;
        }
        if ("live".equals(type)) {
            School school = getAppSettingProvider().getCurrentSchool();
            final String url = String.format(school.host + Const.WEB_LESSON, courseId, lessonId);
            CoreEngine.create(getContext()).runNormalPlugin("WebViewActivity", getContext(), new PluginRunCallback() {
                @Override
                public void setIntentDate(Intent startIntent) {
                    startIntent.putExtra(Const.WEB_URL, url);
                }
            });
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(Const.LESSON_ID, lessonId);
        bundle.putInt(Const.COURSE_ID, courseId);
        bundle.putInt(LessonActivity.MEMBER_STATE, memberState);
        bundle.putIntegerArrayList(Const.LESSON_IDS, getLessonArray());
        CoreEngine.create(getContext()).runNormalPluginWithBundleForResult(
                "LessonActivity", getActivity(), bundle, LessonActivity.REQUEST_LEARN);
    }

    public ArrayList<Integer> getLessonArray() {
        ArrayList<Integer> lessonArray = new ArrayList<>();
        for (CourseCatalogue.LessonsBean lessonsBean : mCourseCatalogue.getLessons()) {
            if ("lesson".equals(lessonsBean.getItemType())) {
                lessonArray.add(Integer.parseInt(lessonsBean.getId()));
            }
        }
        return lessonArray;
    }

    private boolean isOk = false;
    public void reFreshColor(){
        if (mLvCatalog != null && lessonsBean != null && isOk) {
            int index = ((ArrayList) mCourseCatalogue.getLessons()).indexOf(lessonsBean);
            //mLvCatalog.setItemChecked(index, true);
        }
        isOk = true;
    }

    /**
     * 外部刷新数据
     */
    @Override
    public void reFreshView(boolean mIsJoin) {
        mMemberStatus = mIsJoin ? ISMEMBER : VISITOR;
        isJoin = mIsJoin;
        initCatalogue();
    }

    public View.OnClickListener getCacheCourse() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(getActivity(), "courseDetailsPage_cachingLessons");
                if (mCourseStateCallback.isExpired()) {
                    mCourseStateCallback.handlerCourseExpired();
                    return;
                }
                if (getRomAvailableSize().contains("M")) {
                    if (Float.parseFloat(getRomAvailableSize().replaceAll("[a-zA-Z]", "").trim()) < 100) {
                        CommonUtil.shortCenterToast(getActivity(), getString(R.string.cache_hint));
                        return;
                    }
                }
                startActivity(new Intent(getContext(), LessonDownloadingActivity.class).putExtra(Const.COURSE_ID, mCourseId));
            }
        };
    }

    private String getRomAvailableSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(getActivity(), blockSize * availableBlocks).replace("B", "");
    }

    public void invoke(WidgetMessage message) {
        if (Const.LESSON_STATUS_REFRESH.equals(message.type.type)) {
            updateLessonStatuses();
        }
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

}