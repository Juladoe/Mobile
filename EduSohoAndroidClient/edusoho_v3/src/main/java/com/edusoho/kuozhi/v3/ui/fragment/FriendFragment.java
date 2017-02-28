package com.edusoho.kuozhi.v3.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.internal.widget.DecorToolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.FriendFragmentAdapter;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.PromiseCallback;
import com.edusoho.kuozhi.v3.model.bal.Friend;
import com.edusoho.kuozhi.v3.model.bal.UserRole;
import com.edusoho.kuozhi.v3.model.provider.FriendProvider;
import com.edusoho.kuozhi.v3.model.provider.IMProvider;
import com.edusoho.kuozhi.v3.model.result.FriendResult;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.ImChatActivity;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.ui.friend.CharacterParser;
import com.edusoho.kuozhi.v3.ui.friend.FriendComparator;
import com.edusoho.kuozhi.v3.ui.friend.FriendNewsActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.Promise;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.edusoho.kuozhi.v3.view.SideBar;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by JesseHuang on 15/4/26.
 */
public class FriendFragment extends BaseFragment {

    public static boolean isNews = false;
    private ListView mFriendList;
    private View mFootView;
    private TextView mFriendCount;
    private FriendFragmentAdapter mFriendAdapter;
    private SideBar mSidebar;
    private CharacterParser characterParser;
    private FriendComparator friendComparator;
    private TextView dialog;
    private View mLoading;
    private boolean mLoadLock;

