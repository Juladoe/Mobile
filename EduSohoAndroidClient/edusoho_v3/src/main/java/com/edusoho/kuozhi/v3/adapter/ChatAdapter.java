package com.edusoho.kuozhi.v3.adapter;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.listener.ChatDownloadListener;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.push.BaseMsgEntity;
import com.edusoho.kuozhi.v3.model.bal.push.Chat;
import com.edusoho.kuozhi.v3.model.bal.push.RedirectBody;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.trinea.android.common.util.DigestUtils;
import cn.trinea.android.common.util.FileUtils;

/**
 * Created by JesseHuang on 15/6/3.
 * Chat ListView Adapter
 */
public class ChatAdapter<T extends Chat> extends BaseAdapter implements ChatDownloadListener {

    protected Context mContext;
    protected ArrayList<T> mList;
    protected HashMap<Long, Integer> mDownloadList;
    protected MediaPlayer mMediaPlayer;
    protected ImageView mPrevSpeakImageView;
    protected int mPrevImageViewBg;
    protected ImageErrorClick mImageErrorClick;

    protected int mDurationMax = EdusohoApp.screenW / 2;
    protected int mDurationUnit = EdusohoApp.screenW / 40;
    protected DisplayImageOptions mOptions;
    protected String mCurrentAudioPath;
    protected AnimationDrawable mAnimDrawable;

    protected static long TIME_INTERVAL = 60 * 5 * 1000;
    protected static final int TYPE_COUNT = 8;
    protected static final int MSG_SEND_TEXT = 0;
    protected static final int MSG_RECEIVE_TEXT = 1;
    protected static final int MSG_SEND_IMAGE = 2;
    protected static final int MSG_RECEIVE_IMAGE = 3;
    protected static final int MSG_SEND_AUDIO = 4;
    protected static final int MSG_RECEIVE_AUDIO = 5;
    protected static final int MSG_SEND_MULIT = 6;
    protected static final int MSG_RECEIVE_MULIT = 7;

    public void setSendImageClickListener(ImageErrorClick imageErrorClick) {
        mImageErrorClick = imageErrorClick;
    }

    public void addItems(ArrayList<T> list) {
        mList.addAll(0, list);
        notifyDataSetChanged();
    }

    public ChatAdapter() {
    }

