package com.edusoho.kuozhi.imserver.ui.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.R;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.entity.message.Source;
import com.edusoho.kuozhi.imserver.ui.entity.AudioBody;
import com.edusoho.kuozhi.imserver.ui.entity.Direct;
import com.edusoho.kuozhi.imserver.ui.entity.PushUtil;
import com.edusoho.kuozhi.imserver.ui.helper.MessageHelper;
import com.edusoho.kuozhi.imserver.ui.listener.AudioPlayStatusListener;
import com.edusoho.kuozhi.imserver.ui.listener.MessageListItemController;
import com.edusoho.kuozhi.imserver.ui.util.AudioUtil;
import com.edusoho.kuozhi.imserver.ui.util.ImageCache;
import com.edusoho.kuozhi.imserver.ui.util.MaskBitmap;
import com.edusoho.kuozhi.imserver.ui.util.ResourceDownloadTask;
import com.edusoho.kuozhi.imserver.ui.view.ChatImageView;
import com.edusoho.kuozhi.imserver.ui.view.MessageStatusView;
import com.edusoho.kuozhi.imserver.util.MessageUtil;
import com.edusoho.kuozhi.imserver.util.TimeUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suju on 16/8/26.
 */
public class MessageListAdapter extends BaseAdapter {

    private static final int SEND = 0X01;
    private static final int RECEIVE = 0X02;

    private static final int RECEIVE_TEXT = 0;
    private static final int SEND_TEXT = 1;
    private static final int RECEIVE_AUDIO = 2;
    private static final int SEND_AUDIO = 3;
    private static final int RECEIVE_IMAGE = 4;
    private static final int SEND_IMAGE = 5;
    private static final int RECEIVE_MULTI = 6;
    private static final int SEND_MULTI = 7;

    protected static long TIME_INTERVAL = 60 * 5 * 1000;
    protected static final String TAG = "MessageListAdapter";

    private int mCurrentId;
    private MessageHelper mMessageHelper;
    private MessageListItemController mMessageListItemController;

    protected Context mContext;
    protected List<MessageEntity> mMessageList;
    protected DisplayImageOptions mOptions;
    protected SparseArray<MessageBody> mIndexArray;

    public MessageListAdapter(Context context) {
        this.mContext = context;
        this.mMessageList = new ArrayList<>();
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).
                showImageForEmptyUri(R.drawable.message_image_default).
                showImageOnFail(R.drawable.message_image_default).build();

