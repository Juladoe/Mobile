package com.edusoho.kuozhi.shard;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;

/**
 * Created by JesseHuang on 15/5/31.
 */
public class ThirdPartyLogin {
    private Context mContext;
    private static ThirdPartyLogin mLogin;
    private static Map<String, Platform> mPlats;
    private ShareSDKUtil mShareSDKUtil;
    private static final String[] LOING_TYPE = { "QQ", "SinaWeibo", "Wechat" };

    private ThirdPartyLogin(Context context) {
        mContext = context;
        mShareSDKUtil = new ShareSDKUtil();
        mShareSDKUtil.initSDK(context);
        mPlats = new HashMap<>();
    }

    public List<String> getLoginTypes() {
        ArrayList<String> types = new ArrayList<String>();
        Platform[] platforms = mShareSDKUtil.getPlatformList();
        for(Platform platform : platforms) {
            for (String type : LOING_TYPE) {
                if (type.equals(platform.getName())) {
                    types.add(type);
                }
            }
        }

        return types;
    }

    public static ThirdPartyLogin getInstance(Context context) {
        if (mLogin == null) {
            mLogin = new ThirdPartyLogin(context);
        }
        return mLogin;
    }

    public void login(PlatformActionListener pl, String name) {
        Platform mPlatform = ShareSDK.getPlatform(mContext, name);
        mPlats.put(name, mPlatform);
        mPlatform.setPlatformActionListener(pl);
        mPlatform.SSOSetting(false);
        mPlatform.showUser(null);
    }

    public void loginOut(String name) {
        Platform mPlatform = mPlats.get(name);
        if (mPlatform != null && mPlatform.isValid()) {
            mPlatform.removeAccount(true);
            mPlats.remove(name);
        }
    }
}
