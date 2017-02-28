package com.edusoho.kuozhi.v3.adapter.discuss;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.entity.course.DiscussDetail;
import com.edusoho.kuozhi.v3.ui.course.CourseStudyDetailActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DF on 2017/2/9.
 */

public class CourseDiscussAdapter extends RecyclerView.Adapter implements View.OnClickListener{


    public List<DiscussDetail.ResourcesBean> mList;
    private Context mContext;
    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;
    private static final int TYPE_ITEM   = 0;
    private static final int TYPE_FOOTER = 1;
    //上拉加载更多
    public static final int PULLUP_LOAD_MORE = 0;
    //正在加载中
    public static final int LOADING_MORE     = 1;
    //没有加载更多 隐藏
    public static final int NO_LOAD_MORE     = 2;

    //上拉加载更多状态-默认为0
    private int mLoadMoreStatus = 0;

    public List<DiscussDetail.ResourcesBean> getmList() {
        return mList;
    }

    public CourseDiscussAdapter(Context mContext) {
        this.mContext = mContext;
        this.mList = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            //最后一个item设置为footerView
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public void AddFooterItem(List<DiscussDetail.ResourcesBean> items) {
        mList.addAll(items);
        notifyDataSetChanged();
    }

    public void reFreshData(List<DiscussDetail.ResourcesBean> list) {
        mList.clear();
        mList = list;
        notifyDataSetChanged();
    }

    public void changeMoreStatus(int status){
        mLoadMoreStatus=status;
        notifyDataSetChanged();
    }

    public void setStatus(int status) {
        mLoadMoreStatus = status;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, DiscussDetail.ResourcesBean resourcesBean);
    }

    public void setDataAndNotifyData(List<DiscussDetail.ResourcesBean> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener (OnRecyclerViewItemClickListener listener) {
        this.onRecyclerViewItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View itemVIew = LayoutInflater.from(parent.getContext()).inflate(R.layout.foot_item, parent, false);
            return new FooterViewHolder(itemVIew);
        }
        View rootView= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discuss_topic,parent,false);
        rootView.setOnClickListener(this);
        return new MyViewHolder(rootView);
    }

    @Override
    public void onClick(View v) {
        if (onRecyclerViewItemClickListener != null) {
            onRecyclerViewItemClickListener.onItemClick(v, ((DiscussDetail.ResourcesBean) v.getTag()));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            DiscussDetail.ResourcesBean resourcesBean = mList.get(position);
            holder.itemView.setTag(mList.get(position));
            ImageLoader.getInstance().displayImage(resourcesBean.getUser().getAvatar(), ((MyViewHolder)holder).ivUser, EdusohoApp.app.mAvatarOptions);
            ((MyViewHolder)holder).tvUserName.setText(resourcesBean.getUser().getNickname());
            ((MyViewHolder)holder).tvContent.setText(String.format("         %s", resourcesBean.getTitle()));
            ((MyViewHolder)holder).tvCommentNum.setText(resourcesBean.getPostNum());
            ((MyViewHolder)holder).tvTime.setText(CommonUtil.conver2Date(CommonUtil.convertMilliSec(mContext instanceof CourseStudyDetailActivity ? resourcesBean.getLatestPostTime() : resourcesBean.getUpdatedTime()) + 28800000).substring(2, 16));
            if ("question".equals(resourcesBean.getType())) {
                ((MyViewHolder)holder).tvKind.setText("问题");
                ((MyViewHolder)holder).tvKind.setTextColor(mContext.getResources().getColor(R.color.primary_color));
                ((MyViewHolder)holder).tvKind.setBackgroundResource(R.drawable.discuss_question);
            } else {
                ((MyViewHolder)holder).tvKind.setText("话题");
                ((MyViewHolder)holder).tvKind.setTextColor(mContext.getResources().getColor(R.color.secondary2_color));
                ((MyViewHolder)holder).tvKind.setBackgroundResource(R.drawable.discuss_topic);
            }
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            switch (mLoadMoreStatus) {
                case PULLUP_LOAD_MORE:
                    footerViewHolder.mTvLoadText.setText("上拉加载更多...");
                    break;
                case LOADING_MORE:
                    footerViewHolder.mTvLoadText.setText("正加载更多...");
                    break;
                case NO_LOAD_MORE:
                    //隐藏加载更多
                    footerViewHolder.mLoadLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
            return mList.size() + 1;
        }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivUser;
        private TextView tvUserName;
        private TextView tvKind;
        private TextView tvContent;
        private TextView tvCommentNum;
        private TextView tvTime;

        private MyViewHolder(View itemView) {
            super(itemView);
            ivUser = (ImageView) itemView.findViewById(R.id.iv_user_icon);
            tvUserName = (TextView) itemView.findViewById(R.id.tv_user_name);
            tvKind = (TextView) itemView.findViewById(R.id.tv_kind);
            tvContent = (TextView) itemView.findViewById(R.id.tv_content);
            tvCommentNum = (TextView) itemView.findViewById(R.id.tv_comment_num);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {

        private  LinearLayout mLoadLayout;
        private  TextView mTvLoadText;

        private FooterViewHolder(View itemView) {
            super(itemView);
            mLoadLayout = (LinearLayout) itemView.findViewById(R.id.ll_load);
            mTvLoadText =  (TextView) itemView.findViewById(R.id.tv_load);
        }
    }
}
