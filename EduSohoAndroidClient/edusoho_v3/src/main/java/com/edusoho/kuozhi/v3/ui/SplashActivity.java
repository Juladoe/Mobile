package com.edusoho.kuozhi.v3.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.view.PointLayout;

import java.io.InputStream;
import java.util.ArrayList;

import jazzyviewpager.JazzyViewPager;
import jazzyviewpager.OutlineContainer;

public class SplashActivity extends Activity {

    private JazzyViewPager mJazzy;
    protected View mSplashOkBtn;
    protected ArrayList<View> mViewList;
    private PointLayout mPointLayout;
    protected JazzyViewPager.TransitionEffect mSplashMode;
    public final static String INIT_APP = "init_app";
    private int mPadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        mPadding = getSplashPadding();
        loadConfig();
        setupJazziness(mSplashMode);
    }

    protected int getSplashPadding() {
        float padding = CommonUtil.parseFloat(getResources().getString(R.string.splash_padding));
        return (int) (EdusohoApp.screenW * padding);
    }

    protected void loadConfig() {
        mSplashMode = JazzyViewPager.TransitionEffect.ZoomIn;
    }

    private void setWindowAlpha(float alpha) {
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0));
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        wl.format = PixelFormat.RGBA_8888;
        wl.alpha = 0.0f;
        window.setAttributes(wl);
    }

    private void setupJazziness(JazzyViewPager.TransitionEffect effect) {
        mJazzy = (JazzyViewPager) findViewById(R.id.jazzy_pager);
        mPointLayout = (PointLayout) findViewById(R.id.pointlayout);
        mJazzy.setTransitionEffect(effect);
        mViewList = initSplashList();
        if (mViewList == null || mViewList.isEmpty()) {
            finish();
            return;
        }
        //add last view
        TextView textView = new TextView(this);
        mViewList.add(textView);

        mJazzy.setAdapter(new SplashAdapter(mViewList));
        mJazzy.setPageMargin(30);

        mPointLayout.setViewPaper(mJazzy);
        mPointLayout.addPointImages(mViewList.size() - 1);
        mJazzy.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int size = mViewList.size();
                if (position < (size - 2)) {
                    mJazzy.setBackgroundColor(Color.WHITE);
                } else {
                    mJazzy.setBackgroundColor(Color.alpha(255));
                }
            }

            @Override
            public void onPageSelected(int position) {
                mPointLayout.refresh();
                int size = mViewList.size();
                if (position == (size - 1)) {
                    finish();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private RelativeLayout createLastSplashView(int imageId) {
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        relativeLayout.setBackgroundColor(Color.WHITE);

        ImageView imageView = new ImageView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(layoutParams);
        imageView.setPadding(mPadding, mPadding, mPadding, mPadding);

        Bitmap bitmap = getBitmap(imageId);

        imageView.setImageBitmap(bitmap);
        imageView.setBackgroundColor(getResources().getColor(R.color.splash_bg));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        relativeLayout.addView(imageView);

        mSplashOkBtn = LayoutInflater.from(this).inflate(R.layout.splash_ok_btn_layout, relativeLayout);
        mSplashOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        return relativeLayout;
    }

    private Bitmap getBitmap(int imageId) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        InputStream inputStream = getResources().openRawResource(imageId);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, opts);

        try {
            inputStream.close();
        } catch (Exception e) {
            //
        }
        return bitmap;
    }

    @Override
    protected void onDestroy() {
        EdusohoApp.app.sendMessage(INIT_APP, null);
        super.onDestroy();
    }

    protected ArrayList<View> createSplashList(int[] imageIds) {
        ArrayList<View> mViewList = new ArrayList<View>();
        int size = imageIds.length;

        for (int i = 0; i < size; i++) {
            if (i == (size - 1)) {
                mViewList.add(createLastSplashView(imageIds[i]));
                continue;
            }
            ImageView imageView = new ImageView(this);
            Bitmap bitmap = getBitmap(imageIds[i]);
            imageView.setImageBitmap(bitmap);
            imageView.setBackgroundColor(getResources().getColor(R.color.splash_bg));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(mPadding, mPadding, mPadding, mPadding);

            mViewList.add(imageView);
        }

        return mViewList;
    }

    public ArrayList<View> initSplashList() {
        return null;
    }

    private class SplashAdapter extends PagerAdapter {
        private ArrayList<View> mViewList;

        public SplashAdapter(ArrayList<View> viewList) {
            this.mViewList = viewList;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = mViewList.get(position);
            container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mJazzy.setObjectForPosition(view, position);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object obj) {
            container.removeView(mJazzy.findViewFromObject(position));
        }

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            if (view instanceof OutlineContainer) {
                return ((OutlineContainer) view).getChildAt(0) == obj;
            } else {
                return view == obj;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
