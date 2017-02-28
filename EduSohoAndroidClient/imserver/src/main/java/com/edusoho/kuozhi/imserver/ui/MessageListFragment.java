package com.edusoho.kuozhi.imserver.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.R;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.ui.adapter.MessageRecyclerListAdapter;
import com.edusoho.kuozhi.imserver.ui.broadcast.ResourceStatusReceiver;
import com.edusoho.kuozhi.imserver.ui.entity.PushUtil;
import com.edusoho.kuozhi.imserver.ui.helper.MessageHelper;
import com.edusoho.kuozhi.imserver.ui.listener.AudioPlayStatusListener;
import com.edusoho.kuozhi.imserver.ui.listener.InputViewControllerListener;
import com.edusoho.kuozhi.imserver.ui.listener.MessageItemOnClickListener;
import com.edusoho.kuozhi.imserver.ui.listener.MessageListItemController;
import com.edusoho.kuozhi.imserver.ui.listener.MessageSendListener;
import com.edusoho.kuozhi.imserver.ui.util.MessageAudioPlayer;
import com.edusoho.kuozhi.imserver.ui.view.IMessageInputView;
import com.edusoho.kuozhi.imserver.ui.view.MessageInputView;
import com.edusoho.kuozhi.imserver.ui.view.TextMessageInputView;
import com.edusoho.kuozhi.imserver.util.SystemUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Created by suju on 16/8/26.
 */
