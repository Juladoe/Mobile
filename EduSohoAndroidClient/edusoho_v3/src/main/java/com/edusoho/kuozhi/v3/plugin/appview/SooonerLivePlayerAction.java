package com.edusoho.kuozhi.v3.plugin.appview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.util.AppUtil;

import java.io.File;

/**
 * Created by 菊 on 2016/4/11.
 */
public class SooonerLivePlayerAction {

    private Activity mActivity;

    public SooonerLivePlayerAction(Activity activity)
    {
        this.mActivity = activity;
    }

    public void invoke(Bundle bundle) {
        String liveClassroomId = bundle.getString("liveClassroomId");
        String exStr = bundle.getString("exStr");
        boolean replayState = bundle.getBoolean("replayState");

        Intent intent = new Intent();
        intent.setClassName("com.soooner.EplayerPluginLibary", "com.soooner.EplayerPluginLibary.LivePlayerActivity");
        if (checkLiveAppIsExist(intent)) {
            installLiveApp();
            return;
        }
        intent.putExtra("liveClassroomId", liveClassroomId);
        intent.putExtra("exStr", exStr);
        intent.putExtra("replayState", replayState);
        mActivity.startActivity(intent);
    }

    private void installLiveApp() {
        File installDir = AppUtil.getAppInstallStorage();
        CoreEngine.create(mActivity).installApkFromAssetByPlugin(installDir.getAbsolutePath());
        installApk(new File(installDir, "liveEplayer.apk").getAbsolutePath());
    }

    private boolean checkLiveAppIsExist(Intent intent) {
        return mActivity.getBaseContext().getPackageManager().resolveActivity(intent, 0) == null;
    }

    public void installApk(String file) {
        if (file == null || "".equals(file)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);

        intent.setDataAndType(Uri.parse("file://" + file),
                "application/vnd.android.package-archive");
        mActivity.getBaseContext().startActivity(intent);
    }
}
