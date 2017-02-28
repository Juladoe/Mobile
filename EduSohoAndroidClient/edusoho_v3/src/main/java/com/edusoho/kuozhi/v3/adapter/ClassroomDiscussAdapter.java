package com.edusoho.kuozhi.v3.adapter;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.listener.AvatarClickListener;
import com.edusoho.kuozhi.v3.model.bal.push.BaseMsgEntity;
import com.edusoho.kuozhi.v3.model.bal.push.Chat;
import com.edusoho.kuozhi.v3.model.bal.push.ClassroomDiscussEntity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.edusoho.kuozhi.v3.util.sql.ClassroomDiscussDataSource;
import com.edusoho.kuozhi.v3.util.sql.SqliteChatUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by JesseHuang on 15/10/16.
 */
public class ClassroomDiscussAdapter<T extends Chat> extends ChatAdapter<T> {
    private ClassroomDiscussDataSource mClassroomDiscussDataSource;

    public ClassroomDiscussAdapter(ArrayList<T> list, Context context) {
        mList = list;
        mContext = context;
        mDownloadList = new HashMap<>();
        mClassroomDiscussDataSource = new ClassroomDiscussDataSource(SqliteChatUtil.getSqliteChatUtil(mContext, EdusohoApp.app.domain));
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).
                showImageForEmptyUri(R.drawable.default_avatar).
                showImageOnFail(R.drawable.default_avatar).build();
    }

    public void updateItemByChatId(ClassroomDiscussEntity model) {
        try {
            for (BaseMsgEntity tmpModel : mList) {
                if (((ClassroomDiscussEntity) tmpModel).discussId == model.discussId) {
                    tmpModel.delivery = model.delivery;
                    notifyDataSetChanged();
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("updateItemByDiscussId", e.getMessage());
        }
    }

    public void clear() {
        if (mList.size() > 0) {
            mList.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        Chat model =  mList.get(position);
        int type = -1;
        if (model.fromId == EdusohoApp.app.loginUser.id) {
            switch (model.type) {
                case PushUtil.ChatMsgType.TEXT:
                    type = MSG_SEND_TEXT;
                    break;
                case PushUtil.ChatMsgType.IMAGE:
                    type = MSG_SEND_IMAGE;
                    break;
                case PushUtil.ChatMsgType.AUDIO:
                    type = MSG_SEND_AUDIO;
                    break;
                case PushUtil.ChatMsgType.MULTI:
                    type = MSG_SEND_MULIT;
            }
        } else {
            switch (model.type) {
                case PushUtil.ChatMsgType.TEXT:
                    type = MSG_RECEIVE_TEXT;
                    break;
                case PushUtil.ChatMsgType.IMAGE:
                    type = MSG_RECEIVE_IMAGE;
                    break;
                case PushUtil.ChatMsgType.AUDIO:
                    type = MSG_RECEIVE_AUDIO;
                    break;
                case PushUtil.ChatMsgType.MULTI:
                    type = MSG_RECEIVE_MULIT;
            }
        }
        return type;
    }

    @Override
    public BaseMsgEntity getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DiscussViewHolder holder;
        int type = getItemViewType(position);
        if (convertView == null) {
            convertView = createViewByType(type);
            holder = new DiscussViewHolder(convertView, type);
            convertView.setTag(holder);
        } else {
            holder = (DiscussViewHolder) convertView.getTag();
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

    protected void handleReceiveMsgText(DiscussViewHolder holder, int position) {
        final Chat model = mList.get(position);
        holder.tvSendTime.setVisibility(View.GONE);
        if (position > 0) {
            if (model.createdTime - mList.get(position - 1).createdTime > TIME_INTERVAL) {
                holder.tvSendTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(AppUtil.convertMills2Date(((long) model.createdTime) * 1000));
            }
        } else {
            holder.tvSendTime.setVisibility(View.VISIBLE);
            holder.tvSendTime.setText(AppUtil.convertMills2Date(((long) model.createdTime) * 1000));
        }
        holder.tvNickname.setVisibility(View.VISIBLE);
        holder.tvNickname.setText(model.nickname);
        holder.tvSendContent.setText(model.content);
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
        holder.ivAvatar.setOnClickListener(new AvatarClickListener(mContext, model.fromId));
    }

    protected void handlerReceiveImage(final DiscussViewHolder holder, int position) {
        final Chat model =  mList.get(position);
        final MyImageLoadingListener mMyImageLoadingListener = new MyImageLoadingListener(holder) {
            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                super.onLoadingComplete(s, view, bitmap);
                model.delivery = PushUtil.MsgDeliveryType.SUCCESS;
               // mClassroomDiscussDataSource.update(model);
            }
        };
        if (position > 0) {
            if (model.createdTime - mList.get(position - 1).createdTime > TIME_INTERVAL) {
                holder.tvSendTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(AppUtil.convertMills2Date(((long) model.createdTime) * 1000));
            } else {
                holder.tvSendTime.setVisibility(View.GONE);
            }
        } else {
            holder.tvSendTime.setVisibility(View.VISIBLE);
            holder.tvSendTime.setText(AppUtil.convertMills2Date(((long) model.createdTime) * 1000));
        }
        holder.ivStateError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageLoader.getInstance().displayImage(model.content, holder.ivMsgImage, EdusohoApp.app.mOptions, mMyImageLoadingListener);
            }
        });
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
        holder.ivAvatar.setOnClickListener(new AvatarClickListener(mContext, model.fromId));
        holder.tvNickname.setVisibility(View.VISIBLE);
        holder.tvNickname.setText(model.nickname);
        File receiveImage = ImageLoader.getInstance().getDiskCache().get(model.content);
        holder.ivMsgImage.setOnClickListener(new ImageMsgClick(model.content));
        if (receiveImage.exists()) {
            String thumbImagePath = getThumbFromImageName(receiveImage.getName());
            File thumbImage = new File(thumbImagePath);
            if (thumbImage.exists()) {
                holder.pbLoading.setVisibility(View.GONE);
                holder.ivStateError.setVisibility(View.GONE);
                ImageLoader.getInstance().displayImage("file://" + thumbImagePath, holder.ivMsgImage);
                return;
            }
        }

        ImageLoader.getInstance().displayImage(model.content, holder.ivMsgImage, EdusohoApp.app.mOptions, mMyImageLoadingListener);
    }

    protected void handlerReceiveAudio(final DiscussViewHolder holder, int position) {
        final Chat model = mList.get(position);
        if (position > 0) {
            if (model.createdTime - mList.get(position - 1).createdTime > TIME_INTERVAL) {
                holder.tvSendTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(AppUtil.convertMills2Date(((long) model.createdTime) * 1000));
            } else {
                holder.tvSendTime.setVisibility(View.GONE);
            }
        } else {
            holder.tvSendTime.setVisibility(View.VISIBLE);
            holder.tvSendTime.setText(AppUtil.convertMills2Date(((long) model.createdTime) * 1000));
        }
        holder.tvNickname.setVisibility(View.VISIBLE);
        holder.tvNickname.setText(model.nickname);
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
        holder.ivAvatar.setOnClickListener(new AvatarClickListener(mContext, model.fromId));
        switch (model.delivery) {
            case PushUtil.MsgDeliveryType.SUCCESS:
                holder.ivStateError.setVisibility(View.GONE);
                holder.pbLoading.setVisibility(View.GONE);
                holder.tvAudioLength.setVisibility(View.VISIBLE);
                String audioFileName = EdusohoApp.getChatCacheFile() + Const.UPLOAD_AUDIO_CACHE_FILE + "/" +
                        model.content.substring(model.content.lastIndexOf('/') + 1);
                try {
                    int duration = getDuration(0);
                    holder.tvAudioLength.setText(duration + "\"");
                    holder.ivMsgImage.getLayoutParams().width = 100 + mDurationUnit * duration < mDurationMax ? 100 + mDurationUnit * duration : mDurationMax;
                    holder.ivMsgImage.requestLayout();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                holder.ivMsgImage.setOnClickListener(new AudioMsgClick(audioFileName, holder,
                        R.drawable.chat_from_speak_voice,
                        R.drawable.chat_from_voice_play_anim));
                break;
            case PushUtil.MsgDeliveryType.UPLOADING:
                holder.pbLoading.setVisibility(View.VISIBLE);
                holder.ivStateError.setVisibility(View.GONE);
                holder.tvAudioLength.setVisibility(View.GONE);
                downloadAudio(model.content, model.id);
                break;
            case PushUtil.MsgDeliveryType.FAILED:
                holder.pbLoading.setVisibility(View.GONE);
                holder.ivStateError.setVisibility(View.VISIBLE);
                holder.tvAudioLength.setVisibility(View.GONE);
                holder.ivStateError.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.pbLoading.setVisibility(View.VISIBLE);
                        holder.ivStateError.setVisibility(View.GONE);
                        downloadAudio(model.content, model.id);
                    }
                });
                break;
        }
    }

    @Override
    public void updateVoiceDownloadStatus(long downId) {
        Chat model = null;
        try {
            for (T tmp : mList) {
                Chat tmpModel =  tmp;
                if (mDownloadList.get(downId).equals(tmpModel.id)) {
                    model = tmpModel;
                    break;
                }
            }
            if (model == null) {
                return;
            }
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downId);
            Cursor c = mDownloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                    String fileUri = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                    model.delivery = (TextUtils.isEmpty(fileUri) ? PushUtil.MsgDeliveryType.FAILED : PushUtil.MsgDeliveryType.SUCCESS);
                    c.close();
                } else if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {
                    model.delivery = PushUtil.MsgDeliveryType.FAILED;
                    c.close();
                }
            }
            //mClassroomDiscussDataSource.update(model);
            mDownloadList.remove(downId);
            notifyDataSetChanged();
        } catch (Exception ex) {
            Log.d("downloader", ex.toString());
            if (model != null) {
                model.delivery = PushUtil.MsgDeliveryType.FAILED;
            }
        }
    }

    public static class DiscussViewHolder extends ViewHolder {
        public TextView tvNickname;

        public DiscussViewHolder(View view, int type) {
            super(view, type);
            if (type == MSG_RECEIVE_TEXT || type == MSG_RECEIVE_IMAGE || type == MSG_RECEIVE_AUDIO) {
                tvNickname = (TextView) view.findViewById(R.id.tv_nickname);
            }
        }
    }
}
