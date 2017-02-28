package com.edusoho.kuozhi.v3.listener;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by JesseHuang on 15/11/10.
 */
public class AvatarLoadingListener implements ImageLoadingListener {
    public String mType;

    public AvatarLoadingListener(String type) {
        mType = type;
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        Log.d("FriendAdapter", "onLoadingStarted:" + imageUri);
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        ImageView imageView = (ImageView) view;
        setAvatarImage(imageView);
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        ImageView imageView = (ImageView) view;
        imageView.setBackgroundColor(Color.TRANSPARENT);
        if (TextUtils.isEmpty(imageUri)) {
            setAvatarImage(imageView);
        }
    }

    private void setAvatarImage(ImageView imageView) {
        if (mType == null) {
            imageView.setPadding(0, 0, 0, 0);
            imageView.setBackgroundColor(Color.TRANSPARENT);
            imageView.setImageResource(R.drawable.default_avatar);
            return;
        }
        imageView.setBackgroundColor(Color.TRANSPARENT);
        switch (mType) {
            case PushUtil.ChatUserType.FRIEND:
            case PushUtil.ChatUserType.TEACHER:
                imageView.setImageResource(R.drawable.default_avatar);
                break;
            case PushUtil.ChatUserType.CLASSROOM:
                imageView.setImageResource(R.drawable.default_classroom);
                break;
            case PushUtil.ChatUserType.COURSE:
                imageView.setImageResource(R.drawable.default_course);
                break;
            case PushUtil.ChatUserType.NEWS:
                imageView.setBackgroundColor(imageView.getContext().getResources().getColor(R.color.normal_reply_tag));
                imageView.setPadding(16, 16, 16, 16);
                imageView.setImageResource(R.drawable.article_app_icon);
                return;

        }
        imageView.setPadding(0, 0, 0, 0);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        Log.d("FriendAdapter", "onLoadingCancelled:" + imageUri);
    }
}
