package com.edusoho.kuozhi.v3.plugin.appview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.ui.FragmentPageActivity;
import com.edusoho.kuozhi.v3.ui.fragment.video.LessonVideoPlayerFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.PopupDialog;

/**
 * Created by 菊 on 2016/4/11.
 */
public class LonginusLivePlayerAction {

    private Activity mActivity;

    public LonginusLivePlayerAction(Activity activity)
    {
        this.mActivity = activity;
    }

    public void invoke(Bundle bundle) {
        Intent intent = new Intent();
        intent.setClassName(mActivity.getPackageName(), "com.edusoho.longinus.ui.LessonLivePlayerActivity");
        if (checkLiveAppIsExist(intent)) {
            CommonUtil.shortToast(mActivity.getApplicationContext(), "客户端暂时不支持播放此直播类型");
            return;
        }

        if (bundle.getBoolean("replayState", false)) {
            startReplyActivity(mActivity.getBaseContext(), bundle);
            return;
        }
        intent.putExtras(bundle);
        mActivity.startActivity(intent);
    }

    private void startReplyActivity(Context context, Bundle bundle) {
        bundle.putString(LessonVideoPlayerFragment.PLAY_URI, bundle.getString("playUrl"));
        bundle.putString(Const.ACTIONBAR_TITLE, bundle.getString("title"));
        CoreEngine.create(context).runNormalPluginWithBundle("VideoPlayerActivity", context, bundle);
    }

    private boolean checkLiveAppIsExist(Intent intent) {
        return mActivity.getBaseContext().getPackageManager().resolveActivity(intent, 0) == null;
    }
}