    public ChatAdapter(Context ctx, ArrayList<T> list) {
        mContext = ctx;
        mList = list;
        mDownloadList = new HashMap<>();
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).
                showImageForEmptyUri(R.drawable.default_avatar).
                showImageOnFail(R.drawable.default_avatar).build();
    }

    public void addItem(T chat) {
        mList.add(chat);
        notifyDataSetChanged();
    }

    public void updateItemByMsgId(Chat chat) {
        try {
            Chat tmpChat = null;
            for (BaseMsgEntity entity : mList) {
                tmpChat = (Chat) entity;
                if (tmpChat.mid != null && tmpChat.mid.equals(chat.mid)) {
                    tmpChat.delivery = chat.delivery;
                    notifyDataSetChanged();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        if (mList.size() > 0) {
            mList.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        Chat msg = (Chat) mList.get(position);
        int type = -1;
        if (msg.direct == Chat.Direct.SEND) {
            switch (msg.type) {
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
            switch (msg.type) {
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
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);
        if (convertView == null) {
            convertView = createViewByType(type);
            holder = new ViewHolder(convertView, type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
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

    protected void handlerMultiMsg(ViewHolder holder, int position) {
        BaseMsgEntity model = mList.get(position);
        holder.tvSendTime.setVisibility(View.GONE);
        if (position > 0) {
            if (model.createdTime - mList.get(position - 1).createdTime > TIME_INTERVAL) {
                holder.tvSendTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
            }
        } else {
            holder.tvSendTime.setVisibility(View.VISIBLE);
            holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
        }

        final RedirectBody body = EdusohoApp.app.parseJsonValue(model.content, new TypeToken<RedirectBody>() {
        });
        holder.multiBodyContent.setText(body.content);
        holder.multiBodyTitle.setText(body.title);
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
        ImageLoader.getInstance().displayImage(body.image, holder.multiBodyIcon, mOptions);

        holder.multiBodyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CoreEngine.create(mContext).runNormalPlugin("WebViewActivity", mContext, new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(Const.WEB_URL, body.url);
                    }
                });
            }
        });
    }

    protected void handleSendMsgMulti(ViewHolder holder, int position) {
        final Chat model = mList.get(position);
        switch (model.delivery) {
            case MessageEntity.StatusType.SUCCESS:
                holder.pbLoading.setVisibility(View.GONE);
                holder.ivStateError.setVisibility(View.GONE);
                break;
            case MessageEntity.StatusType.UPLOADING:
                holder.pbLoading.setVisibility(View.VISIBLE);
                holder.ivStateError.setVisibility(View.GONE);
                break;
            case MessageEntity.StatusType.FAILED:
                holder.pbLoading.setVisibility(View.GONE);
                holder.ivStateError.setVisibility(View.VISIBLE);
                holder.ivStateError.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageErrorClick.sendMsgAgain(model);
                    }
                });
                break;
        }
    }

    protected void handleSendMsgText(ViewHolder holder, int position) {
        final Chat model = mList.get(position);
        holder.tvSendTime.setVisibility(View.GONE);
        if (position > 0) {
            if (model.createdTime - mList.get(position - 1).createdTime > TIME_INTERVAL) {
                holder.tvSendTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
            }
        } else {
            holder.tvSendTime.setVisibility(View.VISIBLE);
            holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
        }
        holder.tvSendContent.setText(model.content);
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
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
                        mImageErrorClick.sendMsgAgain(model);
                    }
                });
                break;
        }
    }

    protected void handleReceiveMsgText(ViewHolder holder, int position) {
        final BaseMsgEntity model = mList.get(position);
        holder.tvSendTime.setVisibility(View.GONE);
        if (position > 0) {
            if (model.createdTime - mList.get(position - 1).createdTime > TIME_INTERVAL) {
                holder.tvSendTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
            }
        } else {
            holder.tvSendTime.setVisibility(View.VISIBLE);
            holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
        }
        holder.tvSendContent.setText(model.content);
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
    }


    protected void handlerSendImage(final ViewHolder holder, int position) {
        final Chat model = mList.get(position);
        if (position > 0) {
            if (model.createdTime - mList.get(position - 1).createdTime > TIME_INTERVAL) {
                holder.tvSendTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
            } else {
                holder.tvSendTime.setVisibility(View.GONE);
            }
        } else {
            holder.tvSendTime.setVisibility(View.VISIBLE);
            holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
        }
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
                                mImageErrorClick.uploadMediaAgain(file, model, PushUtil.ChatMsgType.IMAGE, Const.MEDIA_IMAGE);
                            } else {
                                CommonUtil.longToast(mContext, "图片不存在，无法上传");
                            }
                        }
                    }
                });
                break;
        }
        holder.ivMsgImage.setOnClickListener(new ImageMsgClick(model.content));
        ImageLoader.getInstance().displayImage("file://" + getThumbFromOriginalImagePath(model.content), holder.ivMsgImage, EdusohoApp.app.mOptions);
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
    }

    protected void handlerReceiveImage(final ViewHolder holder, int position) {
        final BaseMsgEntity model = mList.get(position);
        final MyImageLoadingListener mMyImageLoadingListener = new MyImageLoadingListener(holder) {
            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                super.onLoadingComplete(s, view, bitmap);
                model.delivery = PushUtil.MsgDeliveryType.SUCCESS;
            }
        };
        if (position > 0) {
            if (model.createdTime - mList.get(position - 1).createdTime > TIME_INTERVAL) {
                holder.tvSendTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
            } else {
                holder.tvSendTime.setVisibility(View.GONE);
            }
        } else {
            holder.tvSendTime.setVisibility(View.VISIBLE);
            holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
        }
        holder.ivStateError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageLoader.getInstance().displayImage(model.content, holder.ivMsgImage, EdusohoApp.app.mOptions, mMyImageLoadingListener);
            }
        });
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);

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

    private JSONObject getAudioContentFromString(String content) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(content);
        } catch (JSONException e) {
        }

        return jsonObject;
    }

    protected void handlerSendAudio(final ViewHolder holder, int position) {
        final Chat model = mList.get(position);
        if (position > 0) {
            if (model.createdTime - mList.get(position - 1).createdTime > TIME_INTERVAL) {
                holder.tvSendTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
            } else {
                holder.tvSendTime.setVisibility(View.GONE);
            }
        } else {
            holder.tvSendTime.setVisibility(View.VISIBLE);
            holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
        }
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
        final JSONObject audioJsonObject = getAudioContentFromString(model.content);
        switch (model.delivery) {
            case PushUtil.MsgDeliveryType.SUCCESS:
                holder.ivStateError.setVisibility(View.GONE);
                holder.pbLoading.setVisibility(View.GONE);
                holder.tvAudioLength.setVisibility(View.VISIBLE);
                try {
                    int duration = getDuration(audioJsonObject.optInt("duration"));
                    holder.tvAudioLength.setText(duration + "\"");

                    holder.ivMsgImage.getLayoutParams().width = 100 + mDurationUnit * duration < mDurationMax ? 100 + mDurationUnit * duration : mDurationMax;
                    holder.ivMsgImage.requestLayout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                holder.ivMsgImage.setOnClickListener(new AudioMsgClick(audioJsonObject.optString("file"), holder, R.drawable.chat_to_speak_voice, R.drawable.chat_to_voice_play_anim));
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
                            File file = new File(audioJsonObject.optString("file"));
                            if (file.exists()) {
                                model.delivery = PushUtil.MsgDeliveryType.UPLOADING;
                                holder.pbLoading.setVisibility(View.VISIBLE);
                                holder.ivStateError.setVisibility(View.GONE);
                                mImageErrorClick.uploadMediaAgain(file, model, PushUtil.ChatMsgType.AUDIO, Const.MEDIA_AUDIO);
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

    protected void handlerReceiveAudio(final ViewHolder holder, int position) {
        final Chat model = mList.get(position);
        if (position > 0) {
            if (model.createdTime - mList.get(position - 1).createdTime > TIME_INTERVAL) {
                holder.tvSendTime.setVisibility(View.VISIBLE);
                holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
            } else {
                holder.tvSendTime.setVisibility(View.GONE);
            }
        } else {
            holder.tvSendTime.setVisibility(View.VISIBLE);
            holder.tvSendTime.setText(AppUtil.convertMills2Date(model.createdTime));
        }
        ImageLoader.getInstance().displayImage(model.headImgUrl, holder.ivAvatar, mOptions);
        final JSONObject audioJsonObject = getAudioContentFromString(model.content);
        switch (model.delivery) {
            case PushUtil.MsgDeliveryType.SUCCESS:
                holder.ivStateError.setVisibility(View.GONE);
                holder.pbLoading.setVisibility(View.GONE);
                holder.tvAudioLength.setVisibility(View.VISIBLE);
                String audioFileName = AppUtil.getAppCacheDir() + Const.UPLOAD_AUDIO_CACHE_FILE + "/" +
                        DigestUtils.md5(model.content);
                try {
                    int duration = getDuration(audioJsonObject.optInt("duration"));
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
                downloadAudio(audioJsonObject.optString("file"), model.id);
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
                        downloadAudio(audioJsonObject.optString("file"), model.id);
                    }
                });
                break;
        }
    }

    protected DownloadManager mDownloadManager;

    protected void downloadAudio(String url, int id) {
        if (mDownloadManager == null) {
            mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        Uri uri = Uri.parse(url);
        String filename = DigestUtils.md5(url) + ".mp3";
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setVisibleInDownloadsUi(false);
        //request.setDestinationUri(Uri.fromFile(new File(AppUtil.getAppCacheDir() + Const.UPLOAD_AUDIO_CACHE_FILE + "/", filename)));
        long downloadId = mDownloadManager.enqueue(request);
        mDownloadList.put(downloadId, id);
    }

    /**
     * 获取amr播放长度
     *
     * @return 音频长度
     */
    protected int getDuration(int duration) {
        return (int) Math.ceil(Float.valueOf(duration) / 1000);
    }

    protected int getMediaLength(String path) {
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Uri.parse(path));
        if (mediaPlayer == null) {
            return 0;
        }

        int duration = mediaPlayer.getDuration();
        mediaPlayer.release();
        mediaPlayer = null;

        return duration;
    }

    /**
     * 获取缩略图文件路径
     *
     * @param imagePath 文件路径
     * @return 文件路径
     */
    protected String getThumbFromOriginalImagePath(String imagePath) {
        return imagePath.replace(Const.UPLOAD_IMAGE_CACHE_FILE, Const.UPLOAD_IMAGE_CACHE_THUMB_FILE);
    }

    protected String getThumbFromImageName(String imageName) {
        return EdusohoApp.getChatCacheFile() + Const.UPLOAD_IMAGE_CACHE_THUMB_FILE + "/" + imageName;
    }

    public class ImageMsgClick implements View.OnClickListener {
        private String mImageUrl;

        public ImageMsgClick(String url) {
            mImageUrl = url;
        }

        @Override
        public void onClick(View v) {
            ArrayList<String> imageUrls = getCurrentImageUrls();
            int index = 0;
            int size = imageUrls.size();
            for (int i = 0; i < size; i++) {
                if ((imageUrls.get(i).equals(mImageUrl))) {
                    index = i;
                    break;
                }
            }
            Bundle bundle = new Bundle();
            bundle.putInt("index", index);
            bundle.putStringArrayList("imageList", imageUrls);
            EdusohoApp.app.mEngine.runNormalPluginWithBundle("ViewPagerActivity", mContext, bundle);
        }
    }

    public class AudioMsgClick implements View.OnClickListener {
        private File mAudioFile;
        private ViewHolder holder;
        private int mChatSpeakResId;
        private int mChatSpeakAnimResId;

        public AudioMsgClick(String filePath, ViewHolder h, int resId, int animResId) {
            mAudioFile = new File(filePath);
            holder = h;
            mChatSpeakResId = resId;
            mChatSpeakAnimResId = animResId;
        }

        @Override
        public void onClick(View v) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                if (mCurrentAudioPath.equals(mAudioFile.getPath())) {
                    mMediaPlayer.stop();
                    stopVoiceAnim(holder, mChatSpeakResId);
                    return;
                } else {
                    mMediaPlayer.stop();
                }
            }
            if (mAudioFile != null && mAudioFile.exists()) {
                if (mMediaPlayer == null) {
                    mMediaPlayer = MediaPlayer.create(mContext, Uri.parse(mAudioFile.getPath()));
                    mMediaPlayer.getDuration();
                } else {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                        mMediaPlayer.reset();
                        mMediaPlayer.release();
                    }
                    mMediaPlayer = null;
                    mMediaPlayer = MediaPlayer.create(mContext, Uri.parse(mAudioFile.getPath()));
                }
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopVoiceAnim(holder, mChatSpeakResId);
                        mPrevSpeakImageView = null;
                        mPrevImageViewBg = 0;
                    }
                });
                //判断之前Speaker动画效果是否start，是则需要先Stop，再设置之前Speaker的background
                if (mPrevSpeakImageView != null && mPrevSpeakImageView.getBackground() instanceof AnimationDrawable) {
                    ((AnimationDrawable) mPrevSpeakImageView.getBackground()).stop();
                    mPrevSpeakImageView.setBackgroundResource(mPrevImageViewBg);
                }
                mMediaPlayer.start();
                //记住当前Speaker状态
                mPrevSpeakImageView = holder.ivVoiceAnim;
                mPrevImageViewBg = mChatSpeakResId;
                startVoiceAnim(holder, mChatSpeakAnimResId);
                mCurrentAudioPath = mAudioFile.getPath();
            } else {
                CommonUtil.shortToast(mContext, mContext.getString(R.string.cache_audio_does_not_exist));
            }
        }
    }

    protected void startVoiceAnim(ViewHolder holder, int resId) {
        if (holder.ivVoiceAnim.getBackground() instanceof AnimationDrawable) {
            mAnimDrawable = (AnimationDrawable) holder.ivVoiceAnim.getBackground();
            mAnimDrawable.stop();
            mAnimDrawable.start();
        } else {
            holder.ivVoiceAnim.setBackgroundResource(resId);
            mAnimDrawable = (AnimationDrawable) holder.ivVoiceAnim.getBackground();
            mAnimDrawable.start();
        }
    }

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

    protected void stopVoiceAnim(ViewHolder holder, int resId) {
        if (mAnimDrawable != null) {
            mAnimDrawable.stop();
            holder.ivVoiceAnim.setBackgroundResource(resId);
        }
    }

    public class MyImageLoadingListener implements ImageLoadingListener {
        private ViewHolder holder;

        public MyImageLoadingListener(ViewHolder h) {
            this.holder = h;
        }

        @Override
        public void onLoadingStarted(String s, View view) {
            holder.pbLoading.setVisibility(View.VISIBLE);
            holder.ivStateError.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingFailed(String s, View view, FailReason failReason) {
            holder.pbLoading.setVisibility(View.GONE);
            holder.ivStateError.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
            //分辨率压缩并缓存
            if (bitmap.getWidth() > EdusohoApp.screenW * 0.4f) {
                bitmap = AppUtil.scaleImage(bitmap, EdusohoApp.screenW * 0.4f, 0);
            }
            File receiveFile = ImageLoader.getInstance().getDiskCache().get(s);
            try {
                AppUtil.convertBitmap2File(bitmap, AppUtil.getAppStorage() + Const.UPLOAD_IMAGE_CACHE_THUMB_FILE + "/" + receiveFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }

            holder.pbLoading.setVisibility(View.GONE);
            holder.ivStateError.setVisibility(View.GONE);
            ((ImageView) view).setImageBitmap(bitmap);
        }

        @Override
        public void onLoadingCancelled(String s, View view) {

        }
    }

    protected View createViewByType(int type) {
        View convertView = null;
        switch (type) {
            case MSG_SEND_TEXT:
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout_msg_send_text, null);
                break;
            case MSG_RECEIVE_TEXT:
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout_msg_receive_text, null);
                break;
            case MSG_SEND_IMAGE:
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout_msg_send_image, null);
                break;
            case MSG_RECEIVE_IMAGE:
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout_msg_receive_image, null);
                break;
            case MSG_SEND_AUDIO:
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout_msg_send_audio, null);
                break;
            case MSG_RECEIVE_AUDIO:
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout_msg_receive_audio, null);
                break;
            case MSG_SEND_MULIT:
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout_msg_send_multi, null);
                break;
            case MSG_RECEIVE_MULIT:
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout_msg_receive_multi, null);
                break;
        }
        return convertView;
    }

    public static class ViewHolder {
        public TextView tvSendTime;
        public TextView tvSendContent;
        public ImageView ivAvatar;
        public ImageView ivMsgImage;
        public ProgressBar pbLoading;
        public ImageView ivStateError;
        public TextView tvAudioLength;
        public ImageView ivVoiceAnim;

        public TextView multiBodyTitle;
        public TextView multiBodyContent;
        public ImageView multiBodyIcon;
        public View multiBodyLayout;

        public ViewHolder(View view, int type) {
            switch (type) {
                case MSG_SEND_TEXT:
                    tvSendTime = (TextView) view.findViewById(R.id.tv_send_time);
                    tvSendContent = (TextView) view.findViewById(R.id.tv_send_content);
                    ivAvatar = (ImageView) view.findViewById(R.id.ci_send_pic);
                    pbLoading = (ProgressBar) view.findViewById(R.id.sendProgressPar);
                    ivStateError = (ImageView) view.findViewById(R.id.msg_status);
                    break;
                case MSG_RECEIVE_TEXT:
                    tvSendTime = (TextView) view.findViewById(R.id.tv_send_time);
                    tvSendContent = (TextView) view.findViewById(R.id.tv_send_content);
                    ivAvatar = (ImageView) view.findViewById(R.id.ci_send_pic);
                    break;
                case MSG_SEND_IMAGE:
                case MSG_RECEIVE_IMAGE:
                    tvSendTime = (TextView) view.findViewById(R.id.tv_send_time);
                    tvSendContent = (TextView) view.findViewById(R.id.tv_send_content);
                    ivAvatar = (ImageView) view.findViewById(R.id.ci_send_pic);
                    ivMsgImage = (ImageView) view.findViewById(R.id.iv_msg_image);
                    pbLoading = (ProgressBar) view.findViewById(R.id.sendProgressPar);
                    ivStateError = (ImageView) view.findViewById(R.id.msg_status);
                    break;
                case MSG_SEND_AUDIO:
                case MSG_RECEIVE_AUDIO:
                    tvSendTime = (TextView) view.findViewById(R.id.tv_send_time);
                    ivAvatar = (ImageView) view.findViewById(R.id.ci_send_pic);
                    ivMsgImage = (ImageView) view.findViewById(R.id.iv_msg_image);
                    pbLoading = (ProgressBar) view.findViewById(R.id.sendProgressPar);
                    ivStateError = (ImageView) view.findViewById(R.id.msg_status);
                    tvAudioLength = (TextView) view.findViewById(R.id.tv_audio_length);
                    ivVoiceAnim = (ImageView) view.findViewById(R.id.iv_voice_play_anim);
                    break;
                case MSG_SEND_MULIT:
                    pbLoading = (ProgressBar) view.findViewById(R.id.sendProgressPar);
                    ivStateError = (ImageView) view.findViewById(R.id.msg_status);
                case MSG_RECEIVE_MULIT:
                    multiBodyLayout = view.findViewById(R.id.chat_multi_body);
                    ivAvatar = (ImageView) view.findViewById(R.id.ci_send_pic);
                    tvSendTime = (TextView) view.findViewById(R.id.tv_send_time);
                    multiBodyContent = (TextView) view.findViewById(R.id.chat_multi_content);
                    multiBodyTitle = (TextView) view.findViewById(R.id.chat_multi_title);
                    multiBodyIcon = (ImageView) view.findViewById(R.id.chat_multi_icon);
                    break;
            }
        }
    }

    public interface ImageErrorClick {
        void uploadMediaAgain(File file, Chat chat, String type, String strType);

        void sendMsgAgain(Chat chat);
    }

    @Override
    public HashMap<Long, Integer> getDownloadList() {
        return mDownloadList;
    }

    @Override
    public void updateVoiceDownloadStatus(long downId) {
        Chat downloadChat = null;
        try {
            for (T tmp : mList) {
                Chat tmpChat = (Chat) tmp;
                if (mDownloadList.get(downId).equals(tmpChat.id)) {
                    downloadChat = tmpChat;
                    break;
                }
            }
            if (downloadChat == null) {
                return;
            }
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downId);
            Cursor c = mDownloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                    String fileUri = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                    c.close();
                    if (moveDownloadToCache(fileUri, downloadChat.content)) {
                        downloadChat.delivery = (TextUtils.isEmpty(fileUri) ? MessageEntity.StatusType.FAILED : MessageEntity.StatusType.SUCCESS);
                    }
                } else if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {
                    downloadChat.delivery = MessageEntity.StatusType.FAILED;
                    c.close();
                }
            }

            ContentValues cv = new ContentValues();
            cv.put("status", downloadChat.delivery);
            IMClient.getClient().getMessageManager().updateMessageFieldByMsgNo(downloadChat.msgNo, cv);
            mDownloadList.remove(downId);
            notifyDataSetChanged();
        } catch (Exception ex) {
            Log.d("downloader", ex.toString());
            if (downloadChat != null) {
                downloadChat.delivery = MessageEntity.StatusType.FAILED;
                notifyDataSetChanged();
            }
        }
    }

    private boolean moveDownloadToCache(String downloadFileUri, String fileUrl) {
        String filePath = AppUtil.getPath(mContext, Uri.parse(downloadFileUri));
        String destFilePath = AppUtil.getAppCacheDir() + Const.UPLOAD_AUDIO_CACHE_FILE + "/" +
                DigestUtils.md5(fileUrl);
        return FileUtils.copyFile(filePath, destFilePath);
    }
}
