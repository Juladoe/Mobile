package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.edusoho.kuozhi.R;

/**
 * Created by Melomelon on 2015/5/26.
 */
public class EduSohoRoundedEditText extends EditText implements TextWatcher {

    private Paint mPaint;
    private Drawable mClear;

    public EduSohoRoundedEditText(Context context) {
        super(context);
    }

    public EduSohoRoundedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        mClear = getCompoundDrawables()[2];
        mClear = getResources().getDrawable(R.drawable.icon_edittext_clear);
        mClear.setBounds(0, 0, mClear.getIntrinsicWidth(), mClear.getIntrinsicHeight());
        setClearIconVisible(false);
    }

    public void setClearIconVisible(boolean visible) {
        Drawable iconRight = visible ? mClear : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1],
                iconRight, getCompoundDrawables()[3]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
                        && event.getX() < (getWidth() - getPaddingRight());
                if (touchable) {
                    this.setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        setClearIconVisible(getText().length() > 0);
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.LTGRAY);

        setBackgroundColor(Color.TRANSPARENT);

        canvas.drawRoundRect(new RectF(4 + this.getScrollX(), 4 + this.getScrollY(),
                this.getWidth() - 4 + this.getScrollX(), this.getHeight() + this.getScrollY() - 4), 20, 20, mPaint);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
