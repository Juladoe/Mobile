package com.edusoho.kuozhi.v3.ui.fragment.lesson;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.ui.LessonActivity;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.EduSohoNewIconView;
import com.edusoho.kuozhi.v3.view.photo.HackyViewPager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.trinea.android.common.util.ToastUtils;
import photoview.PhotoView;

/**
 * Created by howzhi on 14-9-18.
 */
public class PptLessonFragment extends BaseFragment {

    private static final String PPT_CONFIG = "ppt_config";
    private static final String PPT_INDEX = "%d_%d_ppt_index";

    private HackyViewPager pptViewPager;
    private ArrayList<String> ppts;
    private Bitmap cacheBitmap;
    private LayoutInflater mLayoutInflater;

    private TextView mStartPageView;
    private EduSohoNewIconView mScreenView;
    private View mToolsView;

    private boolean isScreen;
    private int mCurrentIndex;
    private int mLessonId;
    private int mCourseId;
    private int lastIndex;

    @Override
    public String getTitle() {
        return "ppt";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayoutInflater = LayoutInflater.from(mContext);
        cacheBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.defaultpic);
        setContainerView(R.layout.ppt_lesson_layout);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            ppts = bundle.getStringArrayList(LessonActivity.CONTENT);
            mCourseId = bundle.getInt(Const.COURSE_ID);
            mLessonId = bundle.getInt(Const.LESSON_ID);
        }
        initPPTConfig();
    }

    private void initPPTConfig() {
        SharedPreferences sp = getContext().getSharedPreferences(PPT_CONFIG, Context.MODE_PRIVATE);
        mCurrentIndex = sp.getInt(String.format(PPT_INDEX, mCourseId, mLessonId), 0);
    }

    private void savePPTConfig() {
        SharedPreferences sp = getContext().getSharedPreferences(PPT_CONFIG, Context.MODE_PRIVATE);
        sp.edit().putInt(String.format(PPT_INDEX, mCourseId, mLessonId), mCurrentIndex).commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        savePPTConfig();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mScreenView.setText(R.string.font_shrink_screen);
        } else {
            mScreenView.setText(R.string.font_full_screen);
        }
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        mToolsView = view.findViewById(R.id.ppt_lesson_tools);
        mScreenView = (EduSohoNewIconView) view.findViewById(R.id.ppt_page_screen);
        mStartPageView = (TextView) view.findViewById(R.id.ppt_page_start);
        pptViewPager = (HackyViewPager) view.findViewById(R.id.ppt_viewpager);

        if (ppts == null || ppts.isEmpty()) {
            ToastUtils.show(mContext, "课时暂无PPT!");
            return;
        }
        PptPagerAdapter adapter = new PptPagerAdapter(ppts);
        mStartPageView.setText(String.format("%d/%d", mCurrentIndex + 1, ppts.size()));
        pptViewPager.setAdapter(adapter);
        pptViewPager.setOnPageChangeListener(adapter);

        pptViewPager.setCurrentItem(mCurrentIndex);
        mScreenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int orientation = mActivity.getRequestedOrientation();
                if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    MobclickAgent.onEvent(mContext, "pptLearning_fullScreenPpt");
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mScreenView.setText(R.string.font_shrink_screen);
                } else {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mScreenView.setText(R.string.font_full_screen);
                }
            }
        });

        mStartPageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListView listView = new ListView(mContext);
                ArrayList<String> array = new ArrayList<String>();
                for (int i = 1; i <= ppts.size(); i++) {
                    array.add(String.valueOf(i));
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        mContext, R.layout.ppt_lesson_popwindow_list_item, array
                );
                listView.setAdapter(arrayAdapter);
                final PopupWindow popupWindow = new PopupWindow(listView, ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setWidth(mStartPageView.getWidth());
                popupWindow.setHeight(EdusohoApp.screenH / 4);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_bg));
                popupWindow.setFocusable(true);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        pptViewPager.setCurrentItem(i);
                        popupWindow.dismiss();
                    }
                });
                int[] location = new int[2];
                mStartPageView.getLocationOnScreen(location);
                popupWindow.showAtLocation(
                        mStartPageView,
                        Gravity.NO_GRAVITY,
                        location[0],
                        location[1] - 200
                );
            }
        });
    }

    public class PptPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

        private ArrayList<String> mImages;


        public PptPagerAdapter(ArrayList<String> images) {
            mImages = images;
        }

        @Override
        public int getCount() {
            return mImages.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            final View itemView = mLayoutInflater.inflate(R.layout.ppt_lesson_item, null);
            final PhotoView photoView = (PhotoView) itemView.findViewById(R.id.ppt_lesson_image);
            photoView.setEnabled(false);
            ImageLoader.getInstance().displayImage(mImages.get(position), photoView, EdusohoApp.app.mOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    if (bitmap == null) {
                        return;
                    }
                    itemView.findViewById(R.id.ppt_lesson_progress).setVisibility(View.GONE);
                    photoView.setEnabled(true);
                    photoView.setImageBitmap(bitmap);
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                }
            });
            container.addView(itemView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mCurrentIndex = position;
            mStartPageView.setText((position + 1) + "/" + ppts.size());
            if (position < lastIndex) {
                MobclickAgent.onEvent(mContext, "pptLearning_previousPagePpt");
            } else {
                MobclickAgent.onEvent(mContext, "pptLearning_nextPagePpt");
            }
            lastIndex = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}
