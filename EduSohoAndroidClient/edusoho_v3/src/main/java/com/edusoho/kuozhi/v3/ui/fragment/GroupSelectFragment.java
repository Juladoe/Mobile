package com.edusoho.kuozhi.v3.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.v3.handler.ClassRoomChatSendHandler;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.Classroom;
import com.edusoho.kuozhi.v3.model.bal.DiscussionGroup;
import com.edusoho.kuozhi.v3.model.bal.Friend;
import com.edusoho.kuozhi.v3.model.bal.push.RedirectBody;
import com.edusoho.kuozhi.v3.model.provider.ClassRoomProvider;
import com.edusoho.kuozhi.v3.model.provider.DiscussionGroupProvider;
import com.edusoho.kuozhi.v3.model.provider.IMProvider;
import com.edusoho.kuozhi.v3.model.result.DiscussionGroupResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.friend.FriendComparator;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by howzhi on 15/11/3.
 */
public class GroupSelectFragment extends FriendSelectFragment {

    private DiscussionGroupProvider mDiscussionGroupProvider;

    @Override
    public String getTitle() {
        return "选择讨论组";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mGroupSelectBtn.setVisibility(View.GONE);
    }

    @Override
    protected void initFriendListData() {
        RequestUrl requestUrl = app.bindNewUrl(Const.DISCUSSION_GROUP, true);
        StringBuffer stringBuffer = new StringBuffer(requestUrl.url);
        stringBuffer.append("?start=0&limit=10000/");
        requestUrl.url = stringBuffer.toString();
        mDiscussionGroupProvider.getClassrooms(requestUrl).success(
                new NormalCallback<DiscussionGroupResult>() {
                    @Override
                    public void success(DiscussionGroupResult result) {
                        if (result.resources.length != 0) {
                            List<DiscussionGroup> list = Arrays.asList(result.resources);
                            setChar(list);
                            Collections.sort(list, new FriendComparator());
                            mFriendAdapter.addFriendList(list);
                        }
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Friend friend = (Friend) mFriendAdapter.getItem(position);
        RedirectBody redirectBody = getShowRedirectBody(friend.getNickname(), friend.getMediumAvatar());
        ClassRoomChatSendHandler chatSendHandler = new ClassRoomChatSendHandler(mActivity, redirectBody, position);
        chatSendHandler.handleClick(mSendMessageHandlerCallback);
    }

    private NormalCallback<Integer> mSendMessageHandlerCallback = new NormalCallback<Integer>() {
        @Override
        public void success(Integer index) {
            Friend friend = (Friend) mFriendAdapter.getItem(index);
            ConvEntity convEntity = IMClient.getClient().getConvManager()
                    .getConvByTypeAndId(friend.getType(), friend.id);
            if (convEntity == null) {
                createChatConvNo(friend.id);
                return;
            }
            sendMsg(friend.id, convEntity.getConvNo(), Destination.USER);
        }
    };

    @Override
    protected void createChatConvNo(final int fromId) {
        final LoadDialog loadDialog = LoadDialog.create(mActivity);
        loadDialog.show();
        new ClassRoomProvider(mContext).getClassRoom(fromId)
                .success(new NormalCallback<Classroom>() {
                    @Override
                    public void success(Classroom classroom) {
                        if (classroom == null || TextUtils.isEmpty(classroom.convNo)) {
                            ToastUtils.show(mActivity.getBaseContext(), "发送失败,该讨论组不支持分享!");
                            return;
                        }

                        mConvNo = classroom.convNo;
                        new IMProvider(mContext).createConvInfoByClassRoom(mConvNo, fromId, classroom)
                                .success(new NormalCallback<ConvEntity>() {
                                    @Override
                                    public void success(ConvEntity convEntity) {
                                        loadDialog.dismiss();
                                        sendMsg(fromId, mConvNo, Destination.USER);
                                    }
                                });
                    }
                });
    }
}
