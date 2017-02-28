package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.StatusCallback;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.provider.IMProvider;
import com.edusoho.kuozhi.v3.model.provider.IMServiceProvider;
import com.edusoho.kuozhi.v3.model.provider.SystemProvider;
import com.edusoho.kuozhi.v3.model.sys.AppConfig;
import com.edusoho.kuozhi.v3.model.sys.AppUpdateInfo;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.VolleySingleton;
import com.edusoho.kuozhi.v3.view.EduSohoTextBtn;
import com.edusoho.kuozhi.v3.view.dialog.PopupDialog;
import com.edusoho.kuozhi.v3.view.webview.ESWebViewRequestManager;
import com.umeng.analytics.MobclickAgent;

import java.util.LinkedHashMap;
import java.util.Queue;

/**
 * Created by JesseHuang on 15/4/24.
 */
public class DefaultPageActivity extends ActionBarBaseActivity implements MessageEngine.MessageCallback {
    public static final String TAG = "DefaultPageActivity";
    public static final int LOGIN_CANCEL = 0x001;
    private String mCurrentTag;
    private int mSelectBtn;
    private LinearLayout mNavLayout;
    private EduSohoTextBtn mDownTabNews;
    private EduSohoTextBtn mDownTabFind;
    private EduSohoTextBtn mDownTabFriends;
    private EduSohoTextBtn mDownTabMine;
    private Toolbar tbActionBar;
    private TextView tvTitle;
    private TextView tvSitting;
    private View viewTitleLoading;
    private NavDownTabClickListener mNavDownTabClickListener;
    private Queue<Request<String>> mAjaxQueue;
    private boolean mLogoutFlag = false;
    private View vToolbarBreakline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        initView();
        AppUtil.checkUpateApp(mActivity, new StatusCallback<AppUpdateInfo>() {
            @Override
            public void success(AppUpdateInfo obj) {
                if (obj.show) {
                    showUpdateDlg(obj);
                }
                app.addNotify("app_update", null);
            }
        });
        if (getIntent().hasExtra(Const.INTENT_TARGET) || getIntent().hasExtra(Const.SWITCH_NEWS_TAB)) {
            processIntent(getIntent());
        }

