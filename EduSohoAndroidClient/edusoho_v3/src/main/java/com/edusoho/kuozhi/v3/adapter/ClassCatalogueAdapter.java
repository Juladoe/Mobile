package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by DF on 2016/12/15.
 */

public class ClassCatalogueAdapter extends RecyclerView.Adapter<ClassCatalogueAdapter.ClassHolder>{
    private Context mContext;
    private boolean isJoin;
    private List<Course> mCourseList;
    private OnItemClickListener mOnItemClickListener;


    public ClassCatalogueAdapter(Context mContext, List<Course> mCourseList, boolean isJoin) {
        this.mCourseList = mCourseList;
        this.isJoin = isJoin;
    }

    @Override
    public ClassCatalogueAdapter.ClassHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ClassHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_catalog, parent, false));
    }

    @Override
    public void onBindViewHolder(ClassCatalogueAdapter.ClassHolder holder, final int position) {
        render(holder, mCourseList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.click(position);
                }
            }
        });
    }

    private void render(ClassCatalogueAdapter.ClassHolder holder,Course course) {
        ImageLoader.getInstance().displayImage(course.middlePicture, holder.mIvClass, EdusohoApp.app.mOptions);
        holder.mTvTitle.setText(course.title);
        if (!isJoin) {
            holder.mTvPeople.setText(
                    String.format(mContext.getResources().getString(R.string.class_catalog_people), course.studentNum));
        }
        if (!isJoin) {
            if (course.price <= 0) {
                holder.mTvFree.setText(R.string.class_catalog_free);
                holder.mTvFree.setTextColor(mContext.getResources().getColor(R.color.primary_color));
            } else {
                holder.mTvFree.setTextColor(mContext.getResources().getColor(R.color.secondary_color));
                holder.mTvFree.setText(String.format("¥%.2f", course.price));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mCourseList == null ? 0 : mCourseList.size();
    }

    public static class ClassHolder extends RecyclerView.ViewHolder{
        public ImageView mIvClass;
        public TextView mTvTitle;
        public TextView mTvFree;
        public TextView mTvPeople;
        public ClassHolder(View itemView) {
            super(itemView);
            mIvClass = (ImageView) itemView.findViewById(R.id.iv_class);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mTvFree = (TextView) itemView.findViewById(R.id.tv_free_price);
            mTvPeople = (TextView) itemView.findViewById(R.id.tv_people_num);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void click(int position);
    }
}
