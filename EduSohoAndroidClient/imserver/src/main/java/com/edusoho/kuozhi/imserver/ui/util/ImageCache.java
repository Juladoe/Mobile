package com.edusoho.kuozhi.imserver.ui.util;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by suju on 16/9/5.
 */
public class ImageCache {

    private ImageCache() {
        // use 1/8 of available heap size
        cache = new LruCache<String, MaskBitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
            @Override
            protected int sizeOf(String key, MaskBitmap value) {
                Bitmap bitmap = value.target;
                if (bitmap == null) {
                    return 0;
                }
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    private static ImageCache imageCache = null;

    public static synchronized ImageCache getInstance() {
        if (imageCache == null) {
            imageCache = new ImageCache();
        }
        return imageCache;

    }
    private LruCache<String, MaskBitmap> cache = null;

    /**
     * put bitmap to image cache
     * @param key
     * @param value
     * @return  the puts bitmap
     */
    public MaskBitmap put(String key, MaskBitmap value){
        return cache.put(key, value);
    }

    /**
     * return the bitmap
     * @param key
     * @return
     */
    public MaskBitmap get(String key){
        return cache.get(key);
    }
}
