package com.edusoho.kuozhi.v3.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.util.AppUtil;

import java.util.ArrayList;

public class EduSohoButton extends FrameLayout {

    private Context mContext;

    // # Background Attributs
    private int mDefaultBackgroundColor = Color.BLACK;
    private int mFocusBackgroundColor = 0;

    // # Text Attributs
    private int mDefaultTextColor = Color.WHITE;

    private int mDefaultTextSize = 15;
    private String mText = null;

    // # Icon Attributs
    private Drawable mIconResource = null;
    private int mFontIconSize = 15;
    private String mFontIcon = null;
    private int mIconPosition = 1;

    private int mBorderColor = Color.TRANSPARENT;
    private int mBorderWidth = 0;

    private int mRadius = 0;

    private Typeface mTextTypeFace = Typeface.DEFAULT;
    private Typeface mIconTypeFace = null;


    /**
     * Tags to identify the position of the icon
     */
    public static final int POSITION_LEFT = 1;
    public static final int POSITION_RIGHT = 2;
    public static final int POSITION_TOP = 3;
    public static final int POSITION_BOTTOM = 4;

    //private static final ArrayList<String> mDefaultTextFonts = new ArrayList<String>(Arrays.asList("helveticaneue.ttf","robotoregular.ttf","robotothin.ttf"));
    //private static final ArrayList<String> mDefaultIconFonts = new ArrayList<String>(Arrays.asList("fontawesome-webfont.ttf"));

    private String mDefaultIconFont = "iconfont.ttf";
    //private String mDefaultTextFont = "robotoregular.ttf";

    private ImageView mIconView;
    private TextView mFontIconView;
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private LinearLayout mContainer;

    public static final int PROGRESS = 0010;
    public static final int NORMAL = 0012;

    public EduSohoButton(Context context) {
        super(context);
        this.mContext = context;
        //mTextTypeFace = Typeface.createFromAsset(mContext.getAssets(), mDefaultTextFont);
        mIconTypeFace = Typeface.createFromAsset(mContext.getAssets(), mDefaultIconFont);
        init();
    }

    private void init() {

        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        initContainer();

        mTextView = setupTextView();
        mIconView = setupIconView();
        mProgressBar = setupProgressBar();
        mFontIconView = setupFontIconView();

        int iconIndex, textIndex;
        View view1, view2;

        if (mIconView == null && mFontIconView == null && mTextView == null) {
            Button tempTextView = new Button(mContext);
            tempTextView.setText("Fancy Button");
            mContainer.addView(tempTextView);

        } else {
            mContainer.removeAllViews();
            setupBackground();

            ArrayList<View> views = new ArrayList<View>();

            if (mIconPosition == POSITION_LEFT || mIconPosition == POSITION_TOP) {

                if (mIconView != null) {
                    views.add(mIconView);
                }

                if (mFontIconView != null) {
                    views.add(mFontIconView);
                }
                if (mTextView != null) {
                    views.add(mTextView);
                }

            } else {
                if (mTextView != null) {
                    views.add(mTextView);
                }

                if (mIconView != null) {
                    views.add(mIconView);
                }

                if (mFontIconView != null) {
                    views.add(mFontIconView);
                }

            }

            for (View view : views) {
                mContainer.addView(view);
            }

            addView(mProgressBar);
        }
    }

    public EduSohoButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        TypedArray attrsArray = context.obtainStyledAttributes(attrs, R.styleable.FancyButtonsAttrs, 0, 0);
        initAttributs(attrsArray);
        attrsArray.recycle();

