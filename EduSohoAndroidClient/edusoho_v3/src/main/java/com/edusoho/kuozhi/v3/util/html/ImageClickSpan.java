package com.edusoho.kuozhi.v3.util.html;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.view.View;


/**
 * Created by howzhi on 15/10/16.
 */
public class ImageClickSpan extends ClickableSpan {
    private int mIndex;
    private String mImageUrl;
    private Context mContext;

    public ImageClickSpan(Context context, String imageUrl, int index) {
        this.mIndex = index;
        this.mImageUrl = imageUrl;
        this.mContext = context;
    }

    @Override
    public void onClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putInt("index", 0);
        bundle.putStringArray("images", new String[]{mImageUrl});
        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setComponent(new ComponentName(mContext.getPackageName(), "ViewPagerActivity"));
        mContext.startActivity(intent);
    }
}