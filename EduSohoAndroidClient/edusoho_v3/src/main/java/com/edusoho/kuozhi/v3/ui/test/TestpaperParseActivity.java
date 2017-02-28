package com.edusoho.kuozhi.v3.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.volley.Response;
import com.edusoho.kuozhi.v3.model.bal.test.TestpaperResultType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.fragment.test.TestpaperResultFragment;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.google.gson.reflect.TypeToken;

/**
 * Created by howzhi on 14-10-10.
 */
public class TestpaperParseActivity extends TestpaperBaseActivity {

    private int mTestpaperResultId;
    public static TestpaperParseActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }

    @Override
    protected void initView() {
        super.initView();
        loadTestpaperResult();
    }

    @Override
    protected void initIntentData() {
        super.initIntentData();
        Intent intent = getIntent();
        if (intent != null) {
            mTestpaperResultId = intent.getIntExtra(TestpaperResultFragment.RESULT_ID, 0);
        }

        intent.putExtra(FRAGMENT_DATA, new Bundle());
    }

    private void loadTestpaperResult() {
        final LoadDialog loadDialog = LoadDialog.create(mActivity);
        loadDialog.show();
        RequestUrl requestUrl = mActivity.app.bindUrl(Const.TESTPAPER_RESULT, true);
        requestUrl.setParams(new String[]{
                "id", mTestpaperResultId + ""
        });

        ajaxPost(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadDialog.dismiss();
                TestpaperResultType result = parseJsonValue(
                        response, new TypeToken<TestpaperResultType>() {
                        });

                Log.d(null, "parse->testpaper " + result);
                if (result == null) {
                    return;
                }

                mQuestions = result.items;
                mTestpaper = result.testpaper;
                mTestpaperResult = result.paperResult;
                mFavorites = result.favorites;

                app.sendMessage(Const.TESTPAPER_REFRESH_DATA, null);
            }
        }, null);

    }

    public static TestpaperParseActivity getInstance() {
        return instance;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }
}