        init();

    }

    private TextView setupTextView() {
        if (mText != null) {
            TextView textView = new TextView(mContext);
            textView.setText(mText);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(mDefaultTextColor);
            mDefaultTextSize = AppUtil.px2sp(mContext, mDefaultTextSize);
            textView.setTextSize(mDefaultTextSize);

            textView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
            if (!isInEditMode() && mTextTypeFace != null) {
                textView.setTypeface(mTextTypeFace);
            }
            return textView;
        }
        return null;
    }

    private TextView setupFontIconView() {

        if (mFontIcon != null) {
            TextView fontIconView = new TextView(mContext);
            fontIconView.setTextColor(mDefaultTextColor);

            LinearLayout.LayoutParams iconTextViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

            if (mTextView != null) {
                iconTextViewParams.rightMargin = 10;
                iconTextViewParams.leftMargin = 10;
                if (mIconPosition == POSITION_TOP || mIconPosition == POSITION_BOTTOM) {
                    iconTextViewParams.gravity = Gravity.CENTER;
                    fontIconView.setGravity(Gravity.CENTER);
                } else {
                    fontIconView.setGravity(Gravity.CENTER_VERTICAL);
                    iconTextViewParams.gravity = Gravity.CENTER_VERTICAL;
                }
            } else {
                iconTextViewParams.gravity = Gravity.CENTER;
                fontIconView.setGravity(Gravity.CENTER_VERTICAL);
            }


            fontIconView.setLayoutParams(iconTextViewParams);
            if (!isInEditMode()) {
                fontIconView.setTextSize(mFontIconSize);
                fontIconView.setText(mFontIcon);
                fontIconView.setTypeface(mIconTypeFace);
            } else {
                fontIconView.setText("O");
            }
            return fontIconView;
        }
        return null;
    }

    private ProgressBar setupProgressBar() {
        ProgressBar progressBar = new ProgressBar(mContext);
        progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.action_bar_load));

        LayoutParams iconViewParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (mTextView != null) {
            if (mIconPosition == POSITION_TOP || mIconPosition == POSITION_BOTTOM)
                iconViewParams.gravity = Gravity.CENTER;
            else
                iconViewParams.gravity = Gravity.LEFT;

            iconViewParams.rightMargin = 10;
            iconViewParams.leftMargin = 10;
        } else {
            iconViewParams.gravity = Gravity.CENTER_VERTICAL;
        }

        iconViewParams.width = AppUtil.dp2px(mContext, 24);
        iconViewParams.height = AppUtil.dp2px(mContext, 24);
        progressBar.setLayoutParams(iconViewParams);
        progressBar.setVisibility(INVISIBLE);
        return progressBar;
    }

    private ImageView setupIconView() {
        if (mIconResource != null) {
            ImageView iconView = new ImageView(mContext);
            iconView.setImageDrawable(mIconResource);

            LayoutParams iconViewParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if (mTextView != null) {
                if (mIconPosition == POSITION_TOP || mIconPosition == POSITION_BOTTOM)
                    iconViewParams.gravity = Gravity.CENTER;
                else
                    iconViewParams.gravity = Gravity.LEFT;

                iconViewParams.rightMargin = 10;
                iconViewParams.leftMargin = 10;
            } else {
                iconViewParams.gravity = Gravity.CENTER_VERTICAL;
            }
            iconView.setLayoutParams(iconViewParams);

            return iconView;
        }
        return null;
    }

    private void initAttributs(TypedArray attrsArray) {

        mDefaultBackgroundColor = attrsArray.getColor(R.styleable.FancyButtonsAttrs_defaultColor, mDefaultBackgroundColor);
        mFocusBackgroundColor = attrsArray.getColor(R.styleable.FancyButtonsAttrs_focusColor, mFocusBackgroundColor);

        mDefaultTextColor = attrsArray.getColor(R.styleable.FancyButtonsAttrs_textColor, mDefaultTextColor);
        mDefaultTextSize = (int) attrsArray.getDimension(R.styleable.FancyButtonsAttrs_textSize, mDefaultTextSize);

        mBorderColor = attrsArray.getColor(R.styleable.FancyButtonsAttrs_borderColor, mBorderColor);
        mBorderWidth = (int) attrsArray.getDimension(R.styleable.FancyButtonsAttrs_pba_borderWidth, mBorderWidth);

        mRadius = (int) attrsArray.getDimension(R.styleable.FancyButtonsAttrs_radius, mRadius);
        mFontIconSize = (int) attrsArray.getDimension(R.styleable.FancyButtonsAttrs_fontIconSize, mFontIconSize);

        String text = attrsArray.getString(R.styleable.FancyButtonsAttrs_textStr);
        mIconPosition = attrsArray.getInt(R.styleable.FancyButtonsAttrs_iconPosition, mIconPosition);

        String fontIcon = attrsArray.getString(R.styleable.FancyButtonsAttrs_fontIconResource);

        String iconFontFamily = attrsArray.getString(R.styleable.FancyButtonsAttrs_iconFont);
        String textFontFamily = attrsArray.getString(R.styleable.FancyButtonsAttrs_textFont);

        Drawable icon = null;
        try {
            mIconResource = attrsArray.getDrawable(R.styleable.FancyButtonsAttrs_iconResource);

        } catch (Exception e) {
            mIconResource = null;
        }

        if (fontIcon != null)
            mFontIcon = fontIcon;

        if (text != null)
            mText = text;

        if (!isInEditMode()) {
            if (iconFontFamily != null) {
                try {
                    mIconTypeFace = Typeface.createFromAsset(mContext.getAssets(), iconFontFamily);
                } catch (Exception e) {
                    Log.e("Fancy", e.getMessage());
                    mIconTypeFace = Typeface.createFromAsset(mContext.getAssets(), mDefaultIconFont);
                }

            } else {
                mIconTypeFace = Typeface.createFromAsset(mContext.getAssets(), mDefaultIconFont);
            }

            if (textFontFamily != null) {
                try {
                    mTextTypeFace = Typeface.createFromAsset(mContext.getAssets(), textFontFamily);
                } catch (Exception e) {
                    //mTextTypeFace = Typeface.createFromAsset(mContext.getAssets(), mDefaultTextFont);
                }

            } else {
                //mTextTypeFace = Typeface.createFromAsset(mContext.getAssets(), mDefaultTextFont);
            }


        }


    }

    @SuppressLint("NewApi")
    private void setupBackground() {

        // Default Drawable
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(mRadius);
        drawable.setColor(mDefaultBackgroundColor);
        if (mBorderColor != 0) {
            drawable.setStroke(mBorderWidth, mBorderColor);
        }

        // Focus/Pressed Drawable
        GradientDrawable drawable2 = new GradientDrawable();
        drawable2.setCornerRadius(mRadius);
        drawable2.setColor(mFocusBackgroundColor);
        if (mBorderColor != 0) {
            drawable2.setStroke(mBorderWidth, mBorderColor);
        }

        StateListDrawable states = new StateListDrawable();

        if (mFocusBackgroundColor != 0) {
            states.addState(new int[]{android.R.attr.state_pressed}, drawable2);
            states.addState(new int[]{android.R.attr.state_focused}, drawable2);
        }
        states.addState(new int[]{}, drawable);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            this.setBackgroundDrawable(states);
        } else {
            this.setBackground(states);
        }
    }

    private void initContainer() {

        mContainer = new LinearLayout(mContext);
        if (mIconPosition == POSITION_TOP || mIconPosition == POSITION_BOTTOM) {
            mContainer.setOrientation(LinearLayout.VERTICAL);
        } else {
            mContainer.setOrientation(LinearLayout.HORIZONTAL);
        }
        LayoutParams containerParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        containerParams.gravity = Gravity.CENTER;
        mContainer.setLayoutParams(containerParams);
        mContainer.setGravity(Gravity.CENTER_VERTICAL);
        if (mIconResource == null && mFontIcon == null && getPaddingLeft() == 0 && getPaddingRight() == 0 && getPaddingTop() == 0 && getPaddingBottom() == 0) {
            mContainer.setPadding(20, 20, 20, 20);
        }

        addView(mContainer);
    }

    public void setText(String text) {
        this.mText = text;
        if (mTextView == null) {
            init();
        } else {
            mTextView.setText(text);
        }
    }

    public void setTextColor(int color) {
        this.mDefaultTextColor = color;
        if (mTextView == null) {
            init();
        } else {
            mTextView.setTextColor(color);
        }
    }

    public void setBackgroundColor(int color) {
        this.mDefaultBackgroundColor = color;
        if (mIconView != null || mFontIconView != null || mTextView != null) {
            this.setupBackground();
        }
    }

    public void setFocusBackgroundColor(int color) {
        this.mFocusBackgroundColor = color;
        if (mIconView != null || mFontIconView != null || mTextView != null)
            this.setupBackground();

    }

    public void setStatus(int status) {
        switch (status) {
            case PROGRESS:
                mProgressBar.setVisibility(VISIBLE);
                setEnabled(false);
                break;
            case NORMAL:
                mProgressBar.setVisibility(GONE);
                setEnabled(true);
                break;
        }
    }

    public void setTextSize(int textSize) {
        this.mDefaultTextSize = textSize;
        if (mTextView != null)
            mTextView.setTextSize(textSize);
    }

    public void setIconResource(int drawable) {
        this.mIconResource = mContext.getResources().getDrawable(drawable);
        if (mIconView == null || mFontIconView != null) {
            mFontIconView = null;
            init();
        } else
            mIconView.setImageDrawable(mIconResource);
    }

    public void setIconResource(String icon) {
        this.mFontIcon = icon;
        if (mFontIconView == null) {
            mIconView = null;
            init();
        } else
            mFontIconView.setText(icon);
    }

    public void setFontIconSize(int iconSize) {
        this.mFontIconSize = iconSize;
        if (mFontIconView != null)
            mFontIconView.setTextSize(iconSize);
    }

    public void setIconPosition(int position) {
        if (position > 0 && position < 5)
            mIconPosition = position;
        else
            mIconPosition = POSITION_LEFT;

        this.init();
    }

    public void setBorderColor(int color) {
        this.mBorderColor = color;
        if (mIconView != null || mFontIconView != null || mTextView != null) {
            this.setupBackground();
        }
    }

    public void setBorderWidth(int width) {
        this.mBorderWidth = width;
        if (mIconView != null || mFontIconView != null || mTextView != null) {
            this.setupBackground();
        }
    }

    public void setRadius(int radius) {
        this.mRadius = radius;
        if (mIconView != null || mFontIconView != null || mTextView != null) {
            this.setupBackground();
        }
    }

    public void setCustomTextFont(String fontName) {
        try {
            mTextTypeFace = Typeface.createFromAsset(mContext.getAssets(), String.format("fonts/%s", fontName));
        } catch (Exception e) {
            Log.e("FancyButtons", e.getMessage());
            //mTextTypeFace = Typeface.createFromAsset(mContext.getAssets(), String.format("fonts/%s", mDefaultTextFont));
        }

        if (mTextView == null) {
            init();
        } else {
            mTextView.setTypeface(mTextTypeFace);
        }
    }

    public void setCustomIconFont(String fontName) {
        try {
            mIconTypeFace = Typeface.createFromAsset(mContext.getAssets(), String.format("iconfonts/%s", fontName));
        } catch (Exception e) {
            Log.e("FancyButtons", e.getMessage());
            mIconTypeFace = Typeface.createFromAsset(mContext.getAssets(), String.format("iconfonts/%s", mDefaultIconFont));
        }

        if (mFontIconView == null) {
            init();
        } else {
            mFontIconView.setTypeface(mIconTypeFace);
        }
    }

}
