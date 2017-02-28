package com.edusoho.kuozhi.shard;

import android.view.View;

import cn.sharesdk.framework.TitleLayout;
import cn.sharesdk.framework.authorize.AuthorizeAdapter;

/**
 * Created by JesseHuang on 15/5/31.
 */
public class ShareSDKAdapter extends AuthorizeAdapter {
    @Override
    public void onCreate() {
        hideShareSDKLogo();
        disablePopUpAnimation();
        TitleLayout llTitle = getTitleLayout();
        llTitle.getChildAt(1).setVisibility(View.GONE);
    }
}
