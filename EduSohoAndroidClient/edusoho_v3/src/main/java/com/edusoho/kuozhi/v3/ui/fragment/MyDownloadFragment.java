package com.edusoho.kuozhi.v3.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.CourseDownloadAdapter;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.entity.course.DownloadCourse;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.bal.course.CourseMember;
import com.edusoho.kuozhi.v3.model.provider.CourseProvider;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.CourseCacheHelper;
import com.edusoho.kuozhi.v3.util.M3U8Util;
import cn.trinea.android.common.util.ToastUtils;


/**
 * Created by suju on 17/1/10.
 */

public class MyDownloadFragment extends Fragment implements AdapterView.OnItemClickListener {

    private View mEmptyView;
    private TextView tvEmptyText;
    private ListView mListView;
    private CourseDownloadAdapter mAdapter;
    private CourseCacheHelper mCourseCacheHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User user = getAppSettingProvider().getCurrentUser();
        School school = getAppSettingProvider().getCurrentSchool();
        mCourseCacheHelper = new CourseCacheHelper(getContext(), school.getDomain(), user.id);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View containerView = inflater.inflate(R.layout.fragment_mydownload_layout, null);
        ViewGroup parent = (ViewGroup) containerView.getParent();
        if (parent != null) {
            parent.removeView(containerView);
        }
        return containerView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(R.id.listview);
        mEmptyView = view.findViewById(R.id.ll_empty);
        tvEmptyText = (TextView) view.findViewById(R.id.tv_empty_text);
        tvEmptyText.setText(getContext().getText(R.string.no_lesson_cache));
        mAdapter = new CourseDownloadAdapter(getContext());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getAppSettingProvider().getCurrentUser() == null) {
            return;
        }
        mAdapter.setCourseList(mCourseCacheHelper.getLocalCourseList(M3U8Util.ALL, null, null));
        setEmptyState(mAdapter.getCount() == 0);
    }

    private void setEmptyState(boolean isEmpty) {
        mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bundle bundle = new Bundle();
        DownloadCourse course = (DownloadCourse) parent.getItemAtPosition(position);
        if (course.isExpird()) {
            ToastUtils.show(getContext(), R.string.download_course_expird_timeout);
            return;
        }
        bundle.putInt(Const.COURSE_ID, course.id);
        CoreEngine.create(getContext()).runNormalPluginWithBundle("DownloadManagerActivity", getContext(), bundle);
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }
}