        syncSchoolIMSetting();
    }

    private void syncSchoolIMSetting() {
        User user = getAppSettingProvider().getCurrentUser();
        if (user == null) {
            return;
        }
        new SystemProvider(mContext).getIMSetting()
                .success(new NormalCallback<LinkedHashMap>() {
                    @Override
                    public void success(LinkedHashMap linkedHashMap) {
                        AppConfig appConfig = getAppSettingProvider().getAppConfig();
                        boolean isEnableIMChat = false;
                        if (linkedHashMap != null && linkedHashMap.containsKey("enabled")) {
                            isEnableIMChat = AppConfig.IM_OPEN.equals(linkedHashMap.get("enabled"));
                        }
                        if (appConfig.isEnableIMChat != isEnableIMChat) {
                            appConfig.isEnableIMChat = isEnableIMChat;
                            getAppSettingProvider().saveConfig(appConfig);
                        }

                        if (isEnableIMChat) {
                            reConnectServer();
                        }
                    }
                }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError volleyError) {
                if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {
                    return;
                }
                AppConfig appConfig = getAppSettingProvider().getAppConfig();
                appConfig.isEnableIMChat = false;
                getAppSettingProvider().saveConfig(appConfig);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
        setIntent(intent);
    }

    private void processIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (intent.hasExtra(Const.INTENT_TARGET)) {
            Class target = (Class) intent.getSerializableExtra(Const.INTENT_TARGET);
            Intent targetIntent = new Intent(mContext, target);
            targetIntent.putExtras(intent.getExtras());
            targetIntent.setFlags(intent.getFlags());
            startActivity(targetIntent);
        } else if (!intent.hasExtra(Const.SWITCH_NEWS_TAB)) {
            selectDownTab(R.id.nav_tab_find);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    private void initView() {
        mNavLayout = (LinearLayout) findViewById(R.id.nav_bottom_layout);
        mDownTabNews = (EduSohoTextBtn) findViewById(R.id.nav_tab_news);
        mDownTabFind = (EduSohoTextBtn) findViewById(R.id.nav_tab_find);
        mDownTabFriends = (EduSohoTextBtn) findViewById(R.id.nav_tab_friends);
        mDownTabMine = (EduSohoTextBtn) findViewById(R.id.nav_tab_mine);
        tbActionBar = (Toolbar) findViewById(R.id.tb_action_bar);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvSitting = (TextView) findViewById(R.id.tv_sitting);
        viewTitleLoading = findViewById(R.id.ll_title_loading);
        vToolbarBreakline = findViewById(R.id.v_line);
        setSupportActionBar(tbActionBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mNavDownTabClickListener = new NavDownTabClickListener();

        mToast = Toast.makeText(getApplicationContext(), getString(R.string.app_exit_msg), Toast.LENGTH_SHORT);

        int count = mNavLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mNavLayout.getChildAt(i);
            child.setOnClickListener(mNavDownTabClickListener);
        }

        User user = getAppSettingProvider().getCurrentUser();
        if (user != null) {
            selectDownTab(R.id.nav_tab_news);
        } else {
            selectDownTab(R.id.nav_tab_find);
        }
        mDownTabNews.setUpdateIcon(0);
        if (app.config.newVerifiedNotify) {
            mDownTabFriends.setBageIcon(true);
        }
        tvSitting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(mContext, "i_settings");
                mActivity.app.mEngine.runNormalPlugin("SettingActivity", mContext, null);
            }
        });
    }

    @Override
    public void setTitle(CharSequence title) {
        tvTitle.setText(title);
    }

    public void setTitleLoading(boolean isLoading) {
        if (isLoading) {
            tvTitle.setVisibility(View.GONE);
            viewTitleLoading.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            viewTitleLoading.setVisibility(View.GONE);
        }
    }

    public String getCurrentFragment() {
        return mCurrentTag;
    }

    private class NavDownTabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            selectDownTab(v.getId());
        }
    }

    private void selectDownTab(int id) {
        String tag;
        if (app.loginUser == null && id != R.id.nav_tab_find) {
            app.mEngine.runNormalPluginWithAnim("LoginActivity", mContext, null, new NormalCallback() {
                @Override
                public void success(Object obj) {
                    mActivity.overridePendingTransition(R.anim.down_to_up, R.anim.none);
                }
            });
            return;
        }
        mActionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary_color)));
        tvTitle.setTextColor(Color.parseColor("#ffffff"));
        tvSitting.setVisibility(View.GONE);
        vToolbarBreakline.setVisibility(View.GONE);
        if (id == R.id.nav_tab_news) {
            tag = "NewsFragment";
            setTitle(getString(R.string.title_news));
            setTitleLoading(true);
        } else if (id == R.id.nav_tab_find) {
            tag = "FindFragment";
            setTitle(getSchoolTitle());
            setTitleLoading(false);
        } else if (id == R.id.nav_tab_friends) {
            tag = "FriendFragment";
            setTitle(getString(R.string.title_friends));
            setTitleLoading(false);
        } else {
            MobclickAgent.onEvent(this, "i_userInformationPortal");
            tag = "MineFragment";
            setTitle(getString(R.string.title_mine));
            mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ccf9f9f9")));
            tvTitle.setTextColor(getResources().getColor(R.color.primary_font_color));
            tvSitting.setVisibility(View.VISIBLE);
            vToolbarBreakline.setVisibility(View.VISIBLE);
            setTitleLoading(false);
        }
        if (tag.equals(mCurrentTag)) {
            return;
        }
        hideFragment(mCurrentTag);
        showFragment(tag);
        changeNavBtn(id);
        changeBtnIcon(id);
        mSelectBtn = id;
    }

    protected String getSchoolTitle() {
        return app.defaultSchool == null ? getString(R.string.title_find) : app.defaultSchool.name;
    }

    private void hideFragment(String tag) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag(tag);
        if (fragment != null) {
            fragmentTransaction.hide(fragment);
            if (!mLogoutFlag) {
                fragmentTransaction.commit();
            } else {
                fragmentTransaction.commitAllowingStateLoss();
            }
        }
    }

    private void showFragment(String tag) {
        Fragment fragment;
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragment = mFragmentManager.findFragmentByTag(tag);

        if (fragment != null) {
            fragmentTransaction.show(fragment);
        } else {
            fragment = app.mEngine.runPluginWithFragment(tag, mActivity, null);
            fragmentTransaction.add(R.id.fragment_container, fragment, tag);
        }

        if (!mLogoutFlag) {
            fragmentTransaction.commit();
        } else {
            fragmentTransaction.commitAllowingStateLoss();
        }
        mCurrentTag = tag;
    }

    private void changeNavBtn(int id) {
        int count = mNavLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mNavLayout.getChildAt(i);
            if (child.getId() == id) {
                child.setEnabled(false);
            } else {
                child.setEnabled(true);
            }
        }
    }

    private void changeBtnIcon(int id) {
        mDownTabNews.setIcon(getResources().getString(R.string.font_news));
        mDownTabFind.setIcon(getResources().getString(R.string.font_find));
        mDownTabFriends.setIcon(getResources().getString(R.string.font_friends));
        mDownTabMine.setIcon(getResources().getString(R.string.font_mine));
        if (id == R.id.nav_tab_news) {
            mDownTabNews.setIcon(getResources().getString(R.string.font_news_pressed));
        } else if (id == R.id.nav_tab_find) {
            mDownTabFind.setIcon(getResources().getString(R.string.font_find_pressed));
        } else if (id == R.id.nav_tab_friends) {
            mDownTabFriends.setIcon(getResources().getString(R.string.font_friends_pressed));
        } else if (id == R.id.nav_tab_mine) {
            mDownTabMine.setIcon(getResources().getString(R.string.font_mine_pressed));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            Log.d("onOptionsItemSelected", "home");
        }
        return false;
    }

    @Override
    public void invoke(final WidgetMessage message) {
        processMessage(message);
        final MessageType messageType = message.type;
        switch (messageType.code) {
            case Const.OPEN_COURSE_CHAT:
                app.mEngine.runNormalPlugin("ChatActivity", mContext, null);
                break;
            case Const.SWITCH_TAB:
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mLogoutFlag = true;
                            selectDownTab(R.id.nav_tab_find);
                            mLogoutFlag = false;
                        } catch (Exception ex) {
                            Log.d(TAG, ex.getMessage());
                        }
                    }
                });
                break;
            default:
        }

        if (messageType.type.equals(Const.BADGE_UPDATE)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mDownTabNews.setUpdateIcon(message.data.getInt("badge"));
                }
            });
            return;
        }
        if (messageType.type.equals(Const.LOGIN_SUCCESS)) {
            mLogoutFlag = true;
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    syncSchoolIMSetting();
                    if (getIntent().hasExtra(Const.SWITCH_NEWS_TAB)) {
                        selectDownTab(R.id.nav_tab_find);
                    } else {
                        selectDownTab(R.id.nav_tab_news);
                    }
                    mLogoutFlag = false;
                }
            });
        }
        if (messageType.type.equals(Const.LOGOUT_SUCCESS)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mDownTabNews.setUpdateIcon(0);
                }
            });
        }
        if (messageType.code == Const.NEW_FANS) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (message.data.getBoolean("isNew")) {
                        mDownTabFriends.setBageIcon(true);
                    } else {
                        mDownTabFriends.setBageIcon(false);
                    }
                }
            });
        }
    }

    @Override
    protected void processMessage(WidgetMessage message) {
        MessageType messageType = message.type;
        if (Const.TOKEN_LOSE.equals(messageType.type)) {
            CommonUtil.longToast(getBaseContext(), getString(R.string.token_lose_notice));
            handleTokenLostMsg();
        }
    }

    @Override
    public MessageType[] getMsgTypes() {
        String source = this.getClass().getSimpleName();
        return new MessageType[]{
                new MessageType(Const.OPEN_COURSE_CHAT, source),
                new MessageType(Const.SWITCH_TAB, source),
                new MessageType(Const.LOGIN_SUCCESS),
                new MessageType(Const.LOGOUT_SUCCESS),
                new MessageType(Const.TOKEN_LOSE),
                new MessageType(Const.BADGE_UPDATE),
                new MessageType(Const.NEW_FANS)
        };
    }

    private Toast mToast;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                moveTaskToBack(true);
                return true;
            case KeyEvent.KEYCODE_MENU:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ESWebViewRequestManager.clear();
        VolleySingleton.getInstance(getApplicationContext()).cancelAll();
    }

    private void showUpdateDlg(final AppUpdateInfo result) {
        PopupDialog popupDialog = PopupDialog.createMuilt(
                mActivity,
                "版本更新",
                "更新内容\n" + result.updateInfo, new PopupDialog.PopupClickListener() {
                    @Override
                    public void onClick(int button) {
                        if (button == PopupDialog.OK) {
                            app.startUpdateWebView(result.updateUrl);
                            app.removeNotify("app_update");
                        }
                    }
                });

        popupDialog.setOkText("更新");
        popupDialog.show();
    }

    private boolean checkSchoolHasLogined(String host) {
        if (host.startsWith("http://")) {
            host = host.substring(7);
            Log.d(null, "host->" + host);
        }
        SharedPreferences sp = getSharedPreferences("search_history", MODE_PRIVATE);
        if (sp.contains(host)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        reConnectServer();
        User user = getAppSettingProvider().getCurrentUser();
        if (user != null) {
            new IMProvider(mContext).syncIM().fail(new NormalCallback<VolleyError>() {
                @Override
                public void success(VolleyError volleyError) {
                    volleyError.printStackTrace();
                }
            });
        }
    }

    private void reConnectServer() {
        User user = getAppSettingProvider().getCurrentUser();
        if (user == null) {
            return;
        }
        new IMServiceProvider(getBaseContext()).reConnectServer(user.id, user.nickname);
    }

    public AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_CANCEL) {
            selectDownTab(R.id.nav_tab_find);
        }
    }
}
