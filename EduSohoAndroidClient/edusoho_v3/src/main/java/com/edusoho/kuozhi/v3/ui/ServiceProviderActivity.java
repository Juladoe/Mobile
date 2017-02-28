package com.edusoho.kuozhi.v3.ui;


import android.content.Intent;
import android.text.TextUtils;

import com.edusoho.kuozhi.v3.util.ServiceProviderUtil;

/**
 * Created by howzhi on 15/9/18.
 */
public class ServiceProviderActivity extends FragmentPageActivity {

    public static final String ARTICLE = "news";
    public static final String COURSE = "Course";
    public static final String SERVICE_ID = "id";
    public static final String CONV_NO = "conv_no";
    public static final String SERVICE_TYPE = "type";
    public static String SERVICE_NAME;

    @Override
    protected void initView() {
        Intent data = getIntent();

        String type = data.getStringExtra(SERVICE_TYPE);
        String fragmentName = getFragmentNameByType(type);
        SERVICE_NAME = fragmentName;
        data.putExtra(FRAGMENT, fragmentName);
        super.initView();
    }

    private static String getFragmentNameByType(String type) {
        return ServiceProviderUtil.coverFragmentName(type);
    }

    public static boolean isRunWithFragmentByType(String type) {
        if (TextUtils.isEmpty(SERVICE_NAME)) {
            return false;
        }

        return SERVICE_NAME.equals(getFragmentNameByType(type));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SERVICE_NAME = null;
    }
}
