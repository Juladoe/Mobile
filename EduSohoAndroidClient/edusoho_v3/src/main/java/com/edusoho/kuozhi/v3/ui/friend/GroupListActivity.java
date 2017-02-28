package com.edusoho.kuozhi.v3.ui.friend;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.v3.adapter.FriendFragmentAdapter;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PromiseCallback;
import com.edusoho.kuozhi.v3.model.bal.DiscussionGroup;
import com.edusoho.kuozhi.v3.model.provider.DiscussionGroupProvider;
import com.edusoho.kuozhi.v3.model.provider.IMProvider;
import com.edusoho.kuozhi.v3.model.result.DiscussionGroupResult;
import com.edusoho.kuozhi.v3.ui.ClassroomDiscussActivity;
import com.edusoho.kuozhi.v3.ui.NewsCourseActivity;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Promise;
import com.edusoho.kuozhi.v3.view.SideBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Melomelon on 2015/9/10.
 */
public class GroupListActivity extends ActionBarBaseActivity {

    private final String TAG = "GroupList";
    private ListView mListView;

    private FriendFragmentAdapter mAdapter;

    private SideBar mSidebar;
    private TextView mCharTextView;
    private FrameLayout mEmptyNotice;
    private FrameLayout mLoading;

    private DiscussionGroupProvider mDiscussionGroupProvider;
    private ArrayList<DiscussionGroup> mGroupList = new ArrayList<DiscussionGroup>();

    private CharacterParser characterParser;
    private GroupComparator groupComparator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackMode(BACK, "讨论组");
        setContentView(R.layout.group_list_layout);
        mListView = (ListView) findViewById(R.id.group_listview);
        mSidebar = (SideBar) findViewById(R.id.group_list_sidebar);
        mCharTextView = (TextView) findViewById(R.id.sidebar_char_hint);
        mEmptyNotice = (FrameLayout) findViewById(R.id.empty_discussion_group);
        mLoading = (FrameLayout) findViewById(R.id.discussion_group_loading);
        mSidebar.setTextView(mCharTextView);
        mSidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChangedListener(String string) {
                int position = mAdapter.getPositionForSection(string.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });
        mAdapter = new FriendFragmentAdapter(getBaseContext(), app);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!getAppSettingProvider().getAppConfig().isEnableIMChat) {
                    CommonUtil.longToast(mContext, "聊天功能已关闭,请联系管理员");
                    return;
                }
                final DiscussionGroup discussionGroup = (DiscussionGroup) parent.getItemAtPosition(position);
                Bundle bundle = new Bundle();
                if (Destination.COURSE.equals(discussionGroup.getType())) {
                    bundle.putInt(NewsCourseActivity.COURSE_ID, discussionGroup.id);
                    bundle.putString(NewsCourseActivity.CONV_NO, discussionGroup.getConversationId());
                    bundle.putInt(NewsCourseActivity.SHOW_TYPE, NewsCourseActivity.DISCUSS_TYPE);
                    bundle.putString(NewsCourseActivity.TARGET_TYPE, discussionGroup.getType());
                    CoreEngine.create(mContext).runNormalPluginWithBundle("NewsCourseActivity", mContext, bundle);
                    return;
                }

                bundle.putInt(ClassroomDiscussActivity.FROM_ID, discussionGroup.id);
                bundle.putString(ClassroomDiscussActivity.FROM_NAME, discussionGroup.title);
                bundle.putString(ClassroomDiscussActivity.TARGET_TYPE, discussionGroup.getType());
                CoreEngine.create(mContext).runNormalPluginWithBundle("ClassroomDiscussActivity", mContext, bundle);
            }
        });
        characterParser = CharacterParser.getInstance();
        groupComparator = new GroupComparator();
        mDiscussionGroupProvider = new DiscussionGroupProvider(mContext);

        mLoading.setVisibility(View.VISIBLE);
        mEmptyNotice.setVisibility(View.GONE);
        if (mGroupList.size() != 0) {
            mAdapter.clearList();
            mAdapter.notifyDataSetChanged();
        }
        if (!app.getNetIsConnect()) {
            mLoading.setVisibility(View.GONE);
            Toast.makeText(mContext, "无网络连接", Toast.LENGTH_LONG).show();
        }
        loadGroup().then(new PromiseCallback() {
            @Override
            public Promise invoke(Object obj) {
                mLoading.setVisibility(View.GONE);
                return null;
            }
        });
    }

    private Promise loadGroup() {
        final Promise promise = new Promise();
        mDiscussionGroupProvider.getGroupList().success(new NormalCallback<DiscussionGroupResult>() {
            @Override
            public void success(DiscussionGroupResult discussionGroupResult) {
                if (discussionGroupResult.resources.length != 0) {
                    DiscussionGroup[] groups = discussionGroupResult.resources;
                    List<DiscussionGroup> groupsList = Arrays.asList(groups);
                    setSortChar(groupsList);
                    Collections.sort(groupsList, groupComparator);
                    mAdapter.addFriendList(groupsList);
                    new IMProvider(mContext).updateRoles(groupsList);
                } else {
                    mEmptyNotice.setVisibility(View.VISIBLE);
                }
                promise.resolve(discussionGroupResult);
            }
        });
        return promise;
    }

    public void setSortChar(List<DiscussionGroup> groupList) {
        for (DiscussionGroup discussionGroup : groupList) {
            discussionGroup.nickname = discussionGroup.title;
            discussionGroup.mediumAvatar = discussionGroup.picture;
            String pinyin = characterParser.getSelling(discussionGroup.title);
            String sortString = pinyin.substring(0, 1).toUpperCase();
            if (sortString.matches("[A-Z]")) {
                discussionGroup.setSortLetters(sortString.toUpperCase());
            } else {
                discussionGroup.setSortLetters("#");
            }
        }
    }
}
