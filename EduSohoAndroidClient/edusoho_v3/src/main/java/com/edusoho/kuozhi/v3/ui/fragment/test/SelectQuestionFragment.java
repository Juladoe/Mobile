package com.edusoho.kuozhi.v3.ui.fragment.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by howzhi on 14-9-24.
 */
public abstract class SelectQuestionFragment extends QuestionTypeBaseFragment
        implements ViewPager.OnPageChangeListener{

    protected File mCameraImageFile;
    protected int mCameraIndex;
    protected int mQuestionCount;
    protected ViewPager mQuestionPager;
    protected NormalCallback mEssayQWCallback;

    public static final int PHOTO = 0001;
    public static final int CAMERA = 0002;
    /**
     * 从手机图库中选择图片返回结果表示
     */
    public static final int IMAGE_RESULT = 1;

    public static final int CAMERA_RESULT = 2;

    @Override
    protected void initView(View view) {
        super.initView(view);
        mQuestionPager = (ViewPager) view.findViewById(R.id.question_pager);
        mQuestionPager.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setQuestionNumber(position + 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    protected void setQuestionNumber(int position)
    {
        String text = String.format("%d/%d", position, mQuestionCount);
        SpannableString spannableString = new SpannableString(text);
        int color = getResources().getColor(R.color.action_bar_bg);
        int length = getNumberLength(position);
        spannableString.setSpan(
                new ForegroundColorSpan(color), 0, length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(
                new RelativeSizeSpan(2.0f), 0, length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        mQuestionNumber.setText(spannableString);
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

    protected void uploadImage(int type, String path, final NormalCallback<String> callback) {
        final LoadDialog loadDialog = LoadDialog.create(mActivity);
        loadDialog.setMessage("上传中...");
        loadDialog.show();

        File imageFile = null;
        if (type == PHOTO) {
            imageFile = new File(path);
            if (imageFile.exists()) {
                Log.d(null, "compress imageFile->" + imageFile);
                imageFile = compressImage(imageFile);
            }
        } else {
            imageFile = compressImage(new File(path));
        }
        RequestUrl requestUrl = app.bindUrl(Const.UPLOAD_IMAGE, true);
        requestUrl.setMuiltParams(new Object[]{
                "file", imageFile
        });

        mActivity.ajaxPostMultiUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadDialog.dismiss();
                String result = mActivity.parseJsonValue(
                        response, new TypeToken<String>() {
                        });

                Log.d(null, "upload result->" + result);
                if (result == null || TextUtils.isEmpty(result)) {
                    CommonUtil.longToast(mContext, "上传失败!服务器暂不支持过大图片");
                }
                callback.success(String.format("<img src='%s'/>", result));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CommonUtil.longToast(mContext, "上传失败!服务器暂不支持过大图片");
                loadDialog.dismiss();
            }
        }, Request.Method.POST);
    }

    protected void camera() {
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
                e.printStackTrace();
                Toast.makeText(mContext, "照片创建失败!", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraImageFile));
        startActivityForResult(intent, CAMERA_RESULT);
    }

    private File compressImage(File file) {
        Bitmap bitmap = null;
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), option);
        option.inSampleSize = AppUtil.computeSampleSize(option, -1, EdusohoApp.screenW * EdusohoApp.screenH);
        option.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), option);
        File cacheDir = EdusohoApp.getWorkSpace();
        File targetFile = new File(cacheDir, file.getName());

        try {
            if (targetFile == null || !targetFile.exists()) {
                targetFile.createNewFile();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
            targetFile = file;
        }

        Log.d(null, "compress file->" + targetFile);
        return targetFile;
    }

    @Override
    public MessageType[] getMsgTypes() {
        String source = this.getClass().getSimpleName();
        MessageType[] messageTypes = new MessageType[]{
                new MessageType(PHOTO, source),
                new MessageType(CAMERA, source),
                new MessageType(Const.TESTPAPER_REFRESH_DATA)
        };
        return messageTypes;
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
                camera();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMAGE_RESULT:
                if (null != data) {
                    final String filePath = convertUriToPath(data.getData());
                    final Bundle bundle = new Bundle();
                    Log.d(null, "inser->" + filePath);
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

    private String convertUriToPath(Uri uri) {
        return AppUtil.getPath(mContext, uri);
    }

}
