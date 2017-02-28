package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.model.bal.push.Notify;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suju on 16/11/10.
 */
public class NofityListAdapter extends RecyclerView.Adapter<NofityListAdapter.ViewHolder> {

    static final int NORMAL = 0;
    static final int LIVE_START = 1;

    private Context mContext;
    private List<Notify> mList;
    private OnItemClickListener mOnItemClickListener;

    public NofityListAdapter(Context context) {
        this.mContext = context;
        this.mList = new ArrayList<>();
    }

    public void addDataList(List<Notify> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.item_notify_layout, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentView.setLayoutParams(lp);
        return createViewHolder(contentView, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(mList.get(position));
        if (mOnItemClickListener != null) {
            holder.addOnItemClickListener(mOnItemClickListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Notify notify = mList.get(position);
        switch (notify.getType()) {
            case "live_start":
                return LIVE_START;
        }
        return NORMAL;
    }

    public void addOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    private ViewHolder createViewHolder(View view, int viewType) {
        if (viewType == LIVE_START) {
            return new LiveStartViewHolder(view);
        }

        return new ViewHolder(view);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView timeView;
        protected TextView contentView;
        protected TextView titleView;

        public ViewHolder(View view) {
            super(view);
            timeView = (TextView) view.findViewById(R.id.tv_nofity_time);
            titleView = (TextView) view.findViewById(R.id.tv_nofity_title);
            contentView = (TextView) view.findViewById(R.id.tv_nofity_content);
        }

        public void addOnItemClickListener(final OnItemClickListener onItemClickListener) {
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(view, mList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
        }

        public void setData(Notify notify) {
            timeView.setText(AppUtil.convertMills2Date(notify.getCreatedTime()));
            titleView.setText(notify.getTitle());
            contentView.setText(notify.getContent());
        }
    }

    class LiveStartViewHolder extends ViewHolder {

        public LiveStartViewHolder(View view) {
            super(view);
        }

        @Override
        public void setData(Notify notify) {
            timeView.setText(AppUtil.convertMills2Date(notify.getCreatedTime()));
            Map<String, String> contentData = new Gson().fromJson(notify.getContent(), LinkedHashMap.class);
            String title = contentData.get("lessonTitle");
            titleView.setText(TextUtils.isEmpty(title) ? notify.getTitle() : title);
            contentView.setText(coverContentColor(contentData.get("message"), "点击学习"));
        }

        private SpannableString coverContentColor(String content, String doChange) {
            StringBuffer stringBuffer = new StringBuffer(content);
            int start = content.indexOf(doChange);
            SpannableString spannableString = new SpannableString(stringBuffer);
            if (start == -1) {
                return spannableString;
            }
            int color = mContext.getResources().getColor(R.color.primary);
            spannableString.setSpan(
                    new ForegroundColorSpan(color), start, start + doChange.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

            return spannableString;
        }
    }

    public static interface OnItemClickListener<T> {

        void onItemClick(View view, T item, int position);
    }
}
