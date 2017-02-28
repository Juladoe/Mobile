package com.edusoho.kuozhi.imserver.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.edusoho.kuozhi.imserver.R;
import com.edusoho.kuozhi.imserver.ui.util.MaskBitmap;

/**
 * Created by suju on 16/9/2.
 */
public class ChatImageView extends ImageView {

    private int mBackgroudRes;

    public ChatImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public ChatImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    protected void initView(AttributeSet attrs) {
        TypedArray typeArray = getContext().obtainStyledAttributes(attrs, R.styleable.ChatImageView);
        int resType = typeArray.getInt(R.styleable.ChatImageView_BackgroundRes, 1);
        mBackgroudRes = resType == 1 ? R.drawable.chat_text_from : R.drawable.chat_send_to;
        typeArray.recycle();
    }

    protected void setXfermodeImage(MaskBitmap maskBitmap) {
        if (maskBitmap.isMask) {
            Log.d("ChatImageView", "is mask bitmap");
            super.setImageBitmap(maskBitmap.target);
            return;
        }

        Bitmap original = maskBitmap.target;
        Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_4444);

        //获取遮罩层图片
        Bitmap mask = getBitmap(original.getWidth(), original.getHeight());
        //将遮罩层的图片放到画布中
        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(original, 0, 0, null);

        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        super.setImageBitmap(result);

        mask.recycle();
        maskBitmap.isMask = true;
        maskBitmap.target = result;
    }

    public void setBackgroudRes(int backgroudRes) {
        this.mBackgroudRes = backgroudRes;
    }

    private Bitmap getBitmap(int w, int h) {
        Bitmap resizedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Bitmap bmp_9 = BitmapFactory.decodeResource(getResources(), mBackgroudRes);
        Canvas canvas = new Canvas(resizedBitmap);
        NinePatch np = new NinePatch(bmp_9, bmp_9.getNinePatchChunk(), null);
        Rect rect = new Rect(0, 0, w, h);
        np.draw(canvas, rect);

        return resizedBitmap;
    }

    public void setMaskBitmap(MaskBitmap maskBitmap) {
        setXfermodeImage(maskBitmap);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        setXfermodeImage(new MaskBitmap(bm));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
