package com.edusoho.kuozhi;

import android.content.Intent;
import android.os.Bundle;

import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.StartActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.view.dialog.PopupDialog;


public class KuozhiActivity extends StartActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtil.initConfig(new String[]{
                getResources().getString(R.string.app_code)
        });
    }

    protected void initApp() {
        if (!app.getNetIsConnect()) {
            CommonUtil.longToast(this, "没有网络服务！请检查网络设置。");
            startApp();
            return;
        }

        checkSchoolApiVersion();
    }

    private void setDefaultSchool() {
        School school = new School();
        school.host = app.host;
        school.url = app.host + "/mapi_v2";
        school.name = getResources().getString(R.string.app_name);
        school.logo = "";
        app.setCurrentSchool(school);
    }

    protected void showSchoolErrorDlg() {
        PopupDialog popupDialog = PopupDialog.createMuilt(
                mContext,
                "提示信息",
                "网校客户端已关闭或网校服务器出现异常。\n请联系管理员！或选择新网校",
                new PopupDialog.PopupClickListener() {
                    @Override
                    public void onClick(int button) {
                        if (button == PopupDialog.OK) {
                            finish();
                        }
                    }
                }
        );
        popupDialog.setOkText("退出");
        popupDialog.show();
    }

    protected void startApp() {
        setDefaultSchool();
        app.mEngine.runNormalPlugin("DefaultPageActivity", this, new PluginRunCallback() {
            @Override
            public void setIntentDate(Intent startIntent) {
                if (mCurrentIntent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
                    startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                startIntent.putExtras(mCurrentIntent);
            }
        });
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public void invoke(WidgetMessage message) {
        if (message.type.type == INIT_APP) {
            initApp();
        }
    }

    @Override
    public MessageType[] getMsgTypes() {
        MessageType[] messageTypes = {new MessageType(MessageType.NONE, INIT_APP)};
        return messageTypes;
    }
}