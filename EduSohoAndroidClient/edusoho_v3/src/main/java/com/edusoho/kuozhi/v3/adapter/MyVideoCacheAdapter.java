package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.entity.course.DownloadCourse;
import com.edusoho.kuozhi.v3.ui.fragment.mine.MineFragment;
import com.edusoho.kuozhi.v3.ui.fragment.mine.MyVideoCacheFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by JesseHuang on 2017/2/10.
 */

public class MyVideoCacheAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EMPTY = 0;
    private static final int NOT_EMPTY = 1;
    private int mCurrentDataStatus;

    private Context mContext;
    private List<DownloadCourse> mList;

    public MyVideoCacheAdapter(Context context) {
        this.mContext = context;
        mList = new ArrayList<>();
        mCurrentDataStatus = EMPTY;
    }

    public void setData(List<DownloadCourse> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mCurrentDataStatus;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mCurrentDataStatus == NOT_EMPTY) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_download_manager_course_group, parent, false);
            return new MyVideoCacheFragment.VideoCacheViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_empty, parent, false);
            return new MineFragment.EmptyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (mCurrentDataStatus == NOT_EMPTY) {
            final DownloadCourse course = mList.get(position);
            MyVideoCacheFragment.VideoCacheViewHolder videoCacheViewHolder = (MyVideoCacheFragment.VideoCacheViewHolder) viewHolder;
            ImageLoader.getInstance().displayImage(course.getPicture(), videoCacheViewHolder.ivCover);
            videoCacheViewHolder.tvCourseTitle.setText(course.title);
            videoCacheViewHolder.ivVideoSizes.setText(getCacheSize(videoCacheViewHolder.ivVideoSizes.getContext(), course.getCachedSize()));
            videoCacheViewHolder.ivVideoSum.setText(String.format(
                    videoCacheViewHolder.ivVideoSum.getResources().getString(R.string.download_size_cached),
                    course.getCachedLessonNum()
            ));

            videoCacheViewHolder.tvExpiredView.setVisibility(course.isExpird() ? View.VISIBLE : View.GONE);
            if ("classroom".equals(course.source)) {
                videoCacheViewHolder.tvSource.setVisibility(View.VISIBLE);
                videoCacheViewHolder.tvSource.setText(AppUtil.getColorTextAfter(
                        videoCacheViewHolder.tvSource.getResources().getString(R.string.download_size_course_source),
                        course.getSourceName(),
                        Color.rgb(113, 119, 125)
                ));
            } else {
                videoCacheViewHolder.tvSource.setVisibility(View.GONE);
                videoCacheViewHolder.tvSource.setText("");
            }
            videoCacheViewHolder.rlayoutContent.setTag(position);
            videoCacheViewHolder.rlayoutContent.setOnClickListener(getItemClickListener());
        }
    }

    private View.OnClickListener getItemClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                Bundle bundle = new Bundle();
                final DownloadCourse course = mList.get(position);
                if (course.isExpird()) {
                    ToastUtils.show(mContext, R.string.download_course_expird_timeout);
                    return;
                }
                bundle.putInt(Const.COURSE_ID, course.id);
                CoreEngine.create(mContext).runNormalPluginWithBundle("DownloadManagerActivity", mContext, bundle);
            }
        };
    }

    private String getCacheSize(Context context, long size) {
        float realSize = size / 1024.0f / 1024.0f;
        if (realSize == 0) {
            return "0M";
        } else {
            return String.format("%.1f%s", realSize, "M");
        }
    }

    @Override
    public int getItemCount() {
        if (mList != null && mList.size() != 0) {
            mCurrentDataStatus = NOT_EMPTY;
            return mList.size();
        }
        mCurrentDataStatus = EMPTY;
        return 1;
    }
}
