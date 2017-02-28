package com.edusoho.kuozhi.imserver.ui.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.edusoho.kuozhi.imserver.ui.listener.MessageItemOnClickListener;
import com.edusoho.kuozhi.imserver.ui.listener.MessageListItemController;
import com.edusoho.kuozhi.imserver.ui.util.AudioUtil;
import com.edusoho.kuozhi.imserver.ui.util.ImageCache;
import com.edusoho.kuozhi.imserver.ui.util.MaskBitmap;
import com.edusoho.kuozhi.imserver.ui.util.ResourceDownloadTask;
import com.edusoho.kuozhi.imserver.ui.view.ChatImageView;
import com.edusoho.kuozhi.imserver.ui.view.MessageStatusView;
import com.edusoho.kuozhi.imserver.util.MessageUtil;
import com.edusoho.kuozhi.imserver.util.SystemUtil;
import com.edusoho.kuozhi.imserver.util.TimeUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by suju on 16/8/26.
 */
public class MessageRecyclerListAdapter extends RecyclerView.Adapter<MessageRecyclerListAdapter.MessageViewHolder> {

    private static final int SEND = 0X01;
    private static final int RECEIVE = 0X02;

    protected static final int RECEIVE_TEXT = 0;
    protected static final int SEND_TEXT = 1;
    protected static final int RECEIVE_AUDIO = 2;
    protected static final int SEND_AUDIO = 3;
    protected static final int RECEIVE_IMAGE = 4;
    protected static final int SEND_IMAGE = 5;
    protected static final int RECEIVE_MULTI = 6;
    protected static final int SEND_MULTI = 7;
    protected static final int LABEL = 8;

    protected static long TIME_INTERVAL = 60 * 3 * 1000;
    protected static final String TAG = "MessageListAdapter";

    protected int mCurrentId;
    private int mMaxAudioWidth;
    private MessageHelper mMessageHelper;
    private MessageItemOnClickListener mMessageItemOnClickListener;
    private MessageListItemController mMessageListItemController;

    protected Context mContext;
    protected List<MessageEntity> mMessageList;
    protected DisplayImageOptions mOptions;
    protected SparseArray<MessageBody> mIndexArray;

    public MessageRecyclerListAdapter(Context context) {
        this.mContext = context;
        this.mMessageList = new CopyOnWriteArrayList<>();
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).
                showImageForEmptyUri(R.drawable.message_image_default).
                showImageOnFail(R.drawable.message_image_default).build();

