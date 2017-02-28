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
import com.edusoho.kuozhi.v3.model.bal.thread.CourseThreadEntity;
import com.edusoho.kuozhi.v3.model.bal.thread.CourseThreadPostEntity;
import com.edusoho.kuozhi.v3.model.sys.AudioCacheEntity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.AudioCacheUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.edusoho.kuozhi.v3.util.sql.CourseThreadDataSource;
import com.edusoho.kuozhi.v3.util.sql.CourseThreadPostDataSource;
import com.edusoho.kuozhi.v3.util.sql.SqliteChatUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.edusoho.kuozhi.v3.adapter.ClassroomDiscussAdapter.DiscussViewHolder;

/**
 * Created by JesseHuang on 15/12/23.
 */
public class ThreadDiscussAdapter extends ChatAdapter {
    protected CourseThreadEntity mCourseThreadModel;
    protected final CourseThreadDataSource mCourseThreadDataSource;
    protected final CourseThreadPostDataSource mCourseThreadPostDataSource;
    protected List<ThreadDiscussEntity> mList;

    private HashMap<Integer, Integer> mDurationArray;

    public ThreadDiscussAdapter(Context context) {
        mContext = context;
        mCourseThreadDataSource = new CourseThreadDataSource(SqliteChatUtil.getSqliteChatUtil(mContext, EdusohoApp.app.domain));
        mCourseThreadPostDataSource = new CourseThreadPostDataSource(SqliteChatUtil.getSqliteChatUtil(mContext, EdusohoApp.app.domain));
        mList = new ArrayList<>();
        mDownloadList = new HashMap<>();
        mDurationArray = new HashMap<>();
    }