    private FriendProvider mFriendProvider;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.fragment_friends);
        mActivity.setTitle(getString(R.string.title_friends));
        mFriendProvider = new FriendProvider(mContext);
        setHasOptionsMenu(true);
        isNews = app.config.newVerifiedNotify;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        characterParser = CharacterParser.getInstance();
        friendComparator = new FriendComparator();

        mLoading = view.findViewById(R.id.friend_fragment_loading);
        mFootView = mActivity.getLayoutInflater().inflate(R.layout.friend_list_foot, null);
        mFriendList = (ListView) mContainerView.findViewById(R.id.friends_list);
        mSidebar = (SideBar) mContainerView.findViewById(R.id.sidebar);
        dialog = (TextView) mContainerView.findViewById(R.id.dialog);
        mSidebar.setTextView(dialog);
        mSidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChangedListener(String string) {
                int postion = mFriendAdapter.getPositionForSection(string.charAt(0));
                if (postion != -1) {
                    mFriendList.setSelection(postion + 1);
                }
            }
        });
        mFriendAdapter = new FriendFragmentAdapter(mContext, app);
        mFriendAdapter.setHeadView(getFriendListHeadView());
        mFriendList.addFooterView(mFootView, null, false);
        mFriendList.setAdapter(mFriendAdapter);
        mFriendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!getAppSettingProvider().getAppConfig().isEnableIMChat) {
                    CommonUtil.longToast(mContext, "聊天功能已关闭");
                    return;
                }
                if (position == 0) {
                    return;
                }
                final Friend friend = (Friend) parent.getAdapter().getItem(position);
                app.mEngine.runNormalPlugin("ImChatActivity", mActivity, new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(ImChatActivity.FROM_ID, friend.id);
                        startIntent.putExtra(ImChatActivity.FROM_NAME, friend.nickname);
                        startIntent.putExtra(Const.NEWS_TYPE, CommonUtil.inArray(UserRole.ROLE_TEACHER.name(), friend.roles) ?
                                PushUtil.ChatUserType.TEACHER : PushUtil.ChatUserType.FRIEND);
                        startIntent.putExtra(ImChatActivity.HEAD_IMAGE_URL, friend.mediumAvatar);
                    }
                });
            }
        });

        mFriendCount = (TextView) mFootView.findViewById(R.id.friends_count);
        initViewData();
    }

    private View getFriendListHeadView() {

        View headView = LayoutInflater.from(mContext).inflate(R.layout.item_type_friend_head, null);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.search_friend_btn) {
                    MobclickAgent.onEvent(mContext, "alumni_searchBar");
                    showSearchDialog();
                }
                if (i == R.id.discussion_group) {
                    if (!getAppSettingProvider().getAppConfig().isEnableIMChat) {
                        CommonUtil.longToast(mContext, "聊天功能已关闭,请联系管理员");
                        return;
                    }
                    MobclickAgent.onEvent(mContext, "alumni_discussionGroup");
                    app.mEngine.runNormalPlugin("GroupListActivity", mActivity, null);
                }
                if (i == R.id.service) {
                    MobclickAgent.onEvent(mContext, "alumni_serviceBulletin");
                    app.mEngine.runNormalPlugin("ServiceListActivity", mActivity, null);

                }
            }
        };
        headView.findViewById(R.id.search_friend_btn).setOnClickListener(onClickListener);
        headView.findViewById(R.id.discussion_group).setOnClickListener(onClickListener);
        headView.findViewById(R.id.service).setOnClickListener(onClickListener);

        return headView;
    }

    private void showSearchDialog() {
        SearchDialogFragment searchDialogFragment = new SearchDialogFragment();
        searchDialogFragment.show(getChildFragmentManager(), "searchDialog");
    }

    private void initViewData() {
        mLoading.setVisibility(View.VISIBLE);
        if (!app.getNetIsConnect()) {
            mLoading.setVisibility(View.GONE);
            Toast.makeText(mContext, "无网络连接", Toast.LENGTH_LONG).show();
        } else {
            mFriendAdapter.clearList();
            mFriendAdapter.notifyDataSetChanged();
        }
        loadFriend().then(new PromiseCallback() {
            @Override
            public Promise invoke(Object obj) {
                mLoading.setVisibility(View.GONE);
                return null;
            }
        });
    }

    public synchronized Promise loadFriend() {
        final Promise promise = new Promise();
        if (mLoadLock) {
            return promise;
        }
        mLoadLock = true;
        mFriendProvider.getFriendList()
                .success(new NormalCallback<FriendResult>() {
                    @Override
                    public void success(FriendResult friendResult) {
                        mLoadLock = false;
                        if (friendResult.data.length != 0) {
                            List<Friend> list = Arrays.asList(friendResult.data);
                            setChar(list);
                            Collections.sort(list, friendComparator);
                            mFriendAdapter.clearList();
                            mFriendAdapter.addFriendList(list);
                            new IMProvider(mContext).updateRoles(list);
                        }
                        setFriendsCount(friendResult.data.length + "");
                        promise.resolve(friendResult);
                    }
                }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                mLoadLock = false;
            }
        });

        return promise;
    }

    public void setChar(List<Friend> list) {
        for (Friend friend : list) {
            String pinyin = characterParser.getSelling(friend.nickname);
            String sortString = pinyin.substring(0, 1).toUpperCase();
            if (sortString.matches("[A-Z]")) {
                friend.setSortLetters(sortString.toUpperCase());
            } else {
                friend.setSortLetters("#");
            }
        }
    }

    public void setFriendsCount(String count) {
        mFriendCount.setText(count + "位好友");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            mActivity.setTitle(getString(R.string.title_friends));
            if (mLoading.getVisibility() == View.GONE) {
                loadFriend();
            }
        }
        super.onHiddenChanged(hidden);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friends_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (isNews) {
            menu.findItem(R.id.friends_news).setIcon(R.drawable.icon_menu_notification_news);
        } else {
            menu.findItem(R.id.friends_news).setIcon(R.drawable.icon_menu_notification);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.friends_news) {
            isNews = false;
            app.config.newVerifiedNotify = false;
            app.saveConfig();
            item.setIcon(R.drawable.icon_menu_notification);
            mActivity.supportInvalidateOptionsMenu();
            MobclickAgent.onEvent(mContext, "alumni_friendsNotificationBellButton");
            app.mEngine.runNormalPlugin("FriendNewsActivity", mActivity, null);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void invoke(WidgetMessage message) {
        MessageType messageType = message.type;
        if (messageType.type.equals(Const.LOGIN_SUCCESS)) {
            initViewData();
        }
        if (messageType.type.equals(Const.REFRESH_FRIEND_LIST)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    initViewData();
                }
            });
        }
        if (messageType.type.equals(Const.THIRD_PARTY_LOGIN_SUCCESS)) {
            initViewData();
        }
        if (messageType.type.equals(Const.DELETE_FRIEND)){
            initViewData();
        }
        if (messageType.code == Const.NEW_FANS) {
            isNews = true;
            FriendNewsActivity.isNews = true;
            mActivity.supportInvalidateOptionsMenu();
        }
    }

    @Override
    public MessageType[] getMsgTypes() {
        String source = this.getClass().getSimpleName();
        MessageType[] messageTypes = {new MessageType(Const.LOGIN_SUCCESS)
                , new MessageType(Const.REFRESH_FRIEND_LIST)
                , new MessageType(Const.NEW_FANS, source)
                , new MessageType(Const.THIRD_PARTY_LOGIN_SUCCESS)
                , new MessageType(Const.DELETE_FRIEND)};
        return messageTypes;
    }

    public View getToolbarView() {

        View view = null;
        try {
            ActionBar actionBar = mActivity.getSupportActionBar();
            Field toolbarField = actionBar.getClass().getDeclaredField("mDecorToolbar");
            toolbarField.setAccessible(true);
            DecorToolbar toolbar = (DecorToolbar) toolbarField.get(actionBar);
            view = toolbar.getViewGroup();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return view;
    }
}
