package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.thread.MyThreadEntity;
import com.edusoho.kuozhi.v3.ui.ThreadDiscussChatActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by melomelon on 16/2/29.
 */
public class MyThreadAdapter extends RecyclerView.Adapter {

    public static String POST_THREAD = "postThread";
    public static String CREAT_THREAD = "creatThread";
    private String originType;

    private LayoutInflater mLayoutInflater;
    private List<MyThreadEntity> mDataList;
    private EdusohoApp mApp;
    private Context mContext;

    public MyThreadAdapter(Context context, EdusohoApp app,String type) {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        mDataList = new ArrayList();
        mApp = app;
        originType = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ThreadItemViewHolder(mLayoutInflater.inflate(R.layout.my_thread_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ThreadItemViewHolder) {
            final MyThreadEntity threadEntity = mDataList.get(position);
            final Course course = threadEntity.getCourse();
            final int threadId;
            if (POST_THREAD.equals(originType)){
                threadId = Integer.parseInt(threadEntity.getThreadId());
            }else {
                threadId = Integer.parseInt(threadEntity.getId());
            }
            if ("question".equals(threadEntity.getType())) {
                ((ThreadItemViewHolder) holder).tvThreadType.setText("问");
                ((ThreadItemViewHolder) holder).tvThreadType.setBackgroundResource(R.drawable.round_green_bg);
            } else {
                ((ThreadItemViewHolder) holder).tvThreadType.setText("话");
                ((ThreadItemViewHolder) holder).tvThreadType.setBackgroundResource(R.drawable.round_blue_bg);
            }
            if ("".equals(course.smallPicture)) {
                ((ThreadItemViewHolder) holder).ivAvatar.setImageResource(R.drawable.defaultpic);
            } else {
                ImageLoader.getInstance().displayImage(course.smallPicture, ((ThreadItemViewHolder) holder).ivAvatar, mApp.mOptions);
            }
            ((ThreadItemViewHolder) holder).tvThreadCourseTitle.setText(String.format("来自课程：『%s』", course.title));
            ((ThreadItemViewHolder) holder).tvThreadTitle.setText(threadEntity.getTitle());
            ((ThreadItemViewHolder) holder).tvTime.setText(AppUtil.convertMills2Date(Long.parseLong(threadEntity.getCreatedTime()) * 1000));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mApp.mEngine.runNormalPlugin("ThreadDiscussActivity", mContext, new PluginRunCallback() {
                        @Override
                        public void setIntentDate(Intent startIntent) {
                            startIntent.putExtra(ThreadDiscussChatActivity.THREAD_TARGET_ID, course.id);
                            startIntent.putExtra(ThreadDiscussChatActivity.THREAD_TARGET_TYPE, "course");
                            startIntent.putExtra(ThreadDiscussChatActivity.FROM_ID, threadId);
                            startIntent.putExtra(ThreadDiscussChatActivity.THREAD_TYPE, threadEntity.getType());
                        }
                    });
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ThreadItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivAvatar;
        private TextView tvThreadType;
        private TextView tvThreadTitle;
        private TextView tvTime;
        private TextView tvThreadCourseTitle;


        public ThreadItemViewHolder(View itemView) {
            super(itemView);
            ivAvatar = (ImageView) itemView.findViewById(R.id.my_thread_item_avatar);
            tvThreadTitle = (TextView) itemView.findViewById(R.id.my_thread_item_title);
            tvTime = (TextView) itemView.findViewById(R.id.my_thread_item_time);
            tvThreadType = (TextView) itemView.findViewById(R.id.my_thread_item_type);
            tvThreadCourseTitle = (TextView) itemView.findViewById(R.id.my_thread_item_course);
        }

    }

    public void addDataList(List list) {
        mDataList.addAll(list);
        notifyDataSetChanged();
    }


}
