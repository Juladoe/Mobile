package com.edusoho.kuozhi.v3.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.ClassCatalogueAdapter;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.Classroom;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.provider.ClassRoomProvider;
import com.edusoho.kuozhi.v3.ui.course.ICourseStateListener;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by DF on 2016/12/15.
 */

public class ClassCatalogFragment extends Fragment implements ICourseStateListener {

    public boolean isJoin = false;
    public int mClassRoomId = 0;
    private RecyclerView mRvClass;

    private View mLoadView;
    private List<Course> mCourseList;
    private TextView mLessonEmpytView;

    public ClassCatalogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_catalog, container, false);
        mRvClass = (RecyclerView) view.findViewById(R.id.lv_catalog);
        mLoadView = view.findViewById(R.id.il_class_catalog_load);
        mLessonEmpytView = (TextView) view.findViewById(R.id.ll_course_catalog_empty);
        return view;
    }

    protected void setLoadStatus(int visibility) {
        if (mLoadView != null) {
            mLoadView.setVisibility(visibility);
        }
    }

    private void initData() {
        mClassRoomId = getArguments().getInt(Const.CLASSROOM_ID);
        setLoadStatus(View.VISIBLE);
        new ClassRoomProvider(getActivity()).getCourseList(mClassRoomId)
                .success(new NormalCallback<List<Course>>() {
                    @Override
                    public void success(List<Course> list) {
                        mCourseList = list;
                        setLoadStatus(View.GONE);
                        if (mCourseList != null && !mCourseList.isEmpty()) {
                            saveCourseListToCache(mCourseList);
                            initView();
                        } else {
                            setLessonEmptyViewVisibility(View.VISIBLE);
                        }
                    }
                }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                setLoadStatus(View.GONE);
            }
        });
    }

    private void saveCourseListToCache(List<Course> list) {
        StringBuilder sb = new StringBuilder();
        for (Course course : list) {
            sb.append(course.id).append(",");
        }
        SqliteUtil sqliteUtil = SqliteUtil.getUtil(getContext());
        sqliteUtil.saveLocalCache(
                Const.CACHE_CLASSROOM_COURSE_IDS_TYPE,
                String.format("classroom-%s", mClassRoomId),
                sb.toString()
        );
    }

    private String getClassRoomName(int classRoomId) {
        SqliteUtil sqliteUtil = SqliteUtil.getUtil(getContext());
        Classroom classroom = sqliteUtil.queryForObj(
                new TypeToken<Classroom>() {
                },
                "where type=? and key=?",
                Const.CACHE_COURSE_TYPE,
                "classroom-" + classRoomId
        );
        if (classroom != null) {
            return classroom.title;
        }
        return "";
    }

    private void initView() {
        ClassCatalogueAdapter classAdapter = new ClassCatalogueAdapter(getActivity(), mCourseList, isJoin);
        mRvClass.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvClass.setAdapter(classAdapter);
        classAdapter.setOnItemClickListener(new ClassCatalogueAdapter.OnItemClickListener() {
            @Override
            public void click(int position) {
                Bundle bundle = new Bundle();
                bundle.putInt(Const.COURSE_ID, mCourseList.get(position).id);
                bundle.putString(Const.SOURCE, getClassRoomName(mClassRoomId));
                CoreEngine.create(getContext()).runNormalPluginWithBundle("CourseActivity", getContext(), bundle);
            }
        });
    }

    @Override
    public void reFreshView(boolean mJoin) {
        isJoin = mJoin;
        if (getActivity() != null) {
            initData();
        }
    }

    private void setLessonEmptyViewVisibility(int visibility) {
        mLessonEmpytView.setVisibility(visibility);
    }
}
