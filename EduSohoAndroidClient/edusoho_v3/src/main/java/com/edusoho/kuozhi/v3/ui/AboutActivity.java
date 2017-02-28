package com.edusoho.kuozhi.v3.ui;

import android.os.Bundle;
import android.view.View;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.listener.StatusCallback;
import com.edusoho.kuozhi.v3.model.sys.AppUpdateInfo;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.view.EduUpdateView;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.edusoho.kuozhi.v3.view.dialog.PopupDialog;

import java.util.Set;

/**
 * Created by JesseHuang on 15/5/18.
 */
public class AboutActivity extends ActionBarBaseActivity {
    private EduUpdateView tvCheckUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        setBackMode(BACK, "关于");
        initView();
    }

    private void initView() {
        findViewById(R.id.rl_about_school).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.mEngine.runNormalPlugin("AboutSchool", mActivity, null);
            }
        });

        findViewById(R.id.rl_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.mEngine.runNormalPlugin("SuggestionActivity", mActivity, null);
            }
        });

        tvCheckUpdate = (EduUpdateView) findViewById(R.id.tv_check_update);
        View viewUpdate = findViewById(R.id.linear_check_update);

        viewUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LoadDialog loadDialog = LoadDialog.create(mActivity);
                loadDialog.setMessage("检查版本中...");
                loadDialog.show();
                AppUtil.checkUpateApp(mActivity, new StatusCallback<AppUpdateInfo>() {
                    @Override
                    public void success(final AppUpdateInfo result) {
                        loadDialog.dismiss();
                        PopupDialog popupDialog = PopupDialog.createMuilt(
                                mActivity,
                                "版本更新",
                                "更新内容\n" + result.updateInfo, new PopupDialog.PopupClickListener() {
                                    @Override
                                    public void onClick(int button) {
                                        if (button == PopupDialog.OK) {
                                            app.startUpdateWebView(result.updateUrl);
                                        } else {
                                            tvCheckUpdate.clearUpdateIcon();
                                            app.removeNotify("app_update");
                                        }
                                    }
                                });

                        popupDialog.setOkText("更新");
                        popupDialog.show();
                    }

                    @Override
                    public void error(AppUpdateInfo obj) {
                        loadDialog.dismiss();
                        CommonUtil.shortToast(mContext, "已经是最新版本!");
                    }
                });
            }
        });

        tvCheckUpdate.setText(AppUtil.getColorTextAfter(
                "版本更新 ",
                app.getApkVersion(),
                R.color.system_normal_text
        ));
        tvCheckUpdate.append(" - " + app.getApkVersionCode());

        registNotify();
        checkNotify();

    }

    private void registNotify() {
        tvCheckUpdate.addNotifyType("app_update");
    }

    private void checkNotify() {
        Set<String> notifys = app.getNotifys();
        for (String type : notifys) {
            if (tvCheckUpdate == null) {
                continue;
            }
            if (tvCheckUpdate.hasNotify(type)) {
                tvCheckUpdate.setUpdateIcon(R.drawable.setting_new);
                continue;
            }

            boolean updateMode = tvCheckUpdate.getUpdateMode();
            if (updateMode) {
                tvCheckUpdate.clearUpdateIcon();
            }
        }
    }
}
