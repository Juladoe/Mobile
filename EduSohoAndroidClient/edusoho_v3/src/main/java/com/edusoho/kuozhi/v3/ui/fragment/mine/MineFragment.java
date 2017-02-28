package com.edusoho.kuozhi.v3.ui.fragment.mine;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.circleImageView.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JesseHuang on 2017/2/6.
 */

public class MineFragment extends BaseFragment implements AppBarLayout.OnOffsetChangedListener {

    private View rlayoutUserInfo;
    private AppBarLayout appBarLayout;
    private TextView tvName;
    private CircleImageView ivAvatar;
    private TextView tvUserType;
    private TabLayout tbTitles;
    private ViewPager vpContent;
    private String[] mTabTitles = {"学习", "缓存", "收藏", "问答"};
    private String[] mFragmentNames = {"MyStudyFragment", "MyVideoCacheFragment", "MyFavoriteFragment", "MyQuestionFragment"};

    private MinePagerAdapter minePagerAdapter;

    private List<RefreshFragment> mRefreshFragmentList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.fragment_mine);
    }

    @Override
    protected void initView(View view) {
        rlayoutUserInfo = view.findViewById(R.id.rlayout_user_info);
        rlayoutUserInfo.setOnClickListener(getUserViewClickListener());
        appBarLayout = (AppBarLayout) view.findViewById(R.id.app_bar);
        tvName = (TextView) view.findViewById(R.id.tv_name);
        ivAvatar = (CircleImageView) view.findViewById(R.id.iv_avatar);
        tvUserType = (TextView) view.findViewById(R.id.tv_avatar_type);
        tbTitles = (TabLayout) view.findViewById(R.id.tl_titles);
        vpContent = (ViewPager) view.findViewById(R.id.vp_content);
        vpContent.setOffscreenPageLimit(3);
        initUserInfo();
        initViewPager();
        appBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        for (RefreshFragment refreshFragment : mRefreshFragmentList) {
            refreshFragment.setSwipeEnabled(i);
        }
    }

    private void initUserInfo() {
        if (app.loginUser != null) {
            tvName.setText(app.loginUser.nickname);
            tvUserType.setText(app.loginUser.userRole2String());
            ImageLoader.getInstance().displayImage(app.loginUser.getMediumAvatar(), ivAvatar, app.mAvatarOptions);
        }
    }

    private void initViewPager() {
        minePagerAdapter = new MinePagerAdapter(getFragmentManager(), mTabTitles, mFragmentNames);
        vpContent.setAdapter(minePagerAdapter);
        tbTitles.setupWithViewPager(vpContent);
        vpContent.addOnPageChangeListener(getOnPageChangeListener());
    }

    private ViewPager.OnPageChangeListener getOnPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (0 == position) {
                    MobclickAgent.onEvent(mActivity, "i_study");
                } else if (1 == position) {
                    MobclickAgent.onEvent(mActivity, "i_cache");
                } else if (2 == position) {
                    MobclickAgent.onEvent(mActivity, "i_collection");
                } else if (3 == position) {
                    MobclickAgent.onEvent(mActivity, "i_Q&A");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
    }

    private class MinePagerAdapter extends FragmentPagerAdapter {
        private String[] tabTitles;
        private String[] fragmentTags;

        public MinePagerAdapter(FragmentManager fm, String[] titles, String[] tags) {
            super(fm);
            tabTitles = titles;
            fragmentTags = tags;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = app.mEngine.runPluginWithFragment(
                            fragmentTags[position], mActivity, null);
                    break;
                case 1:
                    fragment = app.mEngine.runPluginWithFragment(
                            fragmentTags[position], mActivity, null);
                    break;
                case 2:
                    fragment = app.mEngine.runPluginWithFragment(
                            fragmentTags[position], mActivity, null);
                    break;
                case 3:
                    fragment = app.mEngine.runPluginWithFragment(
                            fragmentTags[position], getActivity(), null);
                    break;
            }
            if (!mRefreshFragmentList.contains(fragment)) {
                mRefreshFragmentList.add((RefreshFragment) fragment);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    @Override
    public MessageType[] getMsgTypes() {
        return new MessageType[]{new MessageType(Const.REFRESH_MY_FRAGMENT)};
    }

    @Override
    public void invoke(WidgetMessage message) {
        MessageType messageType = message.type;
        if (messageType.type.equals(Const.REFRESH_MY_FRAGMENT)) {
            initUserInfo();
            initViewPager();

            for (RefreshFragment fragment : mRefreshFragmentList) {
                fragment.refreshData();
            }
        }
        Log.d("develop", "refreshData: " + this.getClass().getSimpleName());
    }

    public interface RefreshFragment {
        void refreshData();

        void setSwipeEnabled(int i);
    }

    private View.OnClickListener getUserViewClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(mContext, "i_userInformationPortal");
                mActivity.app.mEngine.runNormalPlugin("WebViewActivity", mContext, new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        String url = String.format(Const.MOBILE_APP_URL, mActivity.app.schoolHost, Const.MY_INFO);
                        startIntent.putExtra(Const.WEB_URL, url);
                    }
                });
            }
        };
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View view) {
            super(view);
        }
    }
}
