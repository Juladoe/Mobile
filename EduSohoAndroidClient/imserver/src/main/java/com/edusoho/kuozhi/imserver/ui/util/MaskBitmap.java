package com.edusoho.kuozhi.imserver.ui.util;

import android.graphics.Bitmap;

import com.edusoho.kuozhi.imserver.ui.entity.Direct;

/**
 * Created by suju on 16/9/5.
 */
public class MaskBitmap {

    public Direct direct = Direct.SEND;
    public boolean isMask;
    public Bitmap target;

    public MaskBitmap(Bitmap target) {
        this.target = target;
    }
}
