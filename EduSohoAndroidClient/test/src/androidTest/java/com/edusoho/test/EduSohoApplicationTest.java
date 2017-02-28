package com.edusoho.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import com.edusoho.kuozhi.v3.EdusohoApp;
import org.junit.Test;

import java.util.HashMap;

/**
 * Created by howzhi on 15/8/13.
 */

@SmallTest
public class EduSohoApplicationTest extends ApplicationTestCase<EdusohoApp> {

    private EdusohoApp mEdusohoApp;
    public EduSohoApplicationTest() {
        super(EdusohoApp.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        init();
        createApplication();
        mEdusohoApp = getApplication();
    }

    private void init() {
        Context context = getContext();
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_APPEND);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("showSplash", false);
        editor.putBoolean("registPublicDevice", false);
        editor.putBoolean("startWithSchool", false);
        editor.putInt("msgSound", 1);
        editor.putInt("msgVibrate", 1);
        editor.commit();

        sp = context.getSharedPreferences("defaultSchool", Context.MODE_APPEND);
        editor = sp.edit();
        editor.putString("name", context.getString(R.string.school_name));
        editor.putString("url", context.getString(R.string.school_url));
        editor.putString("host", context.getString(R.string.school_host));
        editor.putString("logo", context.getString(R.string.school_logo));
        editor.commit();
    }

    @Test
    public void testAppliction() {
        assertNotNull(mEdusohoApp);
        assertNotNull(mEdusohoApp.mVolley);
        assertNotNull(mEdusohoApp.mEngine);
        assertNotNull(mEdusohoApp.gson);
        assertNotNull(mEdusohoApp.runTask);
    }

    @Test
    public void testAppHostAndDomain() {
        String host = getContext().getString(R.string.app_host);
        String domain = getContext().getString(R.string.app_domain);

        assertEquals(host, mEdusohoApp.host);
        assertEquals(domain, mEdusohoApp.domain);
    }

    @Test
    public void testAppVersion() {
        String apiVersion = getContext().getString(R.string.api_version);
        assertEquals(apiVersion, mEdusohoApp.apiVersion);
    }

    @Test
    public void testGetPlatformInfo() {
        HashMap<String, String> param = mEdusohoApp.getPlatformInfo();
        assertNotNull(param);
    }

    @Test
    public void testAppSchool() {
        assertNull(mEdusohoApp.defaultSchool);
    }

    @Test
    public void testAppConfit() {
        assertNotNull(mEdusohoApp.config);
        assertEquals(false, mEdusohoApp.config.showSplash);
        assertEquals(false, mEdusohoApp.config.startWithSchool);
        assertEquals(false, mEdusohoApp.config.isPublicRegistDevice);
        assertEquals(1, mEdusohoApp.config.msgSound);
        assertEquals(1, mEdusohoApp.config.msgVibrate);
    }
}
