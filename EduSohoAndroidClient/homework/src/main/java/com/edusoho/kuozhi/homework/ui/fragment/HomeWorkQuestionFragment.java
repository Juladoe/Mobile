package com.edusoho.kuozhi.homework.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.adapter.HomeworkQuestionAdapter;
import com.edusoho.kuozhi.homework.listener.IHomeworkQuestionResult;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by howzhi on 15/10/16.
 */
public class HomeWorkQuestionFragment extends BaseFragment implements ViewPager.OnPageChangeListener {

    public static final int PHOTO = 0001;
    public static final int CAMERA = 0002;
    public static final int SELECT_QUESTION = 0003;
    /**
     * 从手机图库中选择图片返回结果表示
     */
    private static final int IMAGE_RESULT = 1;

    private static final int CAMERA_RESULT = 2;
    private File mCameraImageFile;
    private int mCameraIndex;
    private NormalCallback mEssayQWCallback;

    protected int mCurrentIndex;
    protected int mHomeworkQuestionCount;
    protected TextView mQuestionIndexView;
    protected TextView mQuestionTitleView;
    protected ViewPager mHomeworkQuestionPager;
    private IHomeworkQuestionResult mQuestionResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.homework_question_layout);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mQuestionResult = (IHomeworkQuestionResult) activity;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        mQuestionIndexView = (TextView) view.findViewById(R.id.homework_index);
        mQuestionTitleView = (TextView) view.findViewById(R.id.homework_title);
        mHomeworkQuestionPager = (ViewPager) view.findViewById(R.id.homework_viewpaper);
        mHomeworkQuestionPager.setOnPageChangeListener(this);

        mQuestionTitleView.setText(getArguments().getString(Const.ACTIONBAR_TITLE));
        List<HomeWorkQuestion> questionList = mQuestionResult.getQuestionList();
        mHomeworkQuestionCount = questionList.size();
        HomeworkQuestionAdapter adapter = new HomeworkQuestionAdapter(mQuestionResult.getType(), mContext, questionList);
        mHomeworkQuestionPager.setAdapter(adapter);
        setHomeworkIndex(mCurrentIndex = 1);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setHomeworkIndex(position + 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    protected void setHomeworkIndex(int position)
    {
        String text = String.format("%d/%d", position, mHomeworkQuestionCount);
        SpannableString spannableString = new SpannableString(text);
        int color = getResources().getColor(R.color.action_bar_bg);
        int length = getNumberLength(position);
        spannableString.setSpan(
                new ForegroundColorSpan(color), 0, length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(
                new RelativeSizeSpan(1.5f), 0, length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        mQuestionIndexView.setText(spannableString);
    }

    private int getNumberLength(int number)
    {
        int length = 1;
        while (number >= 10) {
            length ++;
            number = number / 10;
        }

        return length;
    }

    @Override
    public void invoke(WidgetMessage message) {
        super.invoke(message);
        int type = message.type.code;
        mEssayQWCallback = message.callback;
        switch (type) {
            case PHOTO:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_RESULT);
                break;
            case CAMERA:
                getPhotoFromCamera();
                break;
            case SELECT_QUESTION:
                int currentIndex = mQuestionResult.getCurrentQuestionIndex();
                if (mCurrentIndex != currentIndex) {
                    mHomeworkQuestionPager.setCurrentItem(mCurrentIndex = currentIndex);
                }
        }
    }

    @Override
    public MessageType[] getMsgTypes() {
        String source = getClass().getSimpleName();
        return new MessageType[] {
                new MessageType(PHOTO, source),
                new MessageType(CAMERA, source),
                new MessageType(SELECT_QUESTION, source, MessageType.UI_THREAD)
        };
    }

    private void getPhotoFromCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
        }
        File saveDir = EdusohoApp.getWorkSpace();
        mCameraImageFile = new File(saveDir, "caremaImage" + mCameraIndex + ".jpg");
        mCameraIndex++;
        if (!mCameraImageFile.exists()) {
            try {
                mCameraImageFile.createNewFile();
            } catch (IOException e) {
                Toast.makeText(mContext, "照片创建失败!", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraImageFile));
        startActivityForResult(intent, CAMERA_RESULT);
    }

    private void uploadImage(int type, String path, final NormalCallback<String> callback) {
        final LoadDialog loadDialog = LoadDialog.create(mActivity);
        loadDialog.setMessage("上传中...");
        loadDialog.show();

        File imageFile = null;
        imageFile = new File(path);
        if (imageFile.exists()) {
            Bitmap bitmap = AppUtil.getBitmapFromFile(imageFile);
            bitmap = AppUtil.compressImage(bitmap, null);
            try {
                String cacheFile = new File(AppUtil.getAppCacheDir(), imageFile.getName()).getAbsolutePath();
                imageFile = AppUtil.convertBitmap2File(bitmap, cacheFile);
            } catch (Exception e) {

            }
        }

        RequestUrl requestUrl = app.bindNewUrl(String.format(Const.FILE_UPLOAD, "default"), true);
        requestUrl.setMuiltParams(new Object[]{
                "file", imageFile
        });

        mActivity.ajaxPostMultiUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadDialog.dismiss();
                LinkedHashMap<String, String> resultMap = mActivity.parseJsonValue(
                        response, new TypeToken<LinkedHashMap<String, String>>(){});
                if (!"200".equals(resultMap.get("code"))) {
                    CommonUtil.longToast(mActivity.getBaseContext(), "上传失败");
                    return;
                }

                String url = resultMap.get("message");
                callback.success(url);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CommonUtil.longToast(mContext, "上传失败!服务器暂不支持过大图片");
                loadDialog.dismiss();
            }
        }, Request.Method.POST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMAGE_RESULT:
                if (null != data) {
                    final String filePath = AppUtil.getPath(mContext, data.getData());
                    final Bundle bundle = new Bundle();
                    bundle.putString("file", filePath);
                    uploadImage(PHOTO, filePath, new NormalCallback<String>() {
                        @Override
                        public void success(String obj) {
                            if (mEssayQWCallback != null) {
                                bundle.putString("image", obj);
                                mEssayQWCallback.success(bundle);
                            }
                        }
                    });
                }
                break;
            case CAMERA_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    if (mCameraImageFile != null & mCameraImageFile.exists()) {
                        final Bundle bundle = new Bundle();
                        bundle.putString("file", mCameraImageFile.getPath());
                        uploadImage(CAMERA, mCameraImageFile.getPath(), new NormalCallback<String>() {
                            @Override
                            public void success(String obj) {
                                if (mEssayQWCallback != null) {
                                    bundle.putString("image", obj);
                                    mEssayQWCallback.success(bundle);
                                }
                            }
                        });
                    }
                }
                break;
        }
    }
}
