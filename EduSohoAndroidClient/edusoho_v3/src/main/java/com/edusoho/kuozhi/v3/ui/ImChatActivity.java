package com.edusoho.kuozhi.v3.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.ui.MessageListPresenterImpl;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.provider.UserProvider;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.ui.chat.AbstractIMChatActivity;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.Promise;
import java.util.LinkedHashMap;
import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by suju on 16/8/26.
 */
public class ImChatActivity extends AbstractIMChatActivity {

    protected void createTargetRole(String type, int rid, final MessageListPresenterImpl.RoleUpdateCallback callback) {
        new UserProvider(mContext).getUserInfo(rid)
                .success(new NormalCallback<User>() {
                    @Override
                    public void success(User user) {
                        Role role = new Role();
                        if (user == null) {
                            callback.onCreateRole(role);
                            return;
                        }
                        role.setRid(user.id);
                        role.setAvatar(user.mediumAvatar);
                        role.setType(Destination.USER);
                        role.setNickname(user.nickname);
                        callback.onCreateRole(role);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.user_profile) {
            Bundle bundle = new Bundle();
            School school = getAppSettingProvider().getCurrentSchool();
            bundle.putString(Const.WEB_URL, String.format(Const.MOBILE_APP_URL, school.url + "/", String.format(Const.USER_PROFILE, mTargetId)));
            CoreEngine.create(mContext).runNormalPluginWithBundle("WebViewActivity", mContext, bundle);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected Promise createChatConvNo() {
        final Promise promise = new Promise();
        User currentUser = getAppSettingProvider().getCurrentUser();
        if (currentUser == null || currentUser.id == 0) {
            ToastUtils.show(getBaseContext(), "用户未登录");
            promise.resolve(null);
            return promise;
        }

        new UserProvider(mContext).createConvNo(new int[]{currentUser.id, mTargetId})
                .success(new NormalCallback<LinkedHashMap>() {
                    @Override
                    public void success(LinkedHashMap linkedHashMap) {
                        String no = null;
                        if (linkedHashMap != null || linkedHashMap.containsKey("no")) {
                            no = linkedHashMap.get("no").toString();
                        }
                        promise.resolve(no);
                    }
                });

        return promise;
    }
}
