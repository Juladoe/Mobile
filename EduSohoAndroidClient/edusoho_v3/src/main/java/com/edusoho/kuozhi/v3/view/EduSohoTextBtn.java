package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;

import java.util.HashMap;

/**
 * Created by howzhi on 14-8-7.
 */
public class EduSohoTextBtn extends LinearLayout {
    private Context mContext;
    private String text;
    private String icon;
    private int size;
    private int textSize;
    private float iconSizeScale;
    private ColorStateList color;
    private TextView mText;
    private EduSohoIconView mIcon;
    private FrameLayout mIconLayout;
    private TextView mUpdateIcon;

    private boolean mIsUpdate;
    private HashMap<String, Object> notifyTypes;

    public EduSohoTextBtn(Context context) {
        super(context);
        mContext = context;
    }

    public EduSohoTextBtn(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(attrs);
    }

    private void initView(android.util.AttributeSet attrs) {
        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.EduSohoTextBtn);
        text = ta.getString(R.styleable.EduSohoTextBtn_text);
        icon = ta.getString(R.styleable.EduSohoTextBtn_image);
        size = ta.getDimensionPixelSize(R.styleable.EduSohoTextBtn_size, 0);
        textSize = ta.getDimensionPixelSize(R.styleable.EduSohoTextBtn_text_size, 0);
        iconSizeScale = ta.getFloat(R.styleable.EduSohoTextBtn_iconSizeScale, 1.0f);
        color = ta.getColorStateList(R.styleable.EduSohoTextBtn_fontColor);

        LayoutParams layoutParams = new LayoutParams(0, 0);
        layoutParams.gravity = Gravity.CENTER;
        setLayoutParams(layoutParams);
        setGravity(Gravity.CENTER);

        LayoutParams childlp = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        mIconLayout = new FrameLayout(mContext);
        mIcon = new EduSohoIconView(mContext);
        mIcon.setText(icon);
        mIcon.setTextColor(color);
        mIcon.setTextSize(TypedValue.COMPLEX_UNIT_PX, size * iconSizeScale);
        mIconLayout.setLayoutParams(childlp);
        mIconLayout.addView(mIcon);
        addView(mIconLayout);

        mText = new TextView(mContext);
        mText.setSingleLine();
        mText.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        mText.setText(text);
        mText.setGravity(Gravity.CENTER);
        mText.setTextColor(color);
        mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mText.setLayoutParams(childlp);
        addView(mText);

        if (TextUtils.isEmpty(text)) {
            mText.setVisibility(GONE);
        }
        notifyTypes = new HashMap<>();
        ta.recycle();
    }

    public void setText(String text) {
        mText.setText(text);
    }

    public void setText(int resId) {
        mText.setText(resId);
    }

    public void setText(String text, int iconId) {
        mText.setText(text);
        mIcon.setText(iconId);
    }

    public void setTextColor(int color) {
        mText.setTextColor(color);
        mIcon.setTextColor(color);
    }

    public void setIcon(String icon) {
        mIcon.setText(icon);
    }

    public void setIcon(int iconId) {
        mIcon.setText(iconId);
    }

    public void setUpdateIcon(int badge) {
        if (badge <= 0) {
            clearUpdateIcon();
            return;
        }
        mIsUpdate = true;
        if (mUpdateIcon == null) {
            mUpdateIcon = new TextView(mContext);
            mUpdateIcon.setGravity(Gravity.CENTER);
            mUpdateIcon.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8);
            mUpdateIcon.setTextColor(getResources().getColor(android.R.color.white));
            mUpdateIcon.setPadding(0, 0, 0, 0);
            mUpdateIcon.setBackgroundResource(R.drawable.update_bg);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER | Gravity.TOP;
            layoutParams.leftMargin = 30;
            mUpdateIcon.setLayoutParams(layoutParams);
            mIconLayout.addView(mUpdateIcon);
        }

        mUpdateIcon.setText(badge > 99 ? ".." : String.valueOf(Math.abs(badge)));
    }

    public void setBageIcon(boolean isVisiable) {
        if (!isVisiable) {
            clearUpdateIcon();
            return;
        }
        mIsUpdate = true;
        if (mUpdateIcon == null) {
            mUpdateIcon = new TextView(mContext);
            mUpdateIcon.setGravity(Gravity.CENTER);
            mUpdateIcon.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 4);
            mUpdateIcon.setTextColor(getResources().getColor(android.R.color.white));
            mUpdateIcon.setPadding(0, 0, 0, 0);
            mUpdateIcon.setBackgroundResource(R.drawable.small_update_bg);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER | Gravity.TOP;
            layoutParams.leftMargin = 24;
            mUpdateIcon.setLayoutParams(layoutParams);
            mIconLayout.addView(mUpdateIcon);
        }

        mUpdateIcon.setText("");
    }


    public void clearUpdateIcon() {
        mIsUpdate = false;
        if (mUpdateIcon == null) {
            return;
        }
        mUpdateIcon.setVisibility(GONE);
        mIconLayout.removeView(mUpdateIcon);
        mUpdateIcon = null;
    }

    public boolean getUpdateMode() {
        return mIsUpdate;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mIcon.setEnabled(enabled);
        mText.setEnabled(enabled);
    }

    public void addNotifyType(String type) {
        notifyTypes.put(type, null);
    }

    public void addNotifyTypes(String[] types) {
        for (String type : types) {
            addNotifyType(type);
        }
    }

    public boolean hasNotify(String type) {
        return notifyTypes.containsKey(type);
    }
}
