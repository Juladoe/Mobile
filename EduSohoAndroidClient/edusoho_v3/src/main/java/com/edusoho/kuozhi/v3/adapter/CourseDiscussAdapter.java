package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.push.Chat;
import com.edusoho.kuozhi.v3.util.sql.CourseDiscussDataSource;
import com.edusoho.kuozhi.v3.util.sql.SqliteChatUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by JesseHuang on 15/12/15.
 */
public class CourseDiscussAdapter<T extends Chat> extends ChatAdapter<T> {

    private CourseDiscussDataSource mCourseDiscussDataSource;

    public CourseDiscussAdapter(Context context, ArrayList<T> list) {
        mList = list;
        mContext = context;
        mDownloadList = new HashMap<>();
        mCourseDiscussDataSource = new CourseDiscussDataSource(SqliteChatUtil.getSqliteChatUtil(mContext, EdusohoApp.app.domain));
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).
                showImageForEmptyUri(R.drawable.user_avatar).
                showImageOnFail(R.drawable.user_avatar).build();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CourseViewHolder holder;
        int type = getItemViewType(position);
        if (convertView == null) {
            convertView = createViewByType(type);
            holder = new CourseViewHolder(convertView, type);
            convertView.setTag(holder);
        } else {
            holder = (CourseViewHolder) convertView.getTag();
        }

        switch (type) {
            case MSG_SEND_TEXT:
                handleSendMsgText(holder, position);
                break;
            case MSG_RECEIVE_TEXT:
                handleReceiveMsgText(holder, position);
                break;
            case MSG_SEND_IMAGE:
                handlerSendImage(holder, position);
                break;
            case MSG_RECEIVE_IMAGE:
                handlerReceiveImage(holder, position);
                break;
            case MSG_SEND_AUDIO:
                handlerSendAudio(holder, position);
                break;
            case MSG_RECEIVE_AUDIO:
                handlerReceiveAudio(holder, position);
                break;
            case MSG_SEND_MULIT:
                handleSendMsgMulti(holder, position);
            case MSG_RECEIVE_MULIT:
                handlerMultiMsg(holder, position);
                break;
        }
        return convertView;
    }

    public class CourseViewHolder extends ViewHolder {
        public TextView tvNickname;

        public CourseViewHolder(View view, int type) {
            super(view, type);
            if (type == MSG_RECEIVE_TEXT || type == MSG_RECEIVE_IMAGE || type == MSG_RECEIVE_AUDIO) {
                tvNickname = (TextView) view.findViewById(R.id.tv_nickname);
            }
        }
    }
}
