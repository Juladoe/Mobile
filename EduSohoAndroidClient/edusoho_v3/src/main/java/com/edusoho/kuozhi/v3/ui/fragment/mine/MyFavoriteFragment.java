package com.edusoho.kuozhi.v3.ui.fragment.mine;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.MyFavoriteAdapter;
import com.edusoho.kuozhi.v3.entity.course.LearningCourse;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailModel;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by JesseHuang on 2017/2/7.
 */

public class MyFavoriteFragment extends BaseFragment implements MineFragment.RefreshFragment {

    private SwipeRefreshLayout srlContent;
    private RecyclerView rvContent;
    private MyFavoriteAdapter myFavoriteAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.fragment_mine_tab);
    }

    @Override
    protected void initView(View view) {
        srlContent = (SwipeRefreshLayout) view.findViewById(R.id.srl_content);
        srlContent.setColorSchemeResources(R.color.primary_color);

        rvContent = (RecyclerView) view.findViewById(R.id.rv_content);
        rvContent.setLayoutManager(new LinearLayoutManager(getActivity()));

        View viewBreakline = view.findViewById(R.id.v_breakline);
        viewBreakline.setVisibility(View.GONE);

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
        showLoadingView();
        myFavoriteAdapter = new MyFavoriteAdapter(getActivity());
        rvContent.setAdapter(myFavoriteAdapter);
    }

    private void loadData() {
        final List<Course> loadCourseList = new ArrayList<>();
        CourseDetailModel.getNormalCollect(1000, 0, new ResponseCallbackListener<LearningCourse>() {
            @Override
            public void onSuccess(final LearningCourse liveCourseList) {
                disabledLoadingView();
                loadCourseList.addAll(liveCourseList.data);
                CourseDetailModel.getLiveCollect(1000, 0, new ResponseCallbackListener<LearningCourse>() {
                    @Override
                    public void onSuccess(LearningCourse courseList) {
                        loadCourseList.addAll(courseList.data);
                        myFavoriteAdapter.setData(loadCourseList);
                    }

                    @Override
                    public void onFailure(String code, String message) {
                        ToastUtils.show(mContext, message);
                    }
                });
            }

            @Override
            public void onFailure(String code, String message) {
                ToastUtils.show(mContext, message);
            }
        });
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

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        public View recyclerViewItem;
        public ImageView ivPic;
        public TextView tvAddNum;
        public TextView tvTitle;
        public TextView tvMore;
        public View layoutLive;
        public TextView tvLiveIcon;
        public TextView tvLive;
        public View vLine;

        public FavoriteViewHolder(View view) {
            super(view);
            recyclerViewItem = view.findViewById(R.id.llayout_favorite_content);
            ivPic = (ImageView) view.findViewById(R.id.iv_pic);
            tvAddNum = (TextView) view.findViewById(R.id.tv_add_num);
            tvMore = (TextView) view.findViewById(R.id.tv_more);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            vLine = view.findViewById(R.id.v_line);
            layoutLive = view.findViewById(R.id.layout_live);
            tvLiveIcon = (TextView) view.findViewById(R.id.tv_live_icon);
            tvLive = (TextView) view.findViewById(R.id.tv_live);
        }
    }
}
