package com.edusoho.longinus.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.entity.message.Source;
import com.edusoho.kuozhi.imserver.ui.adapter.MessageRecyclerListAdapter;
import com.edusoho.kuozhi.imserver.util.TimeUtil;
import com.edusoho.longinus.R;
import com.edusoho.longinus.Utils;
import com.edusoho.longinus.data.LiveMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suju on 16/10/13.
 */
public class LiveChatListAdapter extends MessageRecyclerListAdapter {

    public LiveChatListAdapter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int position) {
        MessageEntity messageEntity = mMessageList.get(position);
        LiveMessageBody messageBody = new LiveMessageBody(messageEntity.getMsg());
        if (viewHolder instanceof LiveTextViewHolder) {
            LiveTextViewHolder liveTextViewHolder = ((LiveTextViewHolder) viewHolder);
            liveTextViewHolder.setLiveMessageBody(messageBody, position);
            liveTextViewHolder.setLiveAvatar(mMessageList.get(position));
            liveTextViewHolder.setUserRole(messageBody);
            liveTextViewHolder.setMessageStatus(messageEntity.getStatus());
            liveTextViewHolder.addViewClickListener(new ViewItemClickListener(position));
            return;
        }
        super.onBindViewHolder(viewHolder, position);
    }

    private boolean checkItemTypeIsLabel(MessageEntity messageEntity) {
        switch (messageEntity.getCmd()) {
            case "103004":
            case "103005":
            case "memberJoined":
                return true;
        }

        return false;
    }

    @Override
    public int getItemViewType(int position) {
        MessageEntity messageEntity = mMessageList.get(position);
        if (TextUtils.isEmpty(messageEntity.getCmd())) {
            return super.getItemViewType(position);
        }
        switch (messageEntity.getCmd()) {
            case "103004":
            case "103005":
                return LABEL;
        }
        LiveMessageBody messageBody = new LiveMessageBody(messageEntity.getMsg());
        if ("102001".equals(messageBody.getType())) {
            return mCurrentId == Utils.parseInt(messageEntity.getFromId()) ? SEND_TEXT : RECEIVE_TEXT;
        }
        return super.getItemViewType(position);
    }

    @Override
    protected View createTextView(boolean isSend) {
        if (isSend) {
            return LayoutInflater.from(mContext).inflate(R.layout.item_live_message_list_text_content, null);
        }
        return LayoutInflater.from(mContext).inflate(R.layout.item_live_message_list_receive_text_content, null);
    }

    @Override
    protected View createLabelView() {
        return LayoutInflater.from(mContext).inflate(R.layout.item_live_message_list_label_layout, null);
    }

    protected MessageViewHolder createViewHolder(int viewType, View contentView) {
        if (viewType == RECEIVE_TEXT || viewType == SEND_TEXT) {
            return new LiveTextViewHolder(contentView);
        }
        if (viewType == LABEL) {
            return new LiveLabelViewHolder(contentView);
        }
        return super.createViewHolder(viewType, contentView);
    }

    protected class LiveLabelViewHolder extends LabelViewHolder {

        public LiveLabelViewHolder(View view) {
            super(view);
        }

        @Override
        public void setMessageBody(MessageBody messageBody, int position) {
            try {
                LiveMessageBody liveMessageBody = new LiveMessageBody(mMessageList.get(position).getMsg());
                JSONObject jsonObject = new JSONObject(liveMessageBody.getData());
                switch (liveMessageBody.getType()) {
                    case "103004":
                        mContentView.setVisibility(View.VISIBLE);
                        if (jsonObject.optBoolean("isCanChat")) {
                            mContentView.setText(String.format("管理员将 %s 解除禁言", jsonObject.optString("clientName")));
                        } else {
                            mContentView.setText(String.format("管理员将 %s 禁言", jsonObject.optString("clientName")));
                        }
                        break;
                    case "103005":
                        mContentView.setVisibility(View.VISIBLE);
                        if (jsonObject.optBoolean("isAllCanChat")) {
                            mContentView.setText("管理员将全体解除禁言");
                        } else {
                            mContentView.setText("管理员将全体禁言");
                        }
                        break;
                }
            } catch (JSONException e) {
            }
        }
    }

    protected class LiveTextViewHolder extends TextViewHolder {

        private TextView mRoleView;

        public LiveTextViewHolder(View view) {
            super(view);
            mRoleView = (TextView) view.findViewById(R.id.tv_role_label);
            nicknameView.setVisibility(View.VISIBLE);
        }

        protected void setLiveAvatar(MessageEntity messageEntity) {
            MessageBody messageBody = new MessageBody(messageEntity);
            messageBody.setSource(new Source(Utils.parseInt(messageEntity.getFromId()), Destination.USER));
            super.setAvatar(messageBody);
        }

        public void setUserRole(LiveMessageBody messageBody) {
            mRoleView.setVisibility(View.GONE);
            if ("master".equals(messageBody.getRole())) {
                mRoleView.setText("管理员");
                mRoleView.setVisibility(View.VISIBLE);
                mRoleView.setBackground(mContext.getResources().getDrawable(R.drawable.chat_role_label_bg));
            } else if ("teacher".equals(messageBody.getRole())) {
                mRoleView.setText("讲师");
                mRoleView.setVisibility(View.VISIBLE);
                mRoleView.setBackground(mContext.getResources().getDrawable(R.drawable.chat_role_label_yellow_bg));
            } else if ("assist".equals(messageBody.getRole())) {
                mRoleView.setText("老师");
                mRoleView.setVisibility(View.VISIBLE);
                mRoleView.setBackground(mContext.getResources().getDrawable(R.drawable.chat_role_label_yellow_bg));
            } else if ("support".equals(messageBody.getRole())) {
                mRoleView.setText("技术支持");
                mRoleView.setVisibility(View.VISIBLE);
                mRoleView.setBackground(mContext.getResources().getDrawable(R.drawable.chat_role_label_red_bg));
            }
        }

        protected void setMessageStatus(int status) {
            switch (status) {
                case MessageEntity.StatusType.SUCCESS:
                    errorStatusView.setVisibility(View.INVISIBLE);
                    break;
                case MessageEntity.StatusType.UPLOADING:
                    errorStatusView.setVisibility(View.VISIBLE);
                    errorStatusView.setProgressStatus();
                    break;
                case MessageEntity.StatusType.FAILED:
                    errorStatusView.setVisibility(View.VISIBLE);
                    errorStatusView.setErrorStatus();
            }
        }

        public void addViewClickListener(ViewItemClickListener onClickListener) {
            errorStatusView.setOnClickListener(onClickListener);
        }

        public void setLiveMessageBody(LiveMessageBody messageBody, int position) {
            String body = "";
            try {
                JSONObject jsonObject = new JSONObject(messageBody.getData());
                body = jsonObject.optString("info");
            } catch (JSONException je) {
            }
            mContentView.setText(body);
            nicknameView.setText(messageBody.getClientName());

            timeView.setVisibility(View.GONE);
            if (position < (getItemCount() - 1)) {
                MessageEntity messageEntity = mMessageList.get(position + 1);
                long preTime = messageEntity.getTime() * 1000L;
                if (checkItemTypeIsLabel(messageEntity) || messageBody.getTime() - preTime > TIME_INTERVAL) {
                    timeView.setVisibility(View.VISIBLE);
                    timeView.setText(TimeUtil.convertMills2Date(messageBody.getTime()));
                }
                return;
            }
            timeView.setVisibility(View.VISIBLE);
            timeView.setText(TimeUtil.convertMills2Date(messageBody.getTime()));
        }
    }
}
