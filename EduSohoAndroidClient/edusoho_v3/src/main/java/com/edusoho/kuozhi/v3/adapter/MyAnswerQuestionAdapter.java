package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.thread.MyThreadEntity;
import com.edusoho.kuozhi.v3.ui.DiscussDetailActivity;
import com.edusoho.kuozhi.v3.ui.chat.AbstractIMChatActivity;
import com.edusoho.kuozhi.v3.ui.fragment.mine.MineFragment;
import com.edusoho.kuozhi.v3.ui.fragment.mine.MyQuestionFragment;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.view.EduHtmlHttpImageGetter;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by JesseHuang on 2017/2/9.
 */

public class MyAnswerQuestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<MyThreadEntity> mMyThreadEntities;

    private static final int EMPTY = 0;
    private static final int NOT_EMPTY = 1;
    private int mCurrentDataStatus;

    public MyAnswerQuestionAdapter(Context context) {
        mContext = context;
        mMyThreadEntities = new ArrayList<>();
        mCurrentDataStatus = EMPTY;
    }

    public MyAnswerQuestionAdapter(Context context, List<MyThreadEntity> list) {
        mContext = context;
        mMyThreadEntities = list;
    }

    public void setData(List<MyThreadEntity> list) {
        mMyThreadEntities = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mCurrentDataStatus;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mCurrentDataStatus == NOT_EMPTY) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_my_answer_question, parent, false);
            return new MyQuestionFragment.ViewHolderAnswer(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_empty, parent, false);
            return new MineFragment.EmptyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (mCurrentDataStatus == NOT_EMPTY) {
            MyQuestionFragment.ViewHolderAnswer viewHolderAnswer = (MyQuestionFragment.ViewHolderAnswer) viewHolder;
            final MyThreadEntity entity = mMyThreadEntities.get(position);
            viewHolderAnswer.tvOrder.setText(entity.getCourse().title);
            viewHolderAnswer.tvTime.setText(CommonUtil.convertMills2Date(Long.parseLong(entity.getCreatedTime()) * 1000));
            viewHolderAnswer.tvContentAsk.setText(entity.getTitle());
            viewHolderAnswer.tvContentAnswer.setText(Html.fromHtml(entity.getContent(), new EduHtmlHttpImageGetter(viewHolderAnswer.tvContentAnswer, null, true), null));
            viewHolderAnswer.layout.setTag(entity);
            viewHolderAnswer.layout.setOnClickListener(getAnswerClickListener());
        }
    }

    @Override
    public int getItemCount() {
        if (mMyThreadEntities != null && mMyThreadEntities.size() != 0) {
            mCurrentDataStatus = NOT_EMPTY;
            return mMyThreadEntities.size();
        }
        mCurrentDataStatus = EMPTY;
        return 1;
    }

    private View.OnClickListener getAnswerClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyThreadEntity entity = (MyThreadEntity) v.getTag();
                Bundle bundle = new Bundle();
                bundle.putString(DiscussDetailActivity.THREAD_TARGET_TYPE, "course");
                bundle.putInt(DiscussDetailActivity.THREAD_TARGET_ID, entity.getCourse().id);
                bundle.putInt(AbstractIMChatActivity.FROM_ID, Integer.parseInt(entity.getThreadId()));
                bundle.putString(AbstractIMChatActivity.TARGET_TYPE, entity.getType());
                EdusohoApp.app.mEngine.runNormalPluginWithBundle("DiscussDetailActivity", mContext, bundle);
            }
        };
    }
}
