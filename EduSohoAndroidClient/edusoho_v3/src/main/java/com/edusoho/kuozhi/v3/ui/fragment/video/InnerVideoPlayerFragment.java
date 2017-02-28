package com.edusoho.kuozhi.v3.ui.fragment.video;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.edusoho.kuozhi.R;
import com.edusoho.videoplayer.ui.VideoPlayerFragment;


/**
 * Created by suju on 16/12/16.
 */

public class InnerVideoPlayerFragment extends VideoPlayerFragment {

    private int mLessonId;
    private int mCourseId;
    private long mSaveSeekTime;
    private SharedPreferences mSeekPositionSetting;
    private static final String SEEK_POSITION = "seek_position";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasOptionsMenu();

        mSeekPositionSetting = getContext().getSharedPreferences(SEEK_POSITION, Context.MODE_PRIVATE);
        mSaveSeekTime = mSeekPositionSetting.getLong(String.format("%d-%d", mCourseId, mLessonId), 0);
        setSeekPosition(mSaveSeekTime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem moreItem = menu.findItem(R.id.menu_more);
        if (moreItem != null) {
            moreItem.setVisible(false);
        }
    }

    private void initFragmentSize(int height) {
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        setVideoSize(width, height);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragmentSize(getContext().getResources().getDimensionPixelOffset(com.edusoho.videoplayer.R.dimen.video_height));
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams)lp).gravity = Gravity.CENTER;
            view.setLayoutParams(lp);
        }

        ((View)view.getParent()).setBackgroundColor(Color.BLACK);
    }

    @Override
    protected void changeScreenLayout(int orientation) {
        if (orientation == getResources().getConfiguration().orientation) {
            return;
        }
        int screenOrientation = orientation == Configuration.ORIENTATION_LANDSCAPE ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        getActivity().setRequestedOrientation(screenOrientation);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        View playView = getView();
        ViewParent viewParent = playView.getParent();
        if (viewParent == null) {
            return;
        }
        ViewGroup parent = (ViewGroup) viewParent;

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        ViewGroup.LayoutParams lp = parent.getLayoutParams();
        int height = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ?
                wm.getDefaultDisplay().getHeight() : getContext().getResources().getDimensionPixelOffset(com.edusoho.videoplayer.R.dimen.video_height);
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;

        initFragmentSize(height);
        parent.setLayoutParams(lp);
    }

    @Override
    protected void savePosition(long seekTime) {
        super.savePosition(seekTime);

        SharedPreferences.Editor editor = mSeekPositionSetting.edit();
        editor.putLong(String.format("%d-%d", mCourseId, mLessonId), seekTime);
        editor.commit();
    }
}
