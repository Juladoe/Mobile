package com.edusoho.kuozhi.imserver.ui.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.edusoho.kuozhi.imserver.R;

/**
 * Created by howzhi on 14-5-12.
 */
public class MessageStatusView extends ImageView {

    private AnimationDrawable mAnimationDrawable;
    private Context mContext;

    public MessageStatusView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public MessageStatusView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView() {

    }

    public void setErrorStatus() {
        setImageResource(R.drawable.msg_state_failed_resend);
    }

    public void setProgressStatus() {
        if (mAnimationDrawable == null) {
            Drawable drawable = getResources().getDrawable(R.drawable.load_progress);
            setImageDrawable(drawable);
            if (drawable instanceof AnimationDrawable) {
                mAnimationDrawable = (AnimationDrawable) drawable;
            }
        }

        if (mAnimationDrawable != null && !mAnimationDrawable.isRunning()) {
            mAnimationDrawable.start();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAnimationDrawable != null) {
            mAnimationDrawable.stop();
        }
        mAnimationDrawable = null;
    }
}
