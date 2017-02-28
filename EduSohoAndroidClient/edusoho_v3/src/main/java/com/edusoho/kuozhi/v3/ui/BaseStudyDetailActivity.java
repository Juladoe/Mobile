package com.edusoho.kuozhi.v3.ui;

import android.animation.ObjectAnimator;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.adapter.SectionsPagerAdapter;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.entity.lesson.Lesson;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.course.ICourseStateListener;
import com.edusoho.kuozhi.v3.ui.fragment.CourseDiscussFragment;
import com.edusoho.kuozhi.v3.util.ActivityUtil;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.WeakReferenceHandler;
import com.edusoho.kuozhi.v3.view.EduSohoNewIconView;
import com.edusoho.kuozhi.v3.view.ScrollableAppBarLayout;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import extensions.PagerSlidingTabStrip;

/**
 * Created by DF on 2017/2/8.
 */

public abstract class BaseStudyDetailActivity extends AppCompatActivity
        implements View.OnClickListener, Handler.Callback, MessageEngine.MessageCallback, AppBarLayout.OnOffsetChangedListener {

    protected MenuPop mMenuPop;
    protected int mRunStatus;
    protected ViewPager mViewPager;
    protected Toolbar mToolbar;
    protected TextView mShareView;
    private CollapsingToolbarLayout mToolBarLayout;
    protected ScrollableAppBarLayout mAppBarLayout;
    protected ViewGroup mParentLayout;
    protected RelativeLayout mMediaLayout;
    protected ImageView mIvBackGraound;
    protected RelativeLayout mPlayButtonLayout;
    protected TextView mTvLast;
    protected ViewGroup mPlayLayout;
    protected TextView mTvPlay;
    protected ViewGroup mBottomLayout;
    protected ViewGroup mConsult;
    protected ViewGroup mCollect;
    protected TextView mTvCollectTxt;
    protected TextView mTvAdd;
    protected TextView mTvInclass;
    protected PagerSlidingTabStrip mTabLayout;
    protected Queue<WidgetMessage> mUIMessageQueue;
    protected View mMenu;
    protected TextView mIvGrade;
    protected View mLoadingView;
    protected TextView mTvEditTopic;
    protected LoadDialog mProcessDialog;
    protected PopupWindow mPopupWindow;
    protected TextView tvTopic;
    protected TextView tvQuestion;
    protected TextView mTvCollect;
    protected View mPlayLayout2;
    protected ViewGroup mAddLayout;
    protected ViewGroup mPlayLastLayout;
    protected TextView mTvPlay2;
    private TextView mBackView;
    protected SectionsPagerAdapter mSectionsPagerAdapter;
    public static final int RESULT_REFRESH = 0x111;
    public static final int RESULT_LOGIN = 0x222;
    protected WeakReferenceHandler mHandler = new WeakReferenceHandler(this);
    protected boolean mIsPlay = false;
    protected boolean mIsMemder = false;
    protected boolean mIsJump = false;
    protected String mTitle;
    public int mMediaViewHeight = 210;
    protected static final int TAB_PAGE = 0;
    protected static final int LOADING_END = 1;
    protected boolean mIsClassroomCourse = false;
//    protected boolean mIsCan = true;
//    private LessonItem lessonItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_study_layout);
        mUIMessageQueue = new ArrayDeque<>();
        ((EdusohoApp) getApplication()).registMsgSource(this);
        ActivityUtil.setStatusBarFitsByColor(this, R.color.transparent);
    }

    public MenuPop getMenu() {
        if (isFinishing()) {
            return null;
        } else {
            return mMenuPop;
        }
    }

    protected void initView() {
        mBackView = (TextView) findViewById(R.id.back);
        mTvEditTopic = (TextView) findViewById(R.id.tv_edit_topic);
        mParentLayout = (ViewGroup) findViewById(R.id.parent_rlayout);
        mMediaLayout = (RelativeLayout) findViewById(R.id.media_rlayout);
        mIvBackGraound = (ImageView) findViewById(R.id.iv_media_background);
        mPlayButtonLayout = (RelativeLayout) findViewById(R.id.layout_play_button);
        mPlayLastLayout = (ViewGroup) findViewById(R.id.layout_play_last);
        mTvLast = (TextView) findViewById(R.id.tv_last_title);
        mPlayLayout = (ViewGroup) findViewById(R.id.play_layout);
        mPlayLayout2 = findViewById(R.id.play_layout2);
        mTvPlay = (TextView) findViewById(R.id.tv_play);
        mTvPlay2 = (TextView) findViewById(R.id.tv_play2);
        mBottomLayout = (ViewGroup) findViewById(R.id.bottom_layout);
        mAddLayout = (ViewGroup) findViewById(R.id.bottom_add_layout);
        mConsult = (ViewGroup) findViewById(R.id.consult_layout);
        mCollect = (ViewGroup) findViewById(R.id.collect_layout);
        mTvCollect = (TextView) findViewById(R.id.tv_collect);
        mTvCollectTxt = (TextView) findViewById(R.id.tv_collect_txt);
        mTvAdd = (TextView) findViewById(R.id.tv_add);
        mTvInclass = (TextView) findViewById(R.id.tv_inclass);
        mAppBarLayout = (ScrollableAppBarLayout) findViewById(R.id.app_bar);
        mToolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mIvGrade = (TextView) findViewById(R.id.iv_grade);
        mShareView = (TextView) findViewById(R.id.iv_share);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mMenu = findViewById(R.id.layout_menu);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(3);
        mTabLayout = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mLoadingView = findViewById(R.id.ll_frame_load);
        setSupportActionBar(mToolbar);
        mTabLayout.setIndicatorColor(R.color.primary_color);
        mMenuPop = new MenuPop(this, mMenu);
        mMenuPop.setOnBindViewVisibleChangeListener(
                new MenuPop.OnBindViewVisibleChangeListener() {
                    @Override
                    public void onVisibleChange(boolean show) {
                        if (show) {
                            mIvGrade.setVisibility(View.GONE);
                        } else {
                            mIvGrade.setVisibility(View.VISIBLE);
                        }
                    }
                });


        setLoadStatus(View.VISIBLE);
        initEvent();
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager(),
                getBaseContext(),
                getTitleArray(),
                getFragmentArray(),
                getIntent().getExtras()
        );
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setViewPager(mViewPager);
    }

    protected void setToolbarLayoutBackground(int color) {
        mToolBarLayout.setContentScrimColor(color);
    }

    protected String[] getTitleArray() {
        return new String[]{
                "简介", "目录", "问答"
        };
    }

    protected abstract String[] getFragmentArray();

    protected void setBottomLayoutState(boolean isShow) {
        mBottomLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mViewPager.getLayoutParams();
        lp.bottomMargin = isShow ? AppUtil.dp2px(getBaseContext(), 50) : 0;
        mViewPager.setLayoutParams(lp);
    }

    protected void refreshFragmentViews(boolean isJoin) {
        List<Fragment> list = getSupportFragmentManager().getFragments();
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Fragment fragment : list) {
            if (fragment instanceof ICourseStateListener) {
                ((ICourseStateListener)fragment).reFreshView(isJoin);
            }
        }
    }

    private void initEvent() {
        mBackView.setOnClickListener(this);
        mShareView.setOnClickListener(this);
        mIvGrade.setOnClickListener(this);
        mPlayLayout.setOnClickListener(this);
        mPlayLayout2.setOnClickListener(this);
        mCollect.setOnClickListener(this);
        mTvAdd.setOnClickListener(this);
        mConsult.setOnClickListener(this);
        mTvInclass.setOnClickListener(this);
        mMenu.setOnClickListener(this);
        mTvEditTopic.setOnClickListener(this);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setBottomLayoutVisible(position, mIsMemder);
                showEditTopic(position);
                statTimes(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    protected void statTimes(int position){
        if (position == 1) {
            MobclickAgent.onEvent(this, "courseDetailsPage_contents");
        } else if(position == 2) {
            MobclickAgent.onEvent(this, "courseDetailsPage_Q&A");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_share) {
            share();
        } else if (v.getId() == R.id.iv_grade) {
            grade();
        } else if (v.getId() == R.id.layout_menu) {
            mMenuPop.showAsDropDown(mMenu, -AppUtil.dp2px(this, 6), AppUtil.dp2px(this, 10));
        } else if (v.getId() == R.id.play_layout || v.getId() == R.id.play_layout2) {
            courseStart();
        } else if (v.getId() == R.id.collect_layout) {
            collect();
        } else if (v.getId() == R.id.tv_add) {
            add();
        } else if (v.getId() == R.id.consult_layout) {
            consult();
        } else if (v.getId() == R.id.tv_inclass) {
            goClass();
        } else if (v.getId() == R.id.tv_edit_topic) {
            showEditPop();
        } else if (v.getId() == R.id.back) {
            if (mIsFullScreen) {
                fullScreen();
            } else {
                finish();
            }
        }
    }

    protected void grade() {
    }

    protected abstract void goClass();

    protected abstract void consult();

    protected abstract void add();

    protected void collect() {
    }

    protected abstract void share();

    protected abstract void refreshView();

    protected abstract void initData();

    protected void setLoadStatus(int visibility) {
        mLoadingView.setVisibility(visibility);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsJump) {
            mIsJump = false;
            hideProcesDialog();
        }
        mRunStatus = MSG_RESUME;
        mAppBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRunStatus = MSG_PAUSE;
        mAppBarLayout.removeOnOffsetChangedListener(this);
    }

    @Override
    public void finish() {
        super.finish();
        ((EdusohoApp) getApplication()).unRegistMsgSource(this);
    }

    @Override
    public void invoke(WidgetMessage message) {
        Bundle bundle = message.data;
        switch (message.type.type) {
            case Const.FULL_SCREEN:
                fullScreen();
                break;
            case Const.COURSE_CHANGE:
                courseChange((LessonItem) bundle.getSerializable(Const.COURSE_CHANGE_OBJECT));
                break;
            case Const.COURSE_HASTRIAL:
                courseHastrial(bundle.getString(Const.COURSE_CHANGE_STATE), (LessonItem) bundle.getSerializable(Const.COURSE_CHANGE_OBJECT));
                break;
            case Const.LOGIN_SUCCESS:
            case Const.WEB_BACK_REFRESH:
                reFreshFromWeb0rLogin();
                break;
        }
    }

    private void reFreshFromWeb0rLogin() {
        setLoadStatus(View.GONE);
        hideProcesDialog();
        initData();
    }

    private void changeToolbarStyle(boolean isTop) {
        if (isTop) {
            setToolbarLayoutBackground(getResources().getColor(R.color.textIcons));
            mShareView.setTextColor(getResources().getColor(R.color.textPrimary));
            mBackView.setTextColor(getResources().getColor(R.color.textPrimary));
        } else {
            setToolbarLayoutBackground(getResources().getColor(R.color.transparent));
            mShareView.setTextColor(getResources().getColor(R.color.textIcons));
            mBackView.setTextColor(getResources().getColor(R.color.textIcons));
        }
        if (this instanceof BaseStudyDetailActivity.WidgtState) {
            ((BaseStudyDetailActivity.WidgtState) this).setTopViewVisibility(isTop);
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mViewPager.getCurrentItem() == 2) {
            if (i == 0) {
                if (((AppBarLayout.LayoutParams) mToolBarLayout.getLayoutParams()).getScrollFlags() == 0) {
                    ((WidgtState) mSectionsPagerAdapter.getItem(2)).setTopViewVisibility(false);
                } else {
                    ((WidgtState) mSectionsPagerAdapter.getItem(2)).setTopViewVisibility(true);
                }
            } else {
                ((WidgtState) mSectionsPagerAdapter.getItem(2)).setTopViewVisibility(false);
            }
        }
        int maxHeight = getResources().getDimensionPixelOffset(R.dimen.action_bar_height);
        int toolbarHeight = AppUtil.dp2px(getBaseContext(), 210);
        if (toolbarHeight + i > maxHeight * 2) {
            changeToolbarStyle(false);
            return;
        }
        changeToolbarStyle(true);
    }


    protected void courseHastrial(String state, LessonItem lessonItem) {
    }

    protected void courseChange(LessonItem lessonItem) {
    }

    private boolean isScreenLock = false;

    private void screenLock() {
        isScreenLock = !isScreenLock;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (isScreenLock) {
                return true;
            }
            if (mIsFullScreen) {
                fullScreen();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void courseStart() {
        if ("开始试学".equals(mTvPlay.getText().toString())) {
            MobclickAgent.onEvent(this, "courseDetailsPage_tryItOut");
        } else if ("继续学习".equals(mTvPlay.getText().toString())) {
            MobclickAgent.onEvent(this, "courseDetailsPage_continueLearning");
        }
        if (!mIsFullScreen) {
            mAppBarLayout.expandToolbar(true);
            AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) mToolBarLayout.getLayoutParams();
            lp.setScrollFlags(0);

            int windowHeight = getResources().getDisplayMetrics().heightPixels;
            mViewPager.getLayoutParams().height = windowHeight - mAppBarLayout.getHeight();
        }
        mPlayButtonLayout.setVisibility(View.GONE);
        setPlayStatus(true);
    }

    private void setPlayStatus(boolean isPlay) {
        mIsPlay = isPlay;
    }

    protected void initViewPager() {
    }

    protected void coursePause() {
        if (!mIsFullScreen) {
            mAppBarLayout.expandToolbar();
            AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) mToolBarLayout.getLayoutParams();
            lp.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);

            mViewPager.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        setPlayStatus(false);
        mIsPlay = false;
        mPlayButtonLayout.setVisibility(View.VISIBLE);
    }

    protected boolean mIsFullScreen = false;

    private void fullScreen() {
        ViewGroup.LayoutParams params = mMediaLayout.getLayoutParams();
        if (!mIsFullScreen) {
            MobclickAgent.onEvent(this, "videoClassroom_fullScreen");
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mIsFullScreen = true;
            params.height = AppUtil.getWidthPx(this);
            params.width = -1;
            mMediaLayout.setLayoutParams(params);
            mBottomLayout.setVisibility(View.GONE);
            mTvInclass.setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mIsFullScreen = false;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = AppUtil.dp2px(this, mMediaViewHeight);
            mMediaLayout.setLayoutParams(params);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (!mIsMemder) {
                mBottomLayout.setVisibility(View.VISIBLE);
            } else {
                mBottomLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public MessageType[] getMsgTypes() {
        return new MessageType[]{
                new MessageType(Const.COURSE_HASTRIAL),
                new MessageType(Const.FULL_SCREEN),
                new MessageType(Const.COURSE_CHANGE),
                new MessageType(Const.LOGIN_SUCCESS),
                new MessageType(Const.WEB_BACK_REFRESH)
        };
    }


    protected void showProcessDialog() {
        if (mProcessDialog == null) {
            mProcessDialog = LoadDialog.create(this);
        }
        mProcessDialog.show();
    }

    protected void hideProcesDialog() {
        if (mProcessDialog == null) {
            return;
        }
        if (mProcessDialog.isShowing()) {
            mProcessDialog.dismiss();
        }
    }

    protected void tabPage(final int sleepTime) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(sleepTime);
                    mHandler.sendEmptyMessage(TAB_PAGE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void tabLoadingGone() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    mHandler.sendEmptyMessage(LOADING_END);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case TAB_PAGE:
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(1);
                    setBottomLayoutVisible(1, mIsMemder);
                    tabLoadingGone();
                }
                break;
            case LOADING_END:
                setLoadStatus(View.GONE);
                break;
        }
        return false;
    }

    protected void showEditTopic(int position) {
        if (position == 2 && mIsMemder) {
            mTvEditTopic.setVisibility(View.VISIBLE);
        } else {
            mTvEditTopic.setVisibility(View.GONE);
        }
    }

    protected abstract void showThreadCreateView(String type);

    private boolean isAdd;

    private void showEditPop() {
        MobclickAgent.onEvent(this, "courseDetailsPage_Q&A_launchButton");
        if (!isAdd) {
            isAdd = true;
            View popupView = getLayoutInflater().inflate(R.layout.dialog_discuss_publish, null);
            mPopupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setTouchable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
            tvTopic = (EduSohoNewIconView) popupView.findViewById(R.id.tv_topic);
            tvTopic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MobclickAgent.onEvent(BaseStudyDetailActivity.this, "courseDetailsPage_Q&A_topic");
                    showThreadCreateView("discussion");
                    mPopupWindow.dismiss();
                }
            });
            tvQuestion = (EduSohoNewIconView) popupView.findViewById(R.id.tv_question);
            tvQuestion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MobclickAgent.onEvent(BaseStudyDetailActivity.this, "courseDetailsPage_questionsAnswers");
                    showThreadCreateView("question");
                    mPopupWindow.dismiss();
                }
            });
            popupView.findViewById(R.id.tv_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopupWindow.dismiss();
                }
            });
        }
        mPopupWindow.showAsDropDown(mTvEditTopic, 0, -AppUtil.dp2px(this, 204));
        startAnimation();
    }

    public void startAnimation() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(tvQuestion, "translationY", 0, -AppUtil.dp2px(BaseStudyDetailActivity.this, 73));
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(tvTopic, "translationY", 0, -AppUtil.dp2px(BaseStudyDetailActivity.this, 146));
        animator.setInterpolator(new LinearInterpolator());
        animator1.setInterpolator(new LinearInterpolator());
        animator.setDuration(150);
        animator1.setDuration(300);
        animator.start();
        animator1.start();
    }

    public void setBottomLayoutVisible(int curFragment, boolean isMember) {
        if (mIsClassroomCourse) {
            mBottomLayout.setVisibility(View.GONE);
            mTvInclass.setVisibility(View.GONE);
        } else {
            if (curFragment == 0) {
                if (isMember) {
                    mBottomLayout.setVisibility(View.VISIBLE);
                    mTvInclass.setVisibility(View.VISIBLE);
                } else {
                    mBottomLayout.setVisibility(View.VISIBLE);
                    mTvInclass.setVisibility(View.GONE);
                }
            } else {
                if (!isMember) {
                    mBottomLayout.setVisibility(View.VISIBLE);
                    mTvInclass.setVisibility(View.GONE);
                } else {
                    mBottomLayout.setVisibility(View.GONE);
                    mTvInclass.setVisibility(View.GONE);
                }
            }
        }

    }

    @Override
    public int getMode() {
        return 0;
    }

    protected void saveMessage(WidgetMessage message) {
        mUIMessageQueue.add(message);
    }

    protected void invokeUIMessage() {
        WidgetMessage message;
        while ((message = mUIMessageQueue.poll()) != null) {
            invoke(message);
        }
    }

    public interface WidgtState{
        void setTopViewVisibility(boolean isTop);
    }
}
