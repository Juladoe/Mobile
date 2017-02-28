package com.edusoho.longinus.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.longinus.R;
import com.edusoho.longinus.ui.LiveNoticeListActivity;

import java.util.List;

/**
 * Created by suju on 16/10/23.
 */
public class LiveNoticeListAdapter extends RecyclerView.Adapter<LiveNoticeListAdapter.NoticeViewHolder> {

    private Context mContext;
    private List<LiveNoticeListActivity.NoticeEntity> mNoticeList;

    public LiveNoticeListAdapter(Context context, List<LiveNoticeListActivity.NoticeEntity> list) {
        this.mContext = context;
        this.mNoticeList = list;
    }

    @Override
    public int getItemCount() {
        return mNoticeList.size();
    }

    @Override
    public void onBindViewHolder(NoticeViewHolder holder, int position) {
        holder.setData(mNoticeList.get(position));
    }

    @Override
    public NoticeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.item_live_notice_list_layout, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentView.setLayoutParams(lp);

        return new NoticeViewHolder(contentView);
    }

    protected class NoticeViewHolder extends RecyclerView.ViewHolder {

        private TextView mContentView;
        private TextView mTimeView;

        public NoticeViewHolder(View view) {
            super(view);
            mContentView = (TextView) view.findViewById(R.id.tv_notice_content);
            mTimeView = (TextView) view.findViewById(R.id.tv_notice_time);
        }

        public void setData(LiveNoticeListActivity.NoticeEntity noticeEntity) {
            mContentView.setText(noticeEntity.getContent());
            mTimeView.setText(AppUtil.convertMills2Date(noticeEntity.getCreateTime()));
        }
    }
}
