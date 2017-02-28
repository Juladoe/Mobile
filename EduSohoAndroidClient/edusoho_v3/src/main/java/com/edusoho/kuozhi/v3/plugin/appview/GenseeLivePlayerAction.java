package com.edusoho.kuozhi.v3.plugin.appview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.ui.fragment.video.LessonVideoPlayerFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import java.io.File;

/**
 * Created by 菊 on 2016/4/11.
 */
public class GenseeLivePlayerAction {

    private final static int VERSION = 2;

    private Activity mActivity;

    public GenseeLivePlayerAction(Activity activity)
    {
        this.mActivity = activity;
    }

    public void invoke(Bundle bundle) {
        Intent intent = new Intent();
        intent.setClassName("com.gensee.player", "com.gensee.player.GenseePlayActivity");
        if (checkLiveAppIsExist(mActivity.getBaseContext(), intent)) {
            installLiveApp();
            return;
        }
        intent.putExtras(bundle);
        mActivity.startActivity(intent);
    }

    private void installLiveApp() {
        File installDir = AppUtil.getAppInstallStorage();
        CoreEngine.create(mActivity).installApkFromAssetByPlugin(installDir.getAbsolutePath());
        installApk(new File(installDir, "genseeLivePlayer.apk").getAbsolutePath());
    }

    private boolean checkLiveAppIsExist(Context context, Intent intent) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo("com.gensee.player", PackageManager.GET_ACTIVITIES);
            if (packageInfo != null && packageInfo.versionCode < VERSION) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, 0);
        return resolveInfo == null;
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
