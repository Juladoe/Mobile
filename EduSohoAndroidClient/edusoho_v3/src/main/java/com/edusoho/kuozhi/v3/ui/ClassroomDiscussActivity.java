package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.ui.MessageListPresenterImpl;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.entity.error.Error;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.Classroom;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.provider.ClassRoomProvider;
import com.edusoho.kuozhi.v3.model.provider.IMProvider;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.Promise;
import com.umeng.analytics.MobclickAgent;

import java.util.LinkedHashMap;

import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by JesseHuang on 15/10/16.
 */
public class ClassroomDiscussActivity extends ImChatActivity implements MessageEngine.MessageCallback {

    public static final int CLEAR = 0x10;

    private Classroom mClassRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MessageEngine.getInstance().registMessageSource(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageEngine.getInstance().unRegistMessageSource(this);
    }

    @Override
    protected String getTargetType() {
        return Destination.CLASSROOM;
    }

    @Override
    protected Promise createChatConvNo() {
        final Promise promise = new Promise();
        User currentUser = getAppSettingProvider().getCurrentUser();

        if (currentUser == null || currentUser.id == 0) {
            ToastUtils.show(getBaseContext(), "用户未登录");
            promise.resolve(null);
            return promise;
        }

        new IMProvider(mContext).joinIMConvNo(mTargetId, "classroom")
        .success(new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap map) {
                if (map == null) {
                    ToastUtils.show(getBaseContext(), "加入班级聊天失败!");
                    finish();
                    return;
                }
                if (map.containsKey("error")) {
                    Error error = getUtilFactory().getJsonParser().fromJson(map.get("error").toString(), Error.class);
                    if (error != null) {
                        ToastUtils.show(getBaseContext(), error.message);
                    }
                    finish();
                    return;
                }
                String convNo = map.get("convNo").toString();
                promise.resolve(convNo);
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                promise.resolve(null);
                ToastUtils.show(getBaseContext(), "加入班级聊天失败!");
                finish();
            }
        });

        return promise;
    }

    @Override
    protected void createTargetRole(String type, int rid, final MessageListPresenterImpl.RoleUpdateCallback callback) {
        if (Destination.CLASSROOM.equals(type)) {
            new ClassRoomProvider(mContext).getClassRoom(mTargetId)
                    .success(new NormalCallback<Classroom>() {
                        @Override
                        public void success(Classroom classroom) {
                            if (classroom == null || TextUtils.isEmpty(classroom.convNo)) {
                                ToastUtils.show(getBaseContext(), "加入班级聊天失败!");
                                finish();
                                return;
                            }
                            mClassRoom = classroom;

                            Role role = new Role();
                            role.setRid(classroom.id);
                            role.setAvatar(classroom.middlePicture);
                            role.setType(Destination.CLASSROOM);
                            role.setNickname(classroom.title);
                            callback.onCreateRole(role);
                        }
                    });
            return;
        }
        super.createTargetRole(type, rid, callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.group_profile) {
            CoreEngine.create(mContext).runNormalPlugin("ClassroomDetailActivity", mContext, new PluginRunCallback() {
                @Override
                public void setIntentDate(Intent startIntent) {
                    MobclickAgent.onEvent(mContext, "chatWindow_topRightCourseDetailsButton");
                    startIntent.putExtra(Const.ACTIONBAR_TITLE, mTargetName);
                    startIntent.putExtra(ChatItemBaseDetail.CONV_NO, mConversationNo);
                    startIntent.putExtra(Const.FROM_ID, mTargetId);
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public MessageType[] getMsgTypes() {
        return new MessageType[]{new MessageType(CLEAR)};
    }

    @Override
    public void invoke(WidgetMessage message) {
        if (message.type.code == CLEAR) {
            mIMessageListPresenter.refresh();
        }
    }

    @Override
    public int getMode() {
        return REGIST_CLASS;
    }
}
