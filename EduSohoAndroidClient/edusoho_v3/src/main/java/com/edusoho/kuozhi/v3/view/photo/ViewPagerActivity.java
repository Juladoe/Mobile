package com.edusoho.kuozhi.v3.view.photo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import android.widget.TextView;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.ViewPagerAdapter;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import java.util.ArrayList;
import java.util.List;

public class ViewPagerActivity extends ActionBarBaseActivity implements ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private TextView mViewPaperLabel;
    private int mIndex;
    private int mTotalCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_layout);
        initView();
    }

    private void initView() {
        mViewPaperLabel = (TextView) findViewById(R.id.images_label);
        mViewPager = (HackyViewPager) findViewById(R.id.images_pager);
        Intent dataIntent = getIntent();
        mIndex = dataIntent.getIntExtra("index", 0);
        String[] images;
        if (dataIntent.hasExtra("imageList")) {
            ArrayList<String> list = dataIntent.getStringArrayListExtra("imageList");
            images = getImageUrls(list);
        } else {
            images = (String[]) dataIntent.getSerializableExtra("images");
        }

        mTotalCount = images.length;
        if (images != null && images.length > 0) {
            ViewPagerAdapter adapter = new ViewPagerAdapter(images, new ViewPagerAdapter.ViewPagerAdapterListener() {
                @Override
                public void onFinish() {
                    finish();
                }
            });
            mViewPager.setAdapter(adapter);
            mViewPager.setCurrentItem(mIndex);
            mViewPager.setOnPageChangeListener(this);
            updateViewPaperLabel(mIndex);
        }
    }

    private String[] getImageUrls(List<String> list) {
        String[] imageUrls = new String[list.size()];
        list.toArray(imageUrls);

        return imageUrls;
    }

    public static void start(Context context, int index, String[] imageArray) {
        Intent intent = new Intent();
        intent.setClass(context, ViewPagerActivity.class);
        intent.putExtra("index", index);
        intent.putExtra("images", imageArray);
        context.startActivity(intent);
    }

    protected void updateViewPaperLabel(int position) {
        mViewPaperLabel.setText(String.format("%d/%d", position + 1, mTotalCount));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mIndex = position;
        updateViewPaperLabel(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