        this.mMessageHelper = new MessageHelper(mContext);
        this.mIndexArray = new SparseArray<>();
        this.mMaxAudioWidth = SystemUtil.getScreenWidth(mContext) / 3;
    }

    public void removeItem(int id) {
        int size = mMessageList.size();
        for (int i = 0; i < size; i++) {
            if (mMessageList.get(i).getId() == id) {
                mMessageList.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, size - i);
                return;
            }
        }
    }

    private void updateMessageEntity(MessageEntity oldEntity, MessageEntity newEntity) {
        oldEntity.setStatus(newEntity.getStatus());
        oldEntity.setTime(newEntity.getTime());
        oldEntity.setCmd(newEntity.getCmd());
        oldEntity.setConvNo(newEntity.getConvNo());
        oldEntity.setFromId(newEntity.getFromId());
        oldEntity.setFromName(newEntity.getFromName());
        oldEntity.setMsg(newEntity.getMsg());
        oldEntity.setToId(newEntity.getToId());
        oldEntity.setToName(newEntity.getToName());
        oldEntity.setUid(newEntity.getUid());
    }

    public void updateItem(MessageEntity updateMessageEntity) {
        Iterator<MessageEntity> iterator = mMessageList.iterator();
        int position = 0;
        while (iterator.hasNext()) {
            MessageEntity messageEntity = iterator.next();
            if (messageEntity.getId() == updateMessageEntity.getId()) {
                mMessageList.set(position, updateMessageEntity);
                break;
            }
            position++;
        }

        notifyItemRangeChanged(position, getItemCount() - position);
    }

    public void setCurrentId(int currentId) {
        this.mCurrentId = currentId;
    }

    public MessageEntity getItem(int position) {
        return mMessageList.get(position);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public void setOnItemClickListener(MessageItemOnClickListener listener) {
        this.mMessageItemOnClickListener = listener;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "create view type:" + viewType);
        View contentView = getItemView(viewType);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentView.setLayoutParams(lp);
        MessageViewHolder viewHolder = createViewHolder(viewType, contentView);
        viewHolder.setMessageItemOnClickListener(mMessageItemOnClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int position) {
        MessageBody messageBody = new MessageBody(mMessageList.get(position));

        viewHolder.resetItemClickListenerIndex(position);
        viewHolder.setDirect(messageBody.getSource().getId() == mCurrentId ? Direct.SEND : Direct.RECEIVE);
        viewHolder.setContainerContent(messageBody);
        viewHolder.setMessageBody(messageBody, position);
        viewHolder.setMessageStatus(messageBody);
        viewHolder.setAvatar(messageBody);

        initClickListener(viewHolder, position);
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
            case PushUtil.ChatMsgType.LABEL:
                return LABEL;
        }
        return isSend ? SEND_TEXT : RECEIVE_TEXT;
    }

    protected MessageViewHolder createViewHolder(int viewType, View contentView) {
        switch (viewType) {
            case SEND_AUDIO:
            case RECEIVE_AUDIO:
                return new AudioViewHolder(contentView);
            case SEND_IMAGE:
            case RECEIVE_IMAGE:
                return new ImageVewHolder(contentView);
            case SEND_MULTI:
            case RECEIVE_MULTI:
                return new MultiViewHolder(contentView);
            case LABEL:
                return new LabelViewHolder(contentView);
        }

        return new TextViewHolder(contentView);
    }

    protected class MessageViewHolder extends RecyclerView.ViewHolder {

        protected Direct mDirect;

        public TextView timeView;
        public TextView nicknameView;
        public ImageView avatarView;
        public View containerView;
        public MessageStatusView errorStatusView;
        public ImageView unReadView;

        private int mCurrentPosition;
        private MessageItemOnClickListener mMessageItemOnClickListener;

        public MessageViewHolder(View view) {
            super(view);
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

        public void addViewClickListener(ViewItemClickListener onClickListener) {
            containerView.setOnClickListener(onClickListener);
            avatarView.setOnClickListener(onClickListener);
            errorStatusView.setOnClickListener(onClickListener);
            containerView.setOnLongClickListener(onClickListener);
        }

        public void setMessageItemOnClickListener(MessageItemOnClickListener listener) {
            this.mMessageItemOnClickListener = listener;
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mMessageItemOnClickListener == null) {
                        return false;
                    }
                    mMessageItemOnClickListener.onItemClick(mCurrentPosition, itemView);
                    return false;
                }
            });
        }

        public void resetItemClickListenerIndex(int position) {
            this.mCurrentPosition = position;
        }

        protected void setMessageStatus(MessageBody messageBody) {
            if (mDirect == Direct.RECEIVE
                    && (PushUtil.ChatMsgType.TEXT.equals(messageBody.getType()) || PushUtil.ChatMsgType.MULTI.equals(messageBody.getType()))) {
                errorStatusView.setVisibility(View.INVISIBLE);
                return;
            }
            switch (messageBody.getMsgStatus()) {
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

        private void setNickNameFromMessage(MessageBody messageBody) {
            Destination destination = messageBody.getDestination();
            if (destination == null || TextUtils.isEmpty(destination.getType())) {
                return;
            }
            switch (destination.getType()) {
                case Destination.GROUP:
                case Destination.COURSE:
                case Destination.CLASSROOM:
                    nicknameView.setText(messageBody.getSource().getNickname());
                    nicknameView.setVisibility(View.VISIBLE);
                    break;
                default:
                    nicknameView.setVisibility(View.GONE);
            }
        }

        public void setMessageBody(MessageBody messageBody, int position) {
            setNickNameFromMessage(messageBody);
            timeView.setVisibility(View.GONE);
            if (position < (getItemCount() - 1)) {
                long preTime = mMessageList.get(position + 1).getTime() * 1000L;
                if (messageBody.getCreatedTime() - preTime > TIME_INTERVAL) {
                    timeView.setVisibility(View.VISIBLE);
                    timeView.setText(TimeUtil.convertMills2Date(messageBody.getCreatedTime()));
                }
                return;
            }
            timeView.setVisibility(View.VISIBLE);
            timeView.setText(TimeUtil.convertMills2Date(messageBody.getCreatedTime()));
        }

        protected void setAvatar(MessageBody messageBody) {
            Source source = messageBody.getSource();
            String avatarSrc = mMessageHelper.getRoleAvatar(source.getType(), source.getId());
            MaskBitmap maskBitmap = ImageCache.getInstance().get(avatarSrc);
            if (maskBitmap == null || maskBitmap.target == null) {
                mMessageListItemController.onUpdateRole(source.getType(), source.getId());
                ImageLoader.getInstance().displayImage(avatarSrc, avatarView, mOptions, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (imageUri.startsWith("drawable:")) {
                            return;
                        }
                        ImageCache.getInstance().put(imageUri, new MaskBitmap(loadedImage));
                        avatarView.setImageBitmap(loadedImage);
                    }
                });
                return;
            }
            avatarView.setImageBitmap(maskBitmap.target);
        }
    }

    public void destory() {
        mIndexArray.clear();
        mMessageList.clear();
    }

    private void initClickListener(MessageViewHolder viewHolder, int position) {
        ViewItemClickListener itemClickListener = new ViewItemClickListener(position);
        viewHolder.addViewClickListener(itemClickListener);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setList(List<MessageEntity> messageBodyList) {
        mMessageList.clear();
        mMessageList.addAll(messageBodyList);
        notifyDataSetChanged();
    }

    public void clear() {
        mMessageList.clear();
        notifyDataSetChanged();
    }

    public void insertList(List<MessageEntity> messageBodyList) {
        if (messageBodyList.isEmpty()) {
            return;
        }
        mMessageList.addAll(messageBodyList);
        notifyDataSetChanged();
    }

    public void addItem(MessageEntity messageBody) {
        mMessageList.add(0, messageBody);
        notifyDataSetChanged();
    }

    public void setMessageListItemController(MessageListItemController listItemClickListener) {
        this.mMessageListItemController = listItemClickListener;
    }

    protected View createLabelView() {
        return LayoutInflater.from(mContext).inflate(R.layout.item_message_list_label_layout, null);
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

    protected View createAudioView(boolean isSend) {
        if (isSend) {
            return LayoutInflater.from(mContext).inflate(R.layout.item_message_list_send_audio_content, null);
        }
        return LayoutInflater.from(mContext).inflate(R.layout.item_message_list_receive_audio_content, null);
    }

    protected View getItemView(int type) {
        View contentView = null;
        switch (type) {
            case SEND_TEXT:
                contentView = createTextView(true);
                break;
            case RECEIVE_TEXT:
                contentView = createTextView(false);
                break;
            case SEND_AUDIO:
                contentView = createAudioView(true);
                break;
            case RECEIVE_AUDIO:
                contentView = createAudioView(false);
                break;
            case SEND_IMAGE:
                contentView = createImageView(true);
                break;
            case RECEIVE_IMAGE:
                contentView = createImageView(false);
                break;
            case SEND_MULTI:
                contentView = createMultiView(true);
                break;
            case RECEIVE_MULTI:
                contentView = createMultiView(false);
                break;
            case LABEL:
                contentView = createLabelView();
        }
        return contentView;
    }

    /**
     * ViewItemClickListener
     */
    protected class ViewItemClickListener implements View.OnClickListener, View.OnLongClickListener {

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
                MessageBody messageBody = new MessageBody(mMessageList.get(mPosition));
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
                mMessageListItemController.onAvatarClick(MessageUtil.parseInt(mMessageList.get(mPosition).getFromId()));
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

    class MultiViewHolder extends MessageViewHolder {

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

    protected class LabelViewHolder extends MessageViewHolder {

        public TextView mContentView;
        public LabelViewHolder(View view) {
            super(view);
            mContentView = (TextView) view.findViewById(R.id.tv_label);
        }

        @Override
        public void addViewClickListener(ViewItemClickListener onClickListener) {
        }

        @Override
        protected void setMessageStatus(MessageBody messageBody) {
        }

        @Override
        public void setMessageItemOnClickListener(MessageItemOnClickListener listener) {
        }

        @Override
        protected void setAvatar(MessageBody messageBody) {
        }

        @Override
        public void setMessageBody(MessageBody messageBody, int position) {
            //none
        }

        @Override
        public void setContainerContent(MessageBody messageBody) {
            super.setContainerContent(messageBody);
            try {
                JSONObject jsonObject = new JSONObject(messageBody.getBody());
                switch (jsonObject.optString("cmd")) {
                    case "memberJoined":
                        mContentView.setVisibility(View.VISIBLE);
                        mContentView.setText(String.format("%s 加入直播教室", jsonObject.optString("clientName")));
                        break;
                    default:
                        mContentView.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
            }
        }
    }

    protected class TextViewHolder extends MessageViewHolder {

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

    protected class AudioViewHolder extends MessageViewHolder {

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

        private void checkAudioFileIsExist(MessageBody messageBody, AudioBody audioBody) {
            if (TextUtils.isEmpty(audioBody.getFile())) {
                messageBody.setMsgStatus(MessageEntity.StatusType.FAILED);
                return;
            }
            File realFile = mMessageHelper.getRealAudioFile(audioBody.getFile());
            if (realFile == null || !realFile.exists()) {
                try {
                    if (IMClient.getClient().getResourceHelper().hasTask(messageBody.getMid())) {
                        return;
                    }
                    realFile = mMessageHelper.createAudioFile(audioBody.getFile());
                    ResourceDownloadTask downloadTask = new ResourceDownloadTask(mContext, messageBody.getMid(), audioBody.getFile(), realFile);
                    IMClient.getClient().getResourceHelper().addTask(downloadTask);
                    messageBody.setMsgStatus(MessageEntity.StatusType.UPLOADING);
                    updateMessageStatus(messageBody.getMid(), MessageEntity.StatusType.FAILED);
                } catch (IOException ie) {
                    ie.printStackTrace();
                }
            }
        }

        @Override
        public void setContainerContent(MessageBody messageBody) {
            super.setContainerContent(messageBody);
            AudioBody audioBody = AudioUtil.getAudioBody(messageBody.getBody());
            ViewGroup.LayoutParams lp = mAudioView.getLayoutParams();

            int lengthWidth = TimeUtil.getDuration(audioBody.getDuration()) * 8;
            lp.width = 120 + (lengthWidth > mMaxAudioWidth ? mMaxAudioWidth : lengthWidth);
            //lp.height = 48;
            mAudioView.setLayoutParams(lp);
            if (unReadView != null) {
                unReadView.setVisibility(View.GONE);
            }
            if (MessageEntity.StatusType.FAILED == messageBody.getMsgStatus()) {
                mLengthView.setText("");
                return;
            }
            checkAudioFileIsExist(messageBody, audioBody);
            switch (messageBody.getMsgStatus()) {
                case MessageEntity.StatusType.UPLOADING:
                    mLengthView.setText("");
                    break;
                case MessageEntity.StatusType.NONE:
                    mLengthView.setText("");
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

    protected class ImageVewHolder extends MessageViewHolder {

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
                MaskBitmap maskBitmap = ImageCache.getInstance().get(imagePath);
                if (maskBitmap != null && maskBitmap.direct == mDirect) {
                    Log.d(TAG, "image is cache");
                    mImageView.setMaskBitmap(maskBitmap);
                    return;
                }
                ImageLoader.getInstance().displayImage(imagePath, mImageView, mOptions, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        MaskBitmap maskBitmap = new MaskBitmap(loadedImage);
                        maskBitmap.direct = mDirect;
                        ImageCache.getInstance().put(imageUri, maskBitmap);
                        mImageView.setMaskBitmap(maskBitmap);
                    }
                });
            } else {
                try {
                    if (TextUtils.isEmpty(body) || IMClient.getClient().getResourceHelper().hasTask(messageBody.getMid())) {
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
}