        this.mMessageHelper = new MessageHelper(mContext);
        this.mIndexArray = new SparseArray<>();
    }

    public MessageListAdapter(Context context, int currentId) {
        this(context);
        this.mCurrentId = currentId;
    }

    public void removeItem(int id) {
        int size = mMessageList.size();
        for (int i = 0; i < size; i++) {
            if (mMessageList.get(i).getId() == id) {
                mMessageList.remove(i);
                notifyDataSetChanged();
                return;
            }
        }
    }

    public void updateItem(MessageEntity messageEntity) {
        int size = mMessageList.size();
        for (int i = 0; i < size; i++) {
            if (mMessageList.get(i).getId() == messageEntity.getId()) {
                mMessageList.remove(i);
                mMessageList.add(i, messageEntity);
                notifyDataSetChanged();
                return;
            }
        }
    }

    private void updateViewByPosition(int position, MessageEntity messageEntity) {

    }

    public void setCurrentId(int currentId) {
        this.mCurrentId = currentId;
    }

    @Override
    public int getViewTypeCount() {
        return 8;
    }

    @Override
    public int getItemViewType(int position) {
        MessageBody messageBody = new MessageBody(mMessageList.get(position));
        String type = messageBody.getType();
        boolean isSend = messageBody.getSource().getId() == mCurrentId;
        switch (type) {
            case PushUtil.ChatMsgType.TEXT:
                return isSend ? SEND_TEXT : RECEIVE_TEXT;
            case PushUtil.ChatMsgType.AUDIO:
                return isSend ? SEND_AUDIO : RECEIVE_AUDIO;
            case PushUtil.ChatMsgType.IMAGE:
                return isSend ? SEND_IMAGE : RECEIVE_IMAGE;
            case PushUtil.ChatMsgType.PUSH:
            case PushUtil.ChatMsgType.MULTI:
                return isSend ? SEND_MULTI : RECEIVE_MULTI;
        }
        return isSend ? SEND_TEXT : RECEIVE_TEXT;
    }

    public void destory() {
        mIndexArray.clear();
        mMessageList.clear();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        SendViewHolder viewHolder = null;
        MessageBody messageBody = new MessageBody(mMessageList.get(i));
        if (view == null) {
            int type = getItemViewType(i);
            Log.d(TAG, "create view type:" + type);
            view = getItemView(type, messageBody.getSource().getId() == mCurrentId);
        }

        viewHolder = (SendViewHolder) view.getTag();
        viewHolder.setDirect(messageBody.getSource().getId() == mCurrentId ? Direct.SEND : Direct.RECEIVE);
        viewHolder.setContainerContent(messageBody);
        setMessageBody(viewHolder, messageBody, i);
        setAvatar(viewHolder, messageBody);

        switch (messageBody.getMsgStatus()) {
            case MessageEntity.StatusType.SUCCESS:
                viewHolder.errorStatusView.setVisibility(View.INVISIBLE);
                break;
            case MessageEntity.StatusType.UPLOADING:
                viewHolder.errorStatusView.setVisibility(View.VISIBLE);
                viewHolder.errorStatusView.setProgressStatus();
                break;
            case MessageEntity.StatusType.FAILED:
                viewHolder.errorStatusView.setVisibility(View.VISIBLE);
                viewHolder.errorStatusView.setErrorStatus();

        }
        initClickListener(viewHolder, i);
        return view;
    }

    private void setAvatar(final SendViewHolder viewHolder, MessageBody messageBody) {
        Source source = messageBody.getSource();
        String avatarSrc = mMessageHelper.getRoleAvatar(source.getType(), source.getId());
        MaskBitmap maskBitmap = ImageCache.getInstance().get(avatarSrc);
        if (maskBitmap == null || maskBitmap.target == null) {
            mMessageListItemController.onUpdateRole(source.getType(), source.getId());
            ImageLoader.getInstance().displayImage(avatarSrc, viewHolder.avatarView, mOptions, new SimpleImageLoadingListener() {

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (imageUri.startsWith("drawable:")) {
                        return;
                    }
                    ImageCache.getInstance().put(imageUri, new MaskBitmap(loadedImage));
                    viewHolder.avatarView.setImageBitmap(loadedImage);
                }
            });
            return;
        }
        viewHolder.avatarView.setImageBitmap(maskBitmap.target);
    }

    private void initClickListener(SendViewHolder viewHolder, int position) {
        ViewItemClickListener itemClickListener = new ViewItemClickListener(position);
        viewHolder.containerView.setOnClickListener(itemClickListener);
        viewHolder.avatarView.setOnClickListener(itemClickListener);
        viewHolder.errorStatusView.setOnClickListener(itemClickListener);
        viewHolder.containerView.setOnLongClickListener(itemClickListener);
    }

    public void setMessageBody(SendViewHolder viewHolder, MessageBody messageBody, int position) {
        switch (messageBody.getDestination().getType()) {
            case Destination.COURSE:
            case Destination.CLASSROOM:
                viewHolder.nicknameView.setText(messageBody.getSource().getNickname());
                viewHolder.nicknameView.setVisibility(View.VISIBLE);
                break;
            default:
                viewHolder.nicknameView.setVisibility(View.GONE);
        }
        viewHolder.timeView.setVisibility(View.GONE);
        if (position > 0) {
            long preTime = mMessageList.get(position - 1).getTime() * 1000L;
            if (messageBody.getCreatedTime() - preTime > TIME_INTERVAL) {
                viewHolder.timeView.setVisibility(View.VISIBLE);
                viewHolder.timeView.setText(TimeUtil.convertMills2Date(messageBody.getCreatedTime()));
            }
        } else {
            viewHolder.timeView.setVisibility(View.VISIBLE);
            viewHolder.timeView.setText(TimeUtil.convertMills2Date(messageBody.getCreatedTime()));
        }
    }

    @Override
    public int getCount() {
        return mMessageList.size();
    }

    @Override
    public MessageEntity getItem(int i) {
        return mMessageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void addList(List<MessageEntity> messageBodyList) {
        mMessageList.addAll(0, messageBodyList);
        notifyDataSetChanged();
    }

    public void clear() {
        mMessageList.clear();
        notifyDataSetChanged();
    }

    public void insertList(final List<MessageEntity> messageBodyList) {
        if (messageBodyList.isEmpty()) {
            return;
        }
        mMessageList.addAll(0, messageBodyList);
        notifyDataSetInvalidated();
    }

    public void addItem(MessageEntity messageBody) {
        mMessageList.add(messageBody);
        notifyDataSetChanged();
    }

    public void setmMessageListItemController(MessageListItemController listItemClickListener) {
        this.mMessageListItemController = listItemClickListener;
    }

    protected View createTextView(boolean isSend) {
        if (isSend) {
            return LayoutInflater.from(mContext).inflate(R.layout.item_message_list_text_content, null);
        }
        return LayoutInflater.from(mContext).inflate(R.layout.item_message_list_receive_text_content, null);
    }

    protected View createMultiView(boolean isSend) {
        if (isSend) {
            return LayoutInflater.from(mContext).inflate(R.layout.item_message_list_send_multi_content, null);
        }
        return LayoutInflater.from(mContext).inflate(R.layout.item_message_list_receive_multi_content, null);
    }

    protected View createImageView(boolean isSend) {
        if (isSend) {
            return LayoutInflater.from(mContext).inflate(R.layout.item_message_list_send_image_content, null);
        }
        return LayoutInflater.from(mContext).inflate(R.layout.item_message_list_receive_image_content, null);
    }

    protected View createAudioView(Direct direct) {
        if (direct == Direct.RECEIVE) {
            return LayoutInflater.from(mContext).inflate(R.layout.item_message_list_receive_audio_content, null);
        }
        return LayoutInflater.from(mContext).inflate(R.layout.item_message_list_send_audio_content, null);
    }

    protected View getItemView(int type, boolean isSend) {
        View contentView = null;

        switch (type) {
            case SEND_TEXT:
                contentView = createTextView(true);
                contentView.setTag(new TextViewHolder(contentView));
                break;
            case RECEIVE_TEXT:
                contentView = createTextView(false);
                contentView.setTag(new TextViewHolder(contentView));
                break;
            case SEND_AUDIO:
                contentView = createAudioView(isSend ? Direct.SEND : Direct.RECEIVE);
                contentView.setTag(new AudioViewHolder(contentView));
                break;
            case RECEIVE_AUDIO:
                contentView = createAudioView(isSend ? Direct.SEND : Direct.RECEIVE);
                contentView.setTag(new AudioViewHolder(contentView));
                break;
            case SEND_IMAGE:
                contentView = createImageView(true);
                contentView.setTag(new ImageVewHolder(contentView));
                break;
            case RECEIVE_IMAGE:
                contentView = createImageView(false);
                contentView.setTag(new ImageVewHolder(contentView));
                break;
            case SEND_MULTI:
                contentView = createMultiView(true);
                contentView.setTag(new MultiViewHolder(contentView));
                break;
            case RECEIVE_MULTI:
                contentView = createMultiView(false);
                contentView.setTag(new MultiViewHolder(contentView));
                break;
        }
        return contentView;
    }

    /**
     * ViewItemClickListener
     */
    class ViewItemClickListener implements View.OnClickListener, View.OnLongClickListener {

        private int mPosition;

        public ViewItemClickListener(int position) {
            this.mPosition = position;
        }

        private int[] getAudioAnimResArray(MessageBody messageBody, int currentId) {
            if (messageBody.getSource().getId() == currentId) {
                return new int[]{
                        R.drawable.chat_to_speak_voice,
                        R.drawable.chat_to_voice_play_anim
                };
            }
            return new int[]{
                    R.drawable.chat_from_speak_voice,
                    R.drawable.chat_from_voice_play_anim
            };
        }

        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            if (id == R.id.tv_container) {
                View parent = (View) view.getParent();
                if (parent != null) {
                    parent.performLongClick();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.tv_container) {
                MessageBody messageBody = new MessageBody(getItem(mPosition));
                if (PushUtil.ChatMsgType.AUDIO.equals(messageBody.getType())) {
                    AudioBody audioBody = AudioUtil.getAudioBody(messageBody.getBody());

                    File audioFile = mMessageHelper.getRealAudioFile(audioBody.getFile());
                    if (audioFile == null || !audioFile.exists()) {
                        return;
                    }
                    int[] animResArray = getAudioAnimResArray(messageBody, mCurrentId);
                    mMessageListItemController.onAudioClick(
                            mPosition,
                            audioFile.getAbsolutePath(),
                            new AudioPlayStatusListenerImpl((ImageView) view.findViewById(R.id.iv_voice_play_anim), animResArray));
                } else if (PushUtil.ChatMsgType.IMAGE.equals(messageBody.getType())) {
                    mMessageListItemController.onImageClick(messageBody.getBody());
                } else {
                    mMessageListItemController.onContentClick(mPosition);
                }
            } else if (id == R.id.tv_error_status) {
                mMessageListItemController.onErrorClick(mPosition);
            } else if (id == R.id.tv_avatar) {
                mMessageListItemController.onAvatarClick(MessageUtil.parseInt(getItem(mPosition).getFromId()));
            }
        }

        /*
        AudioPlayStatusListenerImpl
         */
        class AudioPlayStatusListenerImpl implements AudioPlayStatusListener {

            private int[] mAnimRes;
            private ImageView mVoiceView;
            private AnimationDrawable mAnimDrawable;

            public AudioPlayStatusListenerImpl(ImageView voiceView, int[] animRes) {
                this.mAnimRes = animRes;
                this.mVoiceView = voiceView;
            }

            @Override
            public void onPlay() {
                if (mVoiceView.getDrawable() instanceof AnimationDrawable) {
                    mAnimDrawable = (AnimationDrawable) mVoiceView.getDrawable();
                    mAnimDrawable.stop();
                    mAnimDrawable.start();
                } else {
                    mVoiceView.setImageResource(mAnimRes[1]);
                    mAnimDrawable = (AnimationDrawable) mVoiceView.getDrawable();
                    mAnimDrawable.start();
                }
            }

            @Override
            public void onStop() {
                if (mAnimDrawable != null) {
                    mAnimDrawable.stop();
                    mVoiceView.setImageResource(mAnimRes[0]);
                }
            }
        }
    }

    /*
        content MultiViewHolder
     */

    class MultiViewHolder extends SendViewHolder {

        public TextView multiTitleView;
        public TextView multiContentView;
        public ImageView mulitIconView;

        public MultiViewHolder(View view) {
            super(view);
            multiTitleView = (TextView) view.findViewById(R.id.chat_multi_title);
            multiContentView = (TextView) view.findViewById(R.id.chat_multi_content);
            mulitIconView = (ImageView) view.findViewById(R.id.chat_multi_icon);
        }

        @Override
        public void setContainerContent(MessageBody messageBody) {
            super.setContainerContent(messageBody);
            String body = messageBody.getBody();
            try {
                JSONObject jsonObject = new JSONObject(body);
                String type = jsonObject.optString("type");
                switch (type) {
                    case PushUtil.CourseType.QUESTION_CREATED:
                        multiTitleView.setText(jsonObject.optString("questionTitle"));
                        multiContentView.setText(jsonObject.optString("title"));
                        mulitIconView.setImageResource(R.drawable.default_course);
                        break;
                    default:
                        multiTitleView.setText(jsonObject.optString("title"));
                        multiContentView.setText(jsonObject.optString("content"));
                        ImageLoader.getInstance().displayImage(jsonObject.optString("image"), mulitIconView);
                }
            } catch (JSONException e) {
            }
        }
    }

    class TextViewHolder extends SendViewHolder {

        public TextView mContentView;

        public TextViewHolder(View view) {
            super(view);
            mContentView = (TextView) view.findViewById(R.id.tv_send_content);
        }

        @Override
        public void setContainerContent(MessageBody messageBody) {
            super.setContainerContent(messageBody);
            mContentView.setText(messageBody.getBody());
        }
    }

    class AudioViewHolder extends SendViewHolder {

        public ImageView mAudioView;
        public TextView mLengthView;

        public AudioViewHolder(View view) {
            super(view);
            mAudioView = (ImageView) view.findViewById(R.id.iv_voice_play_anim);
            mLengthView = (TextView) view.findViewById(R.id.tv_audio_length);
        }

        private void updateMessageStatus(int id, int status) {
            ContentValues cv = new ContentValues();
            cv.put("status", status);
            IMClient.getClient().getMessageManager().updateMessageField(id, cv);
        }

        @Override
        public void setContainerContent(MessageBody messageBody) {
            super.setContainerContent(messageBody);
            AudioBody audioBody = AudioUtil.getAudioBody(messageBody.getBody());
            ViewGroup.LayoutParams lp = mAudioView.getLayoutParams();
            lp.width = 96 + TimeUtil.getDuration(audioBody.getDuration()) * 8;
            //lp.height = 48;
            mAudioView.setLayoutParams(lp);

            if (unReadView != null) {
                unReadView.setVisibility(View.GONE);
            }

            switch (messageBody.getMsgStatus()) {
                case MessageEntity.StatusType.NONE:
                    mLengthView.setText("");
                    try {
                        if (IMClient.getClient().getResourceHelper().hasTask(messageBody.getMid())) {
                            return;
                        }
                        File realFile = mMessageHelper.createAudioFile(audioBody.getFile());
                        ResourceDownloadTask downloadTask = new ResourceDownloadTask(mContext, messageBody.getMid(), audioBody.getFile(), realFile);
                        IMClient.getClient().getResourceHelper().addTask(downloadTask);
                        messageBody.setMsgStatus(MessageEntity.StatusType.UPLOADING);
                        updateMessageStatus(messageBody.getMid(), MessageEntity.StatusType.FAILED);
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                    break;
                case MessageEntity.StatusType.UNREAD:
                    mLengthView.setText("");
                    errorStatusView.setVisibility(View.GONE);
                    if (unReadView != null) {
                        unReadView.setVisibility(View.VISIBLE);
                    }
                case MessageEntity.StatusType.SUCCESS:
                    mLengthView.setText(String.format("%d\"", TimeUtil.getDuration(audioBody.getDuration())));
                    break;
            }
        }
    }

    class ImageVewHolder extends SendViewHolder {

        public ChatImageView mImageView;

        public ImageVewHolder(View view) {
            super(view);
            mImageView = (ChatImageView) view.findViewById(R.id.iv_msg_image);
        }

        @Override
        public void setContainerContent(MessageBody messageBody) {
            super.setContainerContent(messageBody);
            String body = messageBody.getBody();

            String imagePath = mMessageHelper.getThumbImagePath(body);
            //local file
            if (imagePath.startsWith("file:")) {
                MaskBitmap bitmap = ImageCache.getInstance().get(imagePath);
                if (bitmap != null) {
                    Log.d(TAG, "image is cache");
                    mImageView.setMaskBitmap(bitmap);
                    return;
                }
                ImageLoader.getInstance().displayImage(imagePath, mImageView, mOptions, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        ImageCache.getInstance().put(imageUri, new MaskBitmap(loadedImage));
                        mImageView.setMaskBitmap(new MaskBitmap(loadedImage));
                    }
                });
            } else {
                try {
                    if (IMClient.getClient().getResourceHelper().hasTask(messageBody.getMid())) {
                        return;
                    }
                    File realFile = mMessageHelper.createImageFile(body);
                    ResourceDownloadTask downloadTask = new ResourceDownloadTask(mContext, messageBody.getMid(), body, realFile);
                    IMClient.getClient().getResourceHelper().addTask(downloadTask);
                    messageBody.setMsgStatus(MessageEntity.StatusType.UPLOADING);
                } catch (IOException ie) {
                    ie.printStackTrace();
                }
                mImageView.setImageResource(mMessageHelper.getDefaultImageRes());
            }
        }
    }

    /*
        SendViewHolder
     */
    class SendViewHolder {

        protected Direct mDirect;

        public TextView timeView;
        public TextView nicknameView;
        public ImageView avatarView;
        public View containerView;
        public MessageStatusView errorStatusView;
        public ImageView unReadView;

        public SendViewHolder(View view) {
            timeView = (TextView) view.findViewById(R.id.tv_time);
            nicknameView = (TextView) view.findViewById(R.id.tv_nickname);
            containerView = view.findViewById(R.id.tv_container);
            avatarView = (ImageView) view.findViewById(R.id.tv_avatar);
            errorStatusView = (MessageStatusView) view.findViewById(R.id.tv_error_status);
            unReadView = (ImageView) view.findViewById(R.id.tv_unread_view);
        }

        public void setDirect(Direct direct) {
            this.mDirect = direct;
        }

        public void setContainerContent(MessageBody messageBody) {
        }
    }
}