public class MessageListFragment extends Fragment implements
        ResourceStatusReceiver.StatusReceiverCallback, MessageItemOnClickListener, IMessageListView {

    private static final String TAG = "MessageListFragment";

    public static final String CONV_NO = "convNo";
    public static final String TARGET_TYPE = "targetType";
    public static final String TARGET_ID = "targetId";
    public static final String CURRENT_ID = "currentId";

    private int mStart = 0;
    private int mInputMode = IMessageInputView.INPUT_IMAGE_AND_VOICE;
    private boolean canLoadData = true;
    private Context mContext;
    private boolean mIsEnable = true;
    private int mCurrentSelectedIndex;
    private MessageAudioPlayer mAudioPlayer;
    private MessageSendListener mMessageSendListener;

    protected ResourceStatusReceiver mResourceStatusReceiver;
    protected PtrClassicFrameLayout mPtrFrame;
    protected RecyclerView mMessageListView;
    protected View mContainerView;
    protected LinearLayoutManager mLayoutManager;
    protected IMessageInputView mMessageInputView;
    protected MessageRecyclerListAdapter mListAdapter;
    protected IMessageListPresenter mIMessageListPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResourceStatusReceiver = new ResourceStatusReceiver(this);
        mContext.registerReceiver(mResourceStatusReceiver, new IntentFilter(ResourceStatusReceiver.ACTION));
        Log.d(TAG, "onCreate");
    }

    @Override
    public void setPresenter(IMessageListPresenter presenter) {
        this.mIMessageListPresenter = presenter;
    }

    @Override
    public void setEnable(boolean isEnable) {
        this.mIsEnable = isEnable;
        if (mMessageInputView == null) {
            return;
        }
        mMessageInputView.setEnabled(isEnable);
        mMessageListView.setEnabled(isEnable);
    }

    protected boolean canRefresh() {
        return mIMessageListPresenter.canRefresh();
    }

    @Override
    public void onUserKicked() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("异地登录")
                .setMessage("当前帐号已在其他设备上登录，直播课程不能多端同时学习")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.show();
    }

    @Override
    public void setInputTextMode(int mode) {
        this.mInputMode = mode;
    }

    public void setAdapter(MessageRecyclerListAdapter adapter) {
        this.mListAdapter = adapter;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
        mContext = activity.getBaseContext();
        if (mListAdapter == null) {
            mListAdapter = new MessageRecyclerListAdapter(getActivity().getBaseContext());
            mListAdapter.setCurrentId(IMClient.getClient().getClientId());
            mListAdapter.setOnItemClickListener(this);
        }
        mListAdapter.setMessageListItemController(getMessageListItemClickListener());
    }

    @Override
    public void onItemClick(int position, View itemView) {
        mCurrentSelectedIndex = position;
        showItemMenuDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mContainerView == null) {
            mContainerView = inflater.inflate(R.layout.fragment_message_list_layout, null);
        }

        ViewGroup parent = (ViewGroup) mContainerView.getParent();
        if (parent != null) {
            parent.removeView(mContainerView);
        }
        return mContainerView;
    }

    @Override
    public void onResourceDownloadInvoke(int resId, String resUri) {
        if (resId < 0) {
            return;
        }
        mIMessageListPresenter.processResourceDownload(resId, resUri);
    }

    @Override
    public void onResourceStatusInvoke(int resId, String resUri) {
        mIMessageListPresenter.processResourceStatusChange(resId, resUri);
    }

    @Override
    public void notifyDataSetChanged() {
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifiy(String content) {
        SystemUtil.toast(mContext, content);
    }

    public static final int MESSAGE_SELECT_LAST = 0;
    public static final int MESSAGE_SELECT_POSTION = 1;

    protected Handler mUpdateHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_SELECT_LAST:
                    mMessageListView.scrollToPosition(0);
                    break;
                case MESSAGE_SELECT_POSTION:
                    Log.d(TAG, "p:" + (mListAdapter.getItemCount() - msg.arg1));
                    mMessageListView.smoothScrollToPosition(msg.arg1);
                    break;
            }
        }
    };

    protected void initView(View view) {
        Log.d(TAG, "initView");
        mPtrFrame = (PtrClassicFrameLayout) view.findViewById(R.id.rotate_header_list_view_frame);
        mMessageListView = (RecyclerView) view.findViewById(R.id.listview);

        if (mInputMode == IMessageInputView.INPUT_TEXT) {
            mMessageInputView= new TextMessageInputView(mContext);
        } else {
            mMessageInputView = new MessageInputView(mContext);
        }
        ViewGroup inputViewGroup = (ViewGroup) view.findViewById(R.id.message_input_view);
        inputViewGroup.addView((View) mMessageInputView);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setReverseLayout(true);
        mMessageListView.setLayoutManager(mLayoutManager);
        mMessageListView.setAdapter(mListAdapter);
        mMessageListView.setItemAnimator(null);

        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mPtrFrame.refreshComplete();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIMessageListPresenter.insertMessageList();
                    }
                }, 350);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                boolean canDoRefresh = PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
                return canRefresh() && canDoRefresh;
            }
        });

        setEnable(mIsEnable);
        mMessageSendListener = getMessageSendListener();
        mMessageInputView.setMessageSendListener(mMessageSendListener);
        mMessageInputView.setMessageControllerListener(getMessageControllerListener());
        mMessageListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkCanAutoLoad(recyclerView);
                }
            }
        });
    }

    private synchronized void checkCanAutoLoad(RecyclerView recyclerView) {
        if (!canRefresh() || mPtrFrame.isAutoRefresh()) {
            Log.d(TAG, "auto loading");
            return;
        }
        int chileCount = recyclerView.getChildCount();
        View firstView = recyclerView.getChildAt(chileCount - 1);
        if (firstView != null && firstView.getTop() == 0) {
            Log.d(TAG, "auto load");
            mPtrFrame.autoRefresh();
        }
    }

    protected AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (view.getTag() != null) {
                    mCurrentSelectedIndex = i;
                    showItemMenuDialog();
                    return false;
                }
                return false;
            }
        };
    }

    protected void showItemMenuDialog() {
        registerForContextMenu(mMessageListView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        new MenuInflater(mContext).inflate(R.menu.message_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_copy);
        MenuItem replayItem = menu.findItem(R.id.menu_replay);
        MessageBody messageBody = getSelectedMessageBody();
        if (replayItem == null || menuItem == null || messageBody == null) {
            return;
        }
        replayItem.setVisible(!PushUtil.ChatMsgType.AUDIO.equals(messageBody.getType()));
        menuItem.setVisible(PushUtil.ChatMsgType.TEXT.equals(messageBody.getType()));
    }

    private void copyDataToClipboard(String text) {
        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(text);
        SystemUtil.toast(mContext, "已复制");
    }

    private MessageBody getSelectedMessageBody() {
        MessageEntity messageEntity = mListAdapter.getItem(mCurrentSelectedIndex);
        if (messageEntity == null) {
            return null;
        }
        return new MessageBody(messageEntity);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_copy) {
            MessageBody messageBody = getSelectedMessageBody();
            if (messageBody != null) {
                copyDataToClipboard(messageBody.getBody());
            }
        } else if (id == R.id.menu_replay) {
            MessageBody messageBody = getSelectedMessageBody();
            if (messageBody == null) {
                return true;
            }
            String body = "";
            if (PushUtil.ChatMsgType.MULTI.equals(messageBody.getType())) {
                body = messageBody.getBody();
            } else {
                JSONObject data = new JSONObject();
                try {
                    data.put("type", messageBody.getType());
                    data.put("fromType", Destination.USER);
                    data.put("title", "确定转发给:");
                    data.put("content", messageBody.getBody());
                    data.put("source", "self");
                    data.put("id", messageBody.getDestination().getId());
                    body = data.toString();
                } catch (JSONException e) {
                }
            }

            Bundle bundle = new Bundle();
            bundle.putString("data", body);
            bundle.putString("activityName", "ChatSelectFragment");
            bundle.putString("type", "relay");
            mIMessageListPresenter.onShowActivity(bundle);
        } else if (id == R.id.menu_delete) {
            MessageBody messageBody = getSelectedMessageBody();
            if (messageBody == null) {
                return true;
            }
            mListAdapter.removeItem(messageBody.getMid());
            mIMessageListPresenter.deleteMessageById(messageBody.getMid());
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        mIMessageListPresenter.removeReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
        }
        if (mResourceStatusReceiver != null) {
            mContext.unregisterReceiver(mResourceStatusReceiver);
        }
        mListAdapter.destory();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mIMessageListPresenter.addMessageReceiver();
    }

    @Override
    public void updateMessageEntity(MessageEntity updateMessageEntity) {
        mListAdapter.updateItem(updateMessageEntity);
    }

    /*
        MessageListItemClickListener
     */
    protected MessageListItemController getMessageListItemClickListener() {
        return new MessageListItemController() {

            @Override
            public void onAudioClick(int position, String audioFile, AudioPlayStatusListener listener) {
                if (mAudioPlayer != null) {
                    mAudioPlayer.stop();
                }
                mAudioPlayer = new MessageAudioPlayer(mContext, audioFile, listener);
                mAudioPlayer.play();

                MessageEntity messageEntity = mListAdapter.getItem(position);
                if (messageEntity.getStatus() == MessageEntity.StatusType.SUCCESS) {
                    return;
                }
                mIMessageListPresenter.updateMessageReceiveStatus(messageEntity, MessageEntity.StatusType.SUCCESS);
            }

            @Override
            public void onImageClick(String imageUrl) {
                showImageWithFullScreen(imageUrl);
            }

            @Override
            public void onErrorClick(int position) {
                mIMessageListPresenter.onSendMessageAgain(mListAdapter.getItem(position));
            }

            @Override
            public void onAvatarClick(int userId) {
                mIMessageListPresenter.onShowUser(userId);
            }

            @Override
            public void onUpdateRole(final String type, final int rid) {
                mIMessageListPresenter.updateRole(type, rid);
            }

            @Override
            public void onContentClick(int position) {
                MessageBody messageBody = new MessageBody(mListAdapter.getItem(position));
                switch (messageBody.getType()) {
                    case PushUtil.ChatMsgType.MULTI:
                        try {
                            JSONObject jsonObject = new JSONObject(messageBody.getBody());
                            Bundle bundle = new Bundle();
                            bundle.putString("url", jsonObject.optString("url"));
                            bundle.putString("type", "webpage");
                            mIMessageListPresenter.onShowActivity(bundle);
                        } catch (JSONException e) {
                        }
                        break;
                    case PushUtil.ChatMsgType.PUSH:
                        try {
                            JSONObject jsonObject = new JSONObject(messageBody.getBody());
                            String type = jsonObject.optString("type");
                            if (PushUtil.CourseType.QUESTION_CREATED.equals(type)) {
                                Bundle bundle = new Bundle();
                                bundle.putInt("target_id", jsonObject.optInt("course"));
                                bundle.putString("target_type", "course");
                                bundle.putInt("thread_id", jsonObject.optInt("threadId"));
                                bundle.putString("activity_type", "thread.post");
                                bundle.putString("type", "question.created");
                                mIMessageListPresenter.onShowActivity(bundle);
                            }
                        } catch (JSONException e) {
                        }
                }
            }
        };
    }

    private void showImageWithFullScreen(String imageUrl) {
        ArrayList<String> imageUrls = getAllMessageImageUrls();
        int index = 0;
        int size = imageUrls.size();
        for (int i = 0; i < size; i++) {
            if ((imageUrls.get(i).equals(imageUrl))) {
                index = i;
                break;
            }
        }
        Bundle bundle = new Bundle();
        bundle.putString("type", "showImage");
        bundle.putInt("index", index);
        bundle.putStringArrayList("imageList", imageUrls);
        mIMessageListPresenter.onShowActivity(bundle);
    }

    protected ArrayList<String> getAllMessageImageUrls() {
        ArrayList<String> imagesUrls = new ArrayList<>();
        int size = mListAdapter.getItemCount();
        for (int i = 0; i < size; i++) {
            MessageBody messageBody = new MessageBody(mListAdapter.getItem(i));
            if (PushUtil.ChatMsgType.IMAGE.equals(messageBody.getType())) {
                imagesUrls.add(messageBody.getBody());
            }
        }
        return imagesUrls;
    }

    protected MessageSendListener getMessageSendListener() {
        return new MessageSendListener() {

            @Override
            public void onSendMessage(String message) {
                mIMessageListPresenter.sendTextMessage(message);
            }

            @Override
            public void onSendAudio(File audioFile, int audioLength) {
                if (audioFile == null || !audioFile.exists()) {
                    SystemUtil.toast(mContext, "音频文件不存在,请重新录制");
                    return;
                }
                mIMessageListPresenter.sendAudioMessage(audioFile, audioLength);
            }

            @Override
            public void onStartRecordAudio() {
                if (mAudioPlayer != null) {
                    mAudioPlayer.stop();
                }
            }

            @Override
            public void onStopRecordAudio() {
            }

            @Override
            public void onSendImage(File imageFile) {
                mIMessageListPresenter.sendImageMessage(imageFile);
            }
        };
    }

    private void handleSelectPhotoResult(List<String> pathList) {
        if (pathList == null || pathList.isEmpty()) {
            return;
        }

        for (String path : pathList) {
            MessageHelper messageHelper = new MessageHelper(mContext);
            File file = messageHelper.compressImageByFile(path);
            messageHelper.compressTumbImageByFile(file.getAbsolutePath(), SystemUtil.getScreenWidth(mContext));
            mMessageSendListener.onSendImage(file);
        }
    }

    protected InputViewControllerListener getMessageControllerListener() {
        return new InputViewControllerListener() {
            @Override
            public void onSelectPhoto() {
                mIMessageListPresenter.selectPhoto("select");
            }

            @Override
            public void onTakePhoto() {
                mIMessageListPresenter.selectPhoto("take");
            }

            @Override
            public void onInputViewFocus(boolean isFocus) {
                if (isFocus) {
                    mMessageListView.postDelayed(mListViewScrollToBottomRunnable, 50);
                }
            }
        };
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        initView(view);
        mIMessageListPresenter.start();
    }

    public void updateListByEntity(MessageEntity messageEntity) {
        mListAdapter.updateItem(messageEntity);
    }

    @Override
    public void insertMessageList(List<MessageEntity> messageEntityList) {
        if (messageEntityList == null || messageEntityList.isEmpty()) {
            canLoadData = false;
            return;
        }

        mListAdapter.insertList(messageEntityList);
        Message msg = mUpdateHandler.obtainMessage(MESSAGE_SELECT_POSTION);
        msg.arg1 = mStart;
        mUpdateHandler.sendMessage(msg);
        mStart += messageEntityList.size();
    }

    @Override
    public void setMessageList(List<MessageEntity> messageEntityList) {
        if (messageEntityList == null) {
            canLoadData = false;
            return;
        }
        if (messageEntityList.isEmpty()) {
            canLoadData = false;
        }

        mListAdapter.setList(messageEntityList);
        mStart += messageEntityList.size();
        mUpdateHandler.obtainMessage(MESSAGE_SELECT_LAST).sendToTarget();
    }

    @Override
    public void insertMessage(MessageEntity messageEntity) {
        mListAdapter.addItem(messageEntity);
        mMessageListView.postDelayed(mListViewScrollToBottomRunnable, 50);
    }

    protected Runnable mListViewScrollToBottomRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMessageListView != null && mMessageListView.getAdapter() != null) {
                mMessageListView.smoothScrollToPosition(0);
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult :" + requestCode);
        ArrayList<String> pathList = data.getStringArrayListExtra("ImageList");
        handleSelectPhotoResult(pathList);
    }
}
