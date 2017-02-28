package com.edusoho.kuozhi.v3.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.edusoho.kuozhi.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import photoview.PhotoView;
import photoview.PhotoViewAttacher;

/**
 * Created by suju on 16/9/14.
 */
public class ViewPagerAdapter extends PagerAdapter {

    private String[] mImages;
    private DisplayImageOptions mOptions;
    private ViewPagerAdapterListener mListener;

    public ViewPagerAdapter(String[] images, ViewPagerAdapterListener listener) {
        mImages = images;
        mOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .showImageForEmptyUri(R.drawable.defaultpic)
                .showImageOnLoading(R.drawable.icon_loading)
                .showImageOnFail(R.drawable.defaultpic)
                .build();
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mImages.length;
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());
        if (mImages[position].contains("http://")) {
            ImageLoader.getInstance().displayImage(mImages[position], photoView, mOptions);
        } else {
            ImageLoader.getInstance().displayImage("file://" + mImages[position], photoView, mOptions);
        }
        container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                mListener.onFinish();
            }
        });
        photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                mListener.onFinish();
            }
        });
        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public interface ViewPagerAdapterListener {

        void onFinish();
    }
}
