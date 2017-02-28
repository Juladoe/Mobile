package com.edusoho.longinus.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.edusoho.kuozhi.imserver.broadcast.IMBroadcastReceiver;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.listener.IConnectManagerListener;
import com.edusoho.kuozhi.imserver.managar.IMRoleManager;
import com.edusoho.kuozhi.imserver.ui.IMessageListPresenter;
import com.edusoho.kuozhi.imserver.ui.MessageListFragment;
import com.edusoho.kuozhi.imserver.ui.adapter.MessageRecyclerListAdapter;
import com.edusoho.kuozhi.imserver.ui.helper.MessageResourceHelper;
import com.edusoho.kuozhi.imserver.ui.listener.MessageControllerListener;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.ui.fragment.ViewPagerFragment;
import com.edusoho.kuozhi.v3.util.ActivityUtil;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.longinus.PLMediaPlayerActivity;
import com.edusoho.longinus.PLVideoViewActivity;
import com.edusoho.longinus.R;
import com.edusoho.longinus.adapter.LiveChatListAdapter;
import com.edusoho.longinus.data.LiveChatDataProvider;
import com.edusoho.longinus.data.LiveRoomProvider;
import com.edusoho.longinus.persenter.ILiveChatPresenter;
import com.edusoho.longinus.persenter.ILiveVideoPresenter;
import com.edusoho.longinus.persenter.LiveChatMessageListPresenterImpl;
import com.edusoho.longinus.persenter.LiveChatPresenterImpl;
import com.edusoho.longinus.persenter.LiveVideoPresenterImpl;
import com.edusoho.longinus.util.LiveIMBroadcastReceiver;
import com.edusoho.longinus.util.LiveImClient;
import com.umeng.analytics.MobclickAgent;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by suju on 16/10/12.
 */
public class LessonLivePlayerActivity extends PLMediaPlayerActivity implements ILiveVideoView {

    private Context mContext;
    private int mLessonId;
    private String mConversationNo;
    private String mRole;
    private String mToken;
    private String mClientId;
    private String mClientName;
    private String mRoomNo;
    private String mPlayUrl;
    private String mLiveHost;
    private String mLiveTitle;
    private boolean mIsBan;