    public ThreadDiscussAdapter(List<CourseThreadPostEntity> list, CourseThreadEntity courseThreadEntity, Context context) {
        this(context);
        int size = list.size();
        mCourseThreadModel = courseThreadEntity;
        //回复
        for (int i = 0; i < size; i++) {
            CourseThreadPostEntity courseThreadPostModel = list.get(i);
            ThreadDiscussEntity threadPostDiscussModel = new ThreadDiscussEntity(
                    courseThreadPostModel.pid,
                    courseThreadPostModel.threadId,
                    courseThreadPostModel.courseId,
                    courseThreadPostModel.lessonId,
                    courseThreadPostModel.user.id,
                    courseThreadPostModel.user.nickname,
                    courseThreadPostModel.user.mediumAvatar,
                    courseThreadPostModel.content,
                    courseThreadPostModel.type,
                    courseThreadPostModel.delivery,
                    courseThreadPostModel.createdTime);
            mList.add(threadPostDiscussModel);
        }
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).
                showImageForEmptyUri(R.drawable.default_avatar).
                showImageOnFail(R.drawable.default_avatar).build();
    }

    public List<ThreadDiscussEntity> getList() {
        return mList;
    }

    public void addItem(ThreadDiscussEntity model) {
        mList.add(model);
        notifyDataSetChanged();
    }

    public void addItems(ArrayList list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void updateItemState(int id, int state) {
        try {
            for (ThreadDiscussEntity tmpModel : mList) {
                if (tmpModel.id == id) {
                    tmpModel.delivery = state;
                    notifyDataSetChanged();
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("updateItemState", e.getMessage());
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int pos) {
        return 0;
    }

    @Override
    public ThreadDiscussEntity getItem(int pos) {
        return mList.get(pos);
    }

    public CourseThreadEntity getThread() {
        return mCourseThreadModel;
    }

    @Override
    public int getItemViewType(int pos) {
        ThreadDiscussEntity model = mList.get(pos);
        int msgType = -1;
        if (model.userId == EdusohoApp.app.loginUser.id) {
            switch (model.type) {
                case PushUtil.ChatMsgType.TEXT:
                    msgType = MSG_SEND_TEXT;
                    break;
                case PushUtil.ChatMsgType.IMAGE:
                    msgType = MSG_SEND_IMAGE;
                    break;
                case PushUtil.ChatMsgType.AUDIO:
                    msgType = MSG_SEND_AUDIO;
                    break;
            }
        } else {
            switch (model.type) {
                case PushUtil.ChatMsgType.TEXT:
                    msgType = MSG_RECEIVE_TEXT;
                    break;
                case PushUtil.ChatMsgType.IMAGE:
                    msgType = MSG_RECEIVE_IMAGE;
                    break;
                case PushUtil.ChatMsgType.AUDIO:
                    msgType = MSG_RECEIVE_AUDIO;
                    break;
            }
        }
        return msgType;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        DiscussViewHolder holder;
        int type = getItemViewType(pos);
        if (convertView == null) {
            convertView = createViewByType(type);
            holder = new ClassroomDiscussAdapter.DiscussViewHolder(convertView, type);
            convertView.setTag(holder);
        } else {
            holder = (DiscussViewHolder) convertView.getTag();
        }

        switch (type) {
            case MSG_SEND_TEXT:
                handleSendMsgText(holder, pos);
                break;
            case MSG_RECEIVE_TEXT:
                handleReceiveMsgText(holder, pos);
                break;
            case MSG_SEND_IMAGE:
                handlerSendImage(holder, pos);
                break;
            case MSG_RECEIVE_IMAGE:
                handlerReceiveImage(holder, pos);
                break;
            case MSG_SEND_AUDIO:
                handlerSendAudio(holder, pos);
                break;
            case MSG_RECEIVE_AUDIO:
                handlerReceiveAudio(holder, pos);
                break;
        }
        return convertView;
    }

    @Override
    protected void handleSendMsgText(ViewHolder holder, int pos) {
        final ThreadDiscussEntity model = mList.get(pos);
        holder.tvSendTime.setVisibility(View.GONE);
        setTimer(pos, holder.tvSendTime);
        holder.tvSendContent.setText(model.content);
        ImageLoader.getInstance().displayImage(EdusohoApp.app.loginUser.mediumAvatar, holder.ivAvatar, mOptions);
        switch (model.delivery) {
            case PushUtil.MsgDeliveryType.SUCCESS:
                holder.pbLoading.setVisibility(View.GONE);
                holder.ivStateError.setVisibility(View.GONE);
                break;
            case PushUtil.MsgDeliveryType.UPLOADING:
                holder.pbLoading.setVisibility(View.VISIBLE);
                holder.ivStateError.setVisibility(View.GONE);
                break;
            case PushUtil.MsgDeliveryType.FAILED:
                holder.pbLoading.setVisibility(View.GONE);
                holder.ivStateError.setVisibility(View.VISIBLE);
                holder.ivStateError.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //mImageErrorClick.sendMsgAgain(model);
                    }
                });
                break;
        }
    }

    @Override
    protected void handlerSendImage(final ViewHolder holder, final int pos) {
        final ThreadDiscussEntity model = mList.get(pos);
        setTimer(pos, holder.tvSendTime);
        switch (model.delivery) {
            case PushUtil.MsgDeliveryType.SUCCESS:
                holder.pbLoading.setVisibility(View.GONE);
                holder.ivStateError.setVisibility(View.GONE);
                break;
            case PushUtil.MsgDeliveryType.UPLOADING:
                holder.pbLoading.setVisibility(View.VISIBLE);
                holder.ivStateError.setVisibility(View.GONE);
                break;
            case PushUtil.MsgDeliveryType.FAILED:
                holder.pbLoading.setVisibility(View.GONE);
                holder.ivStateError.setVisibility(View.VISIBLE);
                holder.ivStateError.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mImageErrorClick != null) {
                            File file = new File(model.content);
                            if (file.exists()) {
                                model.delivery = PushUtil.MsgDeliveryType.UPLOADING;
                                holder.pbLoading.setVisibility(View.VISIBLE);
                                holder.ivStateError.setVisibility(View.GONE);
                                //mImageErrorClick.uploadMediaAgain(file, model, PushUtil.ChatMsgType.IMAGE, Const.MEDIA_IMAGE);
                            } else {
                                CommonUtil.longToast(mContext, "图片不存在，无法上传");
                            }
                        }
                    }
                });
                break;
        }
        AudioCacheEntity cache = AudioCacheUtil.getInstance().getAudioCacheByPath(model.content);
        if (cache != null) {
            String imageLocalPath = cache.localPath;
            ImageLoader.getInstance().displayImage("file://" + getThumbFromOriginalImagePath(imageLocalPath), holder.ivMsgImage, EdusohoApp.app.mOptions);
            holder.ivMsgImage.setOnClickListener(new ImageMsgClick(model.content));
        }
        ImageLoader.getInstance().displayImage(EdusohoApp.app.loginUser.mediumAvatar, holder.ivAvatar, mOptions);
    }

    @Override
    protected void handlerSendAudio(final ViewHolder holder, int pos) {
        final ThreadDiscussEntity model = mList.get(pos);
        setTimer(pos, holder.tvSendTime);
        ImageLoader.getInstance().displayImage(EdusohoApp.app.loginUser.mediumAvatar, holder.ivAvatar, mOptions);
        switch (model.delivery) {
            case PushUtil.MsgDeliveryType.SUCCESS:
                holder.ivStateError.setVisibility(View.GONE);
                holder.pbLoading.setVisibility(View.GONE);
                holder.tvAudioLength.setVisibility(View.VISIBLE);
                try {
                    int duration = 0;
                    if (!mDurationArray.containsKey(model.id)) {
                        duration = getMediaLength(model.content);
                        mDurationArray.put(model.id, duration);
                    }
                    duration = getDuration(mDurationArray.get(model.id));
                    holder.tvAudioLength.setText(duration + "\"");

                    holder.ivMsgImage.getLayoutParams().width = 100 + mDurationUnit * duration < mDurationMax ? 100 + mDurationUnit * duration : mDurationMax;
                    holder.ivMsgImage.requestLayout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                holder.ivMsgImage.setOnClickListener(
                        new AudioMsgClick(AudioCacheUtil.getInstance().getAudioCacheByPath(model.content).localPath, holder,
                                R.drawable.chat_to_speak_voice, R.drawable.chat_to_voice_play_anim));
                break;
            case PushUtil.MsgDeliveryType.UPLOADING:
                holder.pbLoading.setVisibility(View.VISIBLE);
                holder.ivStateError.setVisibility(View.GONE);
                holder.tvAudioLength.setVisibility(View.GONE);
                break;
            case PushUtil.MsgDeliveryType.FAILED:
                holder.pbLoading.setVisibility(View.GONE);
                holder.ivStateError.setVisibility(View.VISIBLE);
                holder.tvAudioLength.setVisibility(View.GONE);
                holder.ivStateError.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mImageErrorClick != null) {
                            File file = new File(model.content);
                            if (file.exists()) {
                                model.delivery = PushUtil.MsgDeliveryType.UPLOADING;
                                holder.pbLoading.setVisibility(View.VISIBLE);
                                holder.ivStateError.setVisibility(View.GONE);
                                //mImageErrorClick.uploadMediaAgain(file, model, PushUtil.ChatMsgType.AUDIO, Const.MEDIA_AUDIO);
                                notifyDataSetChanged();
                            } else {
                                CommonUtil.longToast(mContext, "音频不存在，无法上传");
                            }
                        }
                    }
                });
                break;
        }
    }

    protected void handleReceiveMsgText(DiscussViewHolder holder, int pos) {
        final ThreadDiscussEntity model = mList.get(pos);
        holder.tvSendTime.setVisibility(View.GONE);
        setTimer(pos, holder.tvSendTime);
        holder.tvNickname.setVisibility(View.VISIBLE);
        holder.tvNickname.setText(model.nickname);
        holder.tvSendContent.setText(model.content);
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
        holder.ivAvatar.setOnClickListener(new AvatarClickListener(mContext, model.userId));
    }

    protected void handlerReceiveImage(final DiscussViewHolder holder, final int pos) {
        final ThreadDiscussEntity model = mList.get(pos);
        final MyImageLoadingListener mMyImageLoadingListener = new MyImageLoadingListener(holder) {
            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                super.onLoadingComplete(s, view, bitmap);
                if (pos == 0) {
                    mCourseThreadDataSource.update(mCourseThreadModel);
                } else {
                    CourseThreadPostEntity postModel = mCourseThreadPostDataSource.getPost(model.id);
                    postModel.delivery = PushUtil.MsgDeliveryType.SUCCESS;
                    mCourseThreadPostDataSource.update(postModel);
                }
            }
        };
        setTimer(pos, holder.tvSendTime);
        holder.ivStateError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageLoader.getInstance().displayImage(model.content, holder.ivMsgImage, EdusohoApp.app.mOptions, mMyImageLoadingListener);
            }
        });
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
        holder.ivAvatar.setOnClickListener(new AvatarClickListener(mContext, model.userId));
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

    protected void handlerReceiveAudio(final DiscussViewHolder holder, int pos) {
        final ThreadDiscussEntity model = mList.get(pos);
        setTimer(pos, holder.tvSendTime);
        holder.tvNickname.setVisibility(View.VISIBLE);
        holder.tvNickname.setText(model.nickname);
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
        holder.ivAvatar.setOnClickListener(new AvatarClickListener(mContext, model.userId));
        switch (model.delivery) {
            case PushUtil.MsgDeliveryType.SUCCESS:
                holder.ivStateError.setVisibility(View.GONE);
                holder.pbLoading.setVisibility(View.GONE);
                holder.tvAudioLength.setVisibility(View.VISIBLE);
                String audioFileName = EdusohoApp.getChatCacheFile() + Const.UPLOAD_AUDIO_CACHE_FILE + "/" +
                        model.content.substring(model.content.lastIndexOf('/') + 1);
                if (AudioCacheUtil.getInstance().getAudioCache(audioFileName, model.content) == null) {
                    AudioCacheUtil.getInstance().create(new AudioCacheEntity(audioFileName, model.content));
                }
                try {
                    int duration = getDuration(getMediaLength(audioFileName));
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
    protected ArrayList<String> getCurrentImageUrls() {
        ArrayList<String> imagesUrls = new ArrayList<>();
        int size = mList.size();
        for (int i = 0; i < size; i++) {
            BaseMsgEntity entity = mList.get(i);
            if (entity.type.equals(PushUtil.ChatMsgType.IMAGE)) {
                imagesUrls.add(entity.content);
            }
        }
        return imagesUrls;
    }

    @Override
    public void updateVoiceDownloadStatus(long downId) {
        ThreadDiscussEntity model = null;
        try {
            for (ThreadDiscussEntity tmpModel : mList) {
                if (mDownloadList.get(downId).equals(tmpModel.id)) {
                    model = tmpModel;
                    break;
                }
            }
            if (model == null) {
                return;
            }
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downId);
            Cursor cursor = mDownloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                    String fileUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                    model.delivery = (TextUtils.isEmpty(fileUri) ? PushUtil.MsgDeliveryType.FAILED : PushUtil.MsgDeliveryType.SUCCESS);
                    cursor.close();
                } else if (DownloadManager.STATUS_FAILED == cursor.getInt(columnIndex)) {
                    model.delivery = PushUtil.MsgDeliveryType.FAILED;
                    cursor.close();
                }
            }
            CourseThreadPostEntity postModel = mCourseThreadPostDataSource.getPost(model.id);
            postModel.delivery = model.delivery;
            mCourseThreadPostDataSource.update(postModel);
            mDownloadList.remove(downId);
            notifyDataSetChanged();
        } catch (Exception ex) {
            Log.d("downloader", ex.toString());
            if (model != null) {
                model.delivery = PushUtil.MsgDeliveryType.FAILED;
            }
        }
    }

    private void setTimer(int pos, TextView tvTime) {
        ThreadDiscussEntity model = mList.get(pos);
        tvTime.setVisibility(View.GONE);
        if (pos > 0) {
            if (AppUtil.convertTimeZone2Millisecond(model.createdTime) - AppUtil.convertTimeZone2Millisecond(mList.get(pos - 1).createdTime) > TIME_INTERVAL) {
                tvTime.setVisibility(View.VISIBLE);
                tvTime.setText(AppUtil.convertTimeZone2Time(model.createdTime));
            }
        } else {
            tvTime.setVisibility(View.VISIBLE);
            tvTime.setText(AppUtil.convertTimeZone2Time(model.createdTime));
        }
    }

    public static class ThreadDiscussEntity extends BaseMsgEntity {
        public int postId;
        public int threadId;
        public int courseId;
        public int lessonId;
        public int userId;
        public String nickname;
        public String createdTime;

        public ThreadDiscussEntity() {

        }

        public ThreadDiscussEntity(
                int id,
                int threadId,
                int courseId,
                int lessonId,
                int userId,
                String nickname,
                String headImgUrl,
                String content,
                String type,
                int delivery,
                String createdTime) {
            this.id = id;
            this.threadId = threadId;
            this.courseId = courseId;
            this.lessonId = lessonId;
            this.userId = userId;
            this.nickname = nickname;
            this.headImgUrl = headImgUrl;
            this.content = content;
            this.type = type;
            this.delivery = delivery;
            this.createdTime = createdTime;
        }

    }

    private ThreadDiscussEntity convertThreadDiscuss(CourseThreadPostEntity courseThreadPostEntity) {
        return new ThreadDiscussEntity(
                courseThreadPostEntity.pid,
                courseThreadPostEntity.threadId,
                courseThreadPostEntity.courseId,
                courseThreadPostEntity.lessonId,
                courseThreadPostEntity.user.id,
                courseThreadPostEntity.user.nickname,
                courseThreadPostEntity.user.mediumAvatar,
                courseThreadPostEntity.content,
                courseThreadPostEntity.type,
                courseThreadPostEntity.delivery,
                courseThreadPostEntity.createdTime);
    }
}
