package com.edusoho.test.utils;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.AndroidTestCase;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.ContextThemeWrapper;

import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.OpenLoginUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * Created by howzhi on 15/8/25.
 */
public class OpenLoginUtilTest extends AndroidTestCase{

    private OpenLoginUtil mOpenLoginUtil;

    @Before
    public void setUp() {
        mOpenLoginUtil = OpenLoginUtil.getUtil(getContext());
    }

    @Test
    public void testQQLogin() {

        HashMap<String, Object> res = new HashMap<>();
        res.put("nickname", "edusoho");
        res.put("figureurl_qq_2", "avatar");
        res.put("id", "1");
        String[] params = mOpenLoginUtil.bindByPlatform("QQ", res);

        assertNotNull(params);
        assertEquals("1", params[0]);
    }
}
