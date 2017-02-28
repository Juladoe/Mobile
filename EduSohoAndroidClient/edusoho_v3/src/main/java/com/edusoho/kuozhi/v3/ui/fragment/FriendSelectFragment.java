package com.edusoho.kuozhi.v3.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.v3.adapter.FriendFragmentAdapter;
import com.edusoho.kuozhi.v3.handler.ChatSendHandler;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.Friend;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.bal.push.RedirectBody;
import com.edusoho.kuozhi.v3.model.provider.FriendProvider;
import com.edusoho.kuozhi.v3.model.provider.IMProvider;
import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import com.edusoho.kuozhi.v3.model.provider.UserProvider;
import com.edusoho.kuozhi.v3.model.result.FriendResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.FragmentPageActivity;
import com.edusoho.kuozhi.v3.ui.friend.CharacterParser;
import com.edusoho.kuozhi.v3.ui.friend.FriendComparator;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.SideBar;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by howzhi on 15/9/30.
 */
public class FriendSelectFragment extends AbstractChatSendFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    public static final String BODY = "body";

    private TextView mCurrentFriendTagView;
    private SideBar mSidebar;
    private FriendProvider mFriendProvider;
    private ListView mFriendListView;

    protected String mConvNo;
    protected View mGroupSelectBtn;
    protected FriendFragmentAdapter mFriendAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.frient_select_layout);
        ModelProvider.init(mContext, this);
    }

    private void initBundleData() {
        Bundle bundle = getArguments();
        mRedirectBody = (RedirectBody) bundle.getSerializable(BODY);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        initBundleData();
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        mGroupSelectBtn = mContainerView.findViewById(R.id.select_group_btn);
        mFriendListView = (ListView) mContainerView.findViewById(R.id.friends_list);
        mSidebar = (SideBar) mContainerView.findViewById(R.id.sidebar);
        mCurrentFriendTagView = (TextView) mContainerView.findViewById(R.id.dialog);

        mFriendAdapter = new FriendFragmentAdapter(mContext, app);
        mFriendListView.setAdapter(mFriendAdapter);

        mSidebar.setTextView(mCurrentFriendTagView);
        mSidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChangedListener(String string) {
                int postion = mFriendAdapter.getPositionForSection(string.charAt(0));
                if (postion != -1) {
                    mFriendListView.setSelection(postion + 1);
                }
            }
        });

        mGroupSelectBtn.setOnClickListener(this);
        mFriendListView.setOnItemClickListener(this);

        initFriendListData();
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = app.mEngine.runPluginWithFragmentByBundle(
                "GroupSelectFragment", mActivity, getArguments());
        fragmentTransaction.addToBackStack("GroupSelectFragment");
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Friend friend = (Friend) mFriendAdapter.getItem(position);
        RedirectBody redirectBody = getShowRedirectBody(friend.getNickname(), friend.getMediumAvatar());
        ChatSendHandler chatSendHandler = new ChatSendHandler(mActivity, redirectBody, position);
        chatSendHandler.handleClick(mSendMessageHandlerCallback);
    }

    private NormalCallback<Integer> mSendMessageHandlerCallback = new NormalCallback<Integer>() {
        @Override
        public void success(Integer index) {
            Friend friend = (Friend) mFriendAdapter.getItem(index);
            ConvEntity convEntity = IMClient.getClient().getConvManager()
                    .getConvByTypeAndId(Destination.USER, friend.id);
            if (convEntity == null) {
                createChatConvNo(friend.id);
                return;
            }
            sendMsg(friend.id, convEntity.getConvNo(), Destination.USER);
        }
    };

    protected void sendMsg(int fromId, String convNo, String type) {
        MessageBody messageBody = saveMessageToLoacl(fromId, convNo, type);
        sendMessageToServer(convNo, messageBody);
    }

    protected void createChatConvNo(final int fromId) {
        final LoadDialog loadDialog = LoadDialog.create(mActivity);
        loadDialog.show();

        User currentUser = getAppSettingProvider().getCurrentUser();
        new UserProvider(mContext).createConvNo(new int[]{currentUser.id, fromId})
                .success(new NormalCallback<LinkedHashMap>() {
                    @Override
                    public void success(LinkedHashMap linkedHashMap) {
                        if (linkedHashMap == null || (mConvNo = linkedHashMap.get("no").toString()) == null) {
                            ToastUtils.show(mActivity.getBaseContext(), "创建聊天失败!");
                            return;
                        }

                        new IMProvider(mContext).createConvInfoByUser(mConvNo, fromId)
                                .success(new NormalCallback<ConvEntity>() {
                                    @Override
                                    public void success(ConvEntity convEntity) {
                                        loadDialog.dismiss();
                                        sendMsg(fromId, mConvNo, Destination.USER);
                                    }
                                });
                    }});
    }

    protected void initFriendListData() {
        RequestUrl requestUrl = app.bindNewUrl(Const.MY_FRIEND, true);
        StringBuffer stringBuffer = new StringBuffer(requestUrl.url);
        stringBuffer.append("?start=0&limit=10000/");
        requestUrl.url = stringBuffer.toString();
        mFriendProvider.getFriend(requestUrl).success(new NormalCallback<FriendResult>() {
            @Override
            public void success(FriendResult result) {
                if (result.data.length != 0) {
                    List<Friend> list = Arrays.asList(result.data);
                    setChar(list);
                    Collections.sort(list, new FriendComparator());
                    mFriendAdapter.addFriendList(list);
                }
            }
        });
    }

    protected <T extends Friend> void setChar(List<T> list) {
        for (Friend friend : list) {
            String pinyin = CharacterParser.getInstance().getSelling(friend.getNickname());
            String sortString = pinyin.substring(0, 1).toUpperCase();
            if (sortString.matches("[A-Z]")) {
                friend.setSortLetters(sortString.toUpperCase());
            } else {
                friend.setSortLetters("#");
            }
        }
    }

    @Override
    public String getTitle() {
        return "选择校友";
    }

    @Override
    public void onResume() {
        FragmentPageActivity activity = (FragmentPageActivity) getActivity();
        activity.setBackMode(FragmentPageActivity.BACK, getTitle());
        super.onResume();
    }

    @Override
    protected void sendSuccessCallback() {
        mActivity.setResult(ChatSendHandler.RESULT_SELECT_FRIEND_OK);
        mActivity.finish();
    }

    @Override
    protected void sendFailCallback() {
    }
}
