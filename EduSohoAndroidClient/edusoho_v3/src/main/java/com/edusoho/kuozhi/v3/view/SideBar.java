package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Melomelon on 2015/8/4.
 */
public class SideBar extends View {

    private OnTouchingLetterChangedListener mOnTouchingLetterChangedListener;
    private int choose = -1;
    private Paint mPaint = new Paint();
    private TextView mTextDialog;


    private static String[] index = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};

    public SideBar(Context context) {
        super(context);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        int width = getWidth();
        int letterHeight = height / index.length;

        for (int i = 0; i < index.length; i++) {
            mPaint.setColor(Color.BLACK);
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(28);

            if (i == choose) {
                mPaint.setColor(Color.parseColor("#3399ff"));
                mPaint.setFakeBoldText(true);
            }
            float posX = width / 2 - mPaint.measureText(index[i]) / 2;
            float posY = letterHeight * i + letterHeight;
            canvas.drawText(index[i], posX, posY, mPaint);
            mPaint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;

        final OnTouchingLetterChangedListener listener = mOnTouchingLetterChangedListener;
        final int letter = (int) (y / getHeight() * index.length);

        switch (action) {
            case MotionEvent.ACTION_UP:
                setBackgroundDrawable(new ColorDrawable(0x00000000));
                choose = -1;
                invalidate();
                if (mTextDialog != null) {
                    mTextDialog.setVisibility(View.INVISIBLE);
                }
                break;

            default:
                if (oldChoose != letter) {
                    if (letter >= 0 && letter < index.length) {
                        if (listener != null) {
                            listener.onTouchingLetterChangedListener(index[letter]);
                        }
                        if (mTextDialog != null) {
                            mTextDialog.setText(index[letter]);
                            mTextDialog.setVisibility(View.VISIBLE);
                        }
                        choose = letter;
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    public void setTextView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }


    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.mOnTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    public interface OnTouchingLetterChangedListener {
        void onTouchingLetterChangedListener(String string);
    }
}