    private ILiveVideoPresenter mILiveVideoPresenter;
    private ILiveChatPresenter mILiveChatPresenter;
    protected IMessageListPresenter mIMessageListPresenter;
    protected MessageListFragment mMessageListFragment;
    private IMBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtil.setStatusBarTranslucent(this);
    }

    private void initParams() {
        mConversationNo = getIntent().getStringExtra("convNo");
        mRole = getIntent().getStringExtra("role");
        mToken = getIntent().getStringExtra("token");
        mRoomNo = getIntent().getStringExtra("roomNo");
        mPlayUrl = getIntent().getStringExtra("playUrl");

        if (!TextUtils.isEmpty(mPlayUrl)) {
            mPlayUrl = URLDecoder.decode(mPlayUrl);
        }
        mClientId = getIntent().getStringExtra("clientId");
        mClientName = getIntent().getStringExtra("clientName");
        mLiveTitle = getIntent().getStringExtra("title");
        mLiveHost = getIntent().getStringExtra("liveHost");
        mLessonId = AppUtil.parseInt(getIntent().getStringExtra("lessonId"));
    }

    @Override
    protected void onDestroy() {
        LiveImClient liveImClient = LiveImClient.getIMClient(mContext);
        if (liveImClient != null) {
            liveImClient.destory();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMessageListFragment == null) {
            return;
        }
        if (mILiveVideoPresenter != null) {
            mILiveVideoPresenter.handleHistorySignals();
            mILiveVideoPresenter.updateLiveNotice(false);
        }
        registIMReceiver();
    }

    private void registIMReceiver() {
        mReceiver = new LiveIMBroadcastReceiver(mConversationNo, mILiveVideoPresenter, mILiveChatPresenter, mIMessageListPresenter);
        mContext.registerReceiver(mReceiver, new IntentFilter(IMBroadcastReceiver.ACTION_NAME));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
        }
        mReceiver = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.live_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem noticeItem = menu.findItem(R.id.menu_notice);
        if (noticeItem != null) {
            int currentOrientation = getResources().getConfiguration().orientation;
            noticeItem.setVisible(currentOrientation == Configuration.ORIENTATION_PORTRAIT);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_notice) {
            MobclickAgent.onEvent(mContext, "liverRoom_announcementButton");
            Intent intent = new Intent(mContext, LiveNoticeListActivity.class);
            intent.putExtra(LiveNoticeListActivity.TOKEN, mToken);
            intent.putExtra(LiveNoticeListActivity.LIVE_HOST, mLiveHost);
            intent.putExtra(LiveNoticeListActivity.ROOM_NO, mRoomNo);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        invalidateOptionsMenu();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void setRoomPrepareStatus(int status) {
        setLiveChatLoadShowStatus(status != IConnectManagerListener.OPEN ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onLeaveRoom() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("退出教室")
                .setMessage("您已被移出直播教室")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.show();
    }

    @Override
    public void setNotice(String notice) {
        mNoticeView.setText(notice);
        mNoticeView.requestFocus();
    }

    @Override
    public void hideNoticeView() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mNoticeView, "alpha",  1.0f, 0.0f);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.setDuration(500);
        objectAnimator.start();
    }

    @Override
    public synchronized void showNoticeView() {
        if (mNoticeView.getTag() != null) {
            return;
        }
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mNoticeView, "alpha",  0.0f, 1.0f);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.setDuration(360);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mNoticeView.setTag(null);
            }
        });
        mNoticeView.setTag(objectAnimator);
        objectAnimator.start();
    }

    @Override
    public void setLivePlayStatus(String staus) {
        if (PAUSE.equals(staus)) {
            pauseLive();
        } else if (LIVE.equals(staus)) {
            if (TextUtils.isEmpty(getViewPath())) {
                checkLivePlayStatus();
                return;
            }
            resumeLive();
        } else if (CLOSE.equals(staus)) {
            setPlayStatus(CLOSE);
        }
    }

    private void initChatRoom() {
        Bundle params = getIntent().getExtras();
        mILiveChatPresenter = new LiveChatPresenterImpl(mContext, this, params);
        mILiveChatPresenter.connectLiveChatServer();
    }

    @Override
    public void showChatRoomLoadView(String title) {
        setLiveChatLoadContentStatus(View.VISIBLE, title);
    }

    @Override
    public void hideChatRoomLoadView() {
        setLiveChatLoadShowStatus(View.INVISIBLE);
    }

    @Override
    public void addChatRoomView() {
        attachMessageListFragment();
        mILiveVideoPresenter = new LiveVideoPresenterImpl(
                mContext, getIntent().getExtras(), this, mMessageListFragment);
        mILiveVideoPresenter.handleHistorySignals();
        mILiveVideoPresenter.updateLiveNotice(true);

        registIMReceiver();
    }

    @Override
    public void checkLivePlayStatus() {
        new LiveRoomProvider(mContext).getLiveRoom(mLiveHost, mToken, mRoomNo).success(new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap data) {
                String status = data.get("status").toString();
                if (data.containsKey("ban")) {
                    mIsBan = (boolean) data.get("ban");
                    if (mMessageListFragment != null) {
                        mMessageListFragment.setEnable(!mIsBan);
                    }
                }
                if (CLOSE.equals(status)) {
                    setPlayStatus(CLOSE);
                    return;
                }
                if (NOT_START.equals(status)) {
                    setPlayStatus(NOT_START);
                    return;
                }
                if (PAUSE.equals(status)) {
                    setPlayStatus(PAUSE);
                    return;
                }
                startPlay(mPlayUrl);
            }
        });
    }

    @Override
    protected void initView() {
        mContext = getBaseContext();
        initParams();
        super.initView();
        setLiveTitle(mLiveTitle);
        setLiveDesc(null);
        initChatRoom();
        checkLivePlayStatus();
    }

    protected IMessageListPresenter createProsenter(MessageListFragment messageListFragment) {
        Bundle bundle = new Bundle();
        bundle.putString(MessageListFragment.CONV_NO, mConversationNo);
        bundle.putInt(MessageListFragment.TARGET_ID, mLessonId);
        bundle.putString(MessageListFragment.TARGET_TYPE, "live_chatroom");

        LiveChatDataProvider liveChatDataProvider = new LiveChatDataProvider(mContext);
        LiveChatMessageListPresenterImpl presenter = new LiveChatMessageListPresenterImpl(
                mContext,
                bundle,
                new LiveChatDataProvider.MockConvManager(mContext),
                new IMRoleManager(mContext),
                new MessageResourceHelper(mContext),
                liveChatDataProvider,
                messageListFragment
        );
        presenter.setLiveData(getIntent().getExtras());

        return presenter;
    }

    protected void attachMessageListFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("im_container");

        if (fragment != null) {
            mMessageListFragment = (MessageListFragment) fragment;
        } else {
            mMessageListFragment = createFragment();
            fragmentTransaction.add(R.id.chat_content, mMessageListFragment, "im_container");
            fragmentTransaction.commitAllowingStateLoss();
        }
        mIMessageListPresenter = createProsenter(mMessageListFragment);
        mIMessageListPresenter.addMessageControllerListener(getMessageControllerListener());
        mMessageListFragment.setEnable(!mIsBan);

        mILiveChatPresenter.setView(mMessageListFragment);
    }

    protected MessageControllerListener getMessageControllerListener() {
        return new MessageControllerListener() {

            @Override
            public void onShowImage(int index, ArrayList<String> imageList) {
                Bundle bundle = new Bundle();
                bundle.putInt("index", index);
                bundle.putStringArrayList("imageList", imageList);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                ViewPagerFragment viewPagerFragment = new ViewPagerFragment();
                viewPagerFragment.setArguments(bundle);
                fragmentTransaction.setCustomAnimations(R.anim.window_zoom_open, R.anim.window_zoom_exit);
                viewPagerFragment.show(fragmentTransaction, "viewpager");
            }

            @Override
            public void onShowUser(Role role) {
            }

            @Override
            public void onShowWebPage(String url) {
                Bundle bundle = new Bundle();
                bundle.putString(Const.WEB_URL, url);
                CoreEngine.create(mContext).runNormalPluginWithBundle("WebViewActivity", mContext, bundle);
            }

            @Override
            public void selectPhoto() {
            }

            @Override
            public void takePhoto() {
            }

            @Override
            public void onShowActivity(Bundle bundle) {
            }
        };
    }

    protected MessageListFragment createFragment() {
        MessageListFragment messageListFragment = (MessageListFragment) Fragment.instantiate(
                getBaseContext(), MessageListFragment.class.getName());
        MessageRecyclerListAdapter messageRecyclerListAdapter = new LiveChatListAdapter(mContext);
        messageRecyclerListAdapter.setCurrentId(AppUtil.parseInt(mClientId));
        messageListFragment.setAdapter(messageRecyclerListAdapter);
        return messageListFragment;
    }
}
