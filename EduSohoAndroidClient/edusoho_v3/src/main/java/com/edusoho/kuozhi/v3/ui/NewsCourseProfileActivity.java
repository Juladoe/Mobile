package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailsResult;
import com.edusoho.kuozhi.v3.model.bal.push.New;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.plugin.ShareTool;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.sql.NewDataSource;
import com.edusoho.kuozhi.v3.util.sql.NewsCourseDataSource;
import com.edusoho.kuozhi.v3.util.sql.SqliteChatUtil;
import com.edusoho.kuozhi.v3.view.dialog.PopupDialog;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JesseHuang on 15/9/21.
 */
public class NewsCourseProfileActivity extends ActionBarBaseActivity {

    private TextView tvCourseTitle;
    private ImageView ivCourseImage;
    private TextView tvClearMessage;
    private TextView tvRatingNum;
    private CheckBox cbDonotDisturb;
    private Button btnStudyEntrance;
    private RatingBar rbCourseRating;

    private int mCourseId;
    private CourseDetailsResult mCourseResult;
    private Course mCourseInfo;
    private DisplayImageOptions mOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_course_profile);
        initViews();
        initData();
    }

    private void initViews() {
        tvCourseTitle = (TextView) findViewById(R.id.iv_course_title);
        tvRatingNum = (TextView) findViewById(R.id.tv_rating_num);
        ivCourseImage = (ImageView) findViewById(R.id.iv_course_image);
        tvClearMessage = (TextView) findViewById(R.id.tv_clear_message);
        cbDonotDisturb = (CheckBox) findViewById(R.id.cb_do_not_disturb);
        btnStudyEntrance = (Button) findViewById(R.id.btn_study_entrance);
        rbCourseRating = (RatingBar) findViewById(R.id.rb_course_rating);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mCourseId = intent.getIntExtra(Const.COURSE_ID, 0);
        NewDataSource newsCourseDataSource = new NewDataSource(SqliteChatUtil.getSqliteChatUtil(mContext, app.domain));
        if (mCourseId == 0) {
            CommonUtil.longToast(mContext, getString(R.string.course_params_error));
            return;
        }
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).
                showImageForEmptyUri(R.drawable.default_avatar).
                showImageOnFail(R.drawable.default_course).build();

        New entity = newsCourseDataSource.getNew(mCourseId, app.loginUser.id);
        setBackMode(BACK, entity.getTitle());
        tvCourseTitle.setText(entity.getTitle());
        ImageLoader.getInstance().displayImage(entity.getImgUrl(), ivCourseImage, mOptions);

        cbDonotDisturb.setChecked(app.getMsgDisturbFromCourseId(mCourseId) == 0);
        cbDonotDisturb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.saveMsgDisturbConfig(mCourseId, cbDonotDisturb.isChecked() ? 0 : 3);
            }
        });

        tvClearMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupDialog.createMuilt(mContext, "消息记录", "清除课时历史消息", new PopupDialog.PopupClickListener() {
                    @Override
                    public void onClick(int button) {
                        if (button == PopupDialog.OK) {
                            NewsCourseDataSource newsCourseDataSource = new NewsCourseDataSource(SqliteChatUtil.getSqliteChatUtil(mContext, app.domain));
                            newsCourseDataSource.delete(mCourseId, app.loginUser.id);
                        }
                    }
                }).show();
            }
        });

        btnStudyEntrance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.app.mEngine.runNormalPlugin("WebViewActivity", mContext, new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        String url = String.format(Const.MOBILE_APP_URL, mActivity.app.schoolHost, String.format(Const.MOBILE_WEB_COURSE, mCourseId));
                        startIntent.putExtra(Const.WEB_URL, url);
                    }
                });
            }
        });

        RequestUrl requestUrl = app.bindUrl(Const.COURSE, false);
        Map<String, String> params = requestUrl.getParams();
        params.put("courseId", mCourseId + "");
        app.postUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mCourseResult = parseJsonValue(response, new TypeToken<CourseDetailsResult>() {
                });
                mCourseInfo = mCourseResult.course;
                rbCourseRating.setRating((float) mCourseInfo.rating);
                tvRatingNum.setText(String.format("（%s人）", TextUtils.isEmpty(mCourseInfo.ratingNum) ? "0" : mCourseInfo.ratingNum));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.news_course_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.news_course_profile) {
            if (mCourseResult != null) {
                String url = app.host + "/course/" + mCourseId;
                String title = mCourseInfo.title;
                String about = mCourseInfo.about;
                String pic = mCourseInfo.middlePicture;

                final ShareTool shareTool = new ShareTool(mActivity, url, title, about, pic);
                new Handler((mActivity.getMainLooper())).post(new Runnable() {
                    @Override
                    public void run() {
                        shareTool.shardCourse();
                    }
                });
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
