package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Melomelon on 2015/8/12.
 */
public class EduSohoGroupAvatar extends View {

    private int height;
    private int width;
    private int maxRows;
    private int maxAvatarsCount;
    private int minAvatarsCount;

    private int currentRow;
    private int currentRowCount;
    private int avatarCountBefore;

    private int tinyAvatarWidth;


    private ArrayList<TinyAvatar> tinyAvatarList = new ArrayList<>();


    public EduSohoGroupAvatar(Context context) {
        this(context, null);
    }

    public EduSohoGroupAvatar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EduSohoGroupAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    public void addAvatar(int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
        addAvatar(bitmap);
    }

    public void addAvatar(Bitmap bitmap) {
        if (bitmap != null) {
            TinyAvatar tinyAvatar = new TinyAvatar(bitmap);
            addAvatar(tinyAvatar);
        }
    }

    public void addAvatar(TinyAvatar tinyAvatar) {
        if (tinyAvatar != null && tinyAvatar.bitmap != null) {
            tinyAvatarList.add(tinyAvatar);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        super.onDraw(canvas);
        height = getHeight();
        width = getWidth();
        int totalAvatars = tinyAvatarList.size();

        maxAvatarsCount = totalAvatars > 4 ? 3 : 2;
        tinyAvatarWidth = width / maxAvatarsCount;
        minAvatarsCount = totalAvatars % maxAvatarsCount == 0 ? maxAvatarsCount : (totalAvatars % maxAvatarsCount);
        maxRows = totalAvatars > 2 ? (totalAvatars > 6 ? 3 : 2) : 1;

        canvas.drawColor(Color.LTGRAY);

        for (int i = 1; i <= totalAvatars; i++) {
            TinyAvatar tinyAvatar = tinyAvatarList.get(i - 1);

            currentRow = i <= minAvatarsCount ? 1 : 1 + (int) Math.ceil((i - minAvatarsCount) * 1.0f / maxAvatarsCount);
            currentRowCount = currentRow > 1 ? maxAvatarsCount : minAvatarsCount;
            avatarCountBefore = currentRow > 1 ? ((i - minAvatarsCount) - (currentRow - 2) * maxAvatarsCount - 1) : (i - 1);

            tinyAvatar.left = width / maxAvatarsCount * ((maxAvatarsCount - currentRowCount) * 1.0f / 2 + avatarCountBefore);
            tinyAvatar.top = height / 2 - width / maxAvatarsCount * (maxRows * 1.0f / 2 - (currentRow - 1));

            Bitmap bitmap = tinyAvatar.bitmap;
            tinyAvatar.bitmap = Bitmap.createScaledBitmap(bitmap, (int) (tinyAvatarWidth * 0.84f), (int) (tinyAvatarWidth * 0.84f), true);
            if (avatarCountBefore == 0) {
                if (currentRow == 1) {
                    if (currentRowCount == 1){
                        canvas.drawBitmap(tinyAvatar.bitmap, tinyAvatar.left + tinyAvatarWidth * 0.08f, tinyAvatar.top + tinyAvatarWidth * 0.12f, new Paint(paint));
                    }else {
                        canvas.drawBitmap(tinyAvatar.bitmap, tinyAvatar.left + tinyAvatarWidth * 0.12f, tinyAvatar.top + tinyAvatarWidth * 0.12f, new Paint(paint));
                    }
                } else {
                    if (currentRow == 3){
                        canvas.drawBitmap(tinyAvatar.bitmap, tinyAvatar.left + tinyAvatarWidth * 0.12f, tinyAvatar.top + tinyAvatarWidth * 0.04f, new Paint(paint));
                    }else {
                        canvas.drawBitmap(tinyAvatar.bitmap, tinyAvatar.left + tinyAvatarWidth * 0.12f, tinyAvatar.top + tinyAvatarWidth * 0.08f, new Paint(paint));
                    }
                }
            } else {
                if (currentRow == 1) {
                    if (avatarCountBefore == 2){
                        canvas.drawBitmap(tinyAvatar.bitmap, tinyAvatar.left + tinyAvatarWidth * 0.04f, tinyAvatar.top + tinyAvatarWidth * 0.12f, new Paint(paint));
                    }else {
                        canvas.drawBitmap(tinyAvatar.bitmap, tinyAvatar.left + tinyAvatarWidth * 0.08f, tinyAvatar.top + tinyAvatarWidth * 0.12f, new Paint(paint));
                    }
                } else if (currentRow == 3){
                    if (avatarCountBefore == 2){
                        canvas.drawBitmap(tinyAvatar.bitmap, tinyAvatar.left + tinyAvatarWidth * 0.04f, tinyAvatar.top + tinyAvatarWidth * 0.04f, new Paint(paint));
                    }else {
                        canvas.drawBitmap(tinyAvatar.bitmap, tinyAvatar.left + tinyAvatarWidth * 0.08f, tinyAvatar.top + tinyAvatarWidth * 0.04f, new Paint(paint));
                    }
                } else {
                    if (avatarCountBefore == 2){
                        canvas.drawBitmap(tinyAvatar.bitmap, tinyAvatar.left + tinyAvatarWidth * 0.04f, tinyAvatar.top + tinyAvatarWidth * 0.08f, new Paint(paint));
                    }else {
                        canvas.drawBitmap(tinyAvatar.bitmap, tinyAvatar.left + tinyAvatarWidth * 0.08f, tinyAvatar.top + tinyAvatarWidth * 0.08f, new Paint(paint));
                    }
                }
            }


        }
    }

    public class TinyAvatar {
        private Bitmap bitmap;
        private float left;
        private float top;

        public TinyAvatar(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }
}
