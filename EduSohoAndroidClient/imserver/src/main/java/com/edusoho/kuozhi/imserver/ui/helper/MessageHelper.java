package com.edusoho.kuozhi.imserver.ui.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.edusoho.kuozhi.imserver.R;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.managar.IMRoleManager;
import com.edusoho.kuozhi.imserver.ui.util.ChatAudioRecord;
import com.edusoho.kuozhi.imserver.ui.util.ImageUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by suju on 16/8/26.
 */
public class MessageHelper {

    public static final String TAG = "MessageHelper";
    public static final String UPLOAD_AUDIO_CACHE_FILE = "/audio";
    public static final String UPLOAD_IMAGE_CACHE_FILE = "/image";
    public static final String UPLOAD_IMAGE_CACHE_THUMB_FILE = "/image/thumb";

    private Context mContext;

    public MessageHelper(Context context) {
        this.mContext = context;
    }

    private File getMessageStorage() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return mContext.getExternalCacheDir();
        }

        return mContext.getCacheDir();
    }

    public File getAudioStorage() {
        File dir = new File(getMessageStorage(), UPLOAD_AUDIO_CACHE_FILE);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public File getImageStorage() {
        File dir = new File(getMessageStorage(), UPLOAD_IMAGE_CACHE_FILE);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public File getThumbImageStorage() {
        File dir = new File(getMessageStorage(), UPLOAD_IMAGE_CACHE_THUMB_FILE);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public String getRoleAvatar(String type, int rid) {
        Role role = new IMRoleManager(mContext).getRole(type, rid);
        if (role.getRid() != 0) {
            return role.getAvatar();
        }
        return "drawable://" + R.drawable.default_avatar;
    }

    public int getDefaultImageRes() {
        return R.drawable.message_image_default;
    }

    public int[] getThumbImageSize(String path) {
        Uri uri = Uri.parse(path);
        if (uri == null) {
            return new int[] { 80, 80 };
        }

        String uriPath = uri.getLastPathSegment();
        File thumbFile = new File(getThumbImageStorage(), uriPath);
        if (thumbFile.exists()) {
            return ImageUtil.getImageSize(thumbFile.getAbsolutePath());
        }
        return new int[] { 80, 80 };
    }

    public File getRealAudioFile(String audioFile) {
        if (TextUtils.isEmpty(audioFile)) {
            return null;
        }
        String uriPath;
        Uri uri = Uri.parse(audioFile);
        if (uri == null) {
            uriPath = audioFile;
        } else {
            uriPath = uri.getLastPathSegment();
        }
        File realFile = new File(getAudioStorage(), uriPath);
        if (realFile.exists()) {
            return realFile;
        }

        return null;
    }

    public File createAudioFile(String path) throws IOException {
        String uriPath;
        Uri uri = Uri.parse(path);
        if (uri == null) {
            uriPath = path;
        } else {
            uriPath = uri.getLastPathSegment();
        }

        File file = new File(getAudioStorage(), uriPath);
        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }

    public File createImageFile(String path) throws IOException {
        String uriPath;
        Uri uri = Uri.parse(path);
        if (uri == null) {
            uriPath = path;
        } else {
            uriPath = uri.getLastPathSegment();
        }

        File file = new File(getImageStorage(), uriPath);
        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }

    public File createThumbImageFile(String path) throws IOException {
        String uriPath;
        Uri uri = Uri.parse(path);
        if (uri == null) {
            uriPath = path;
        } else {
            uriPath = uri.getLastPathSegment();
        }

        File file = new File(getThumbImageStorage(), uriPath);
        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }

    public String getThumbImagePath(String path) {
        Uri uri = Uri.parse(path);
        String uriPath = uri.getLastPathSegment();
        if (uri == null || TextUtils.isEmpty(uriPath)) {
            return path;
        }

        File thumbFile = new File(getThumbImageStorage(), uriPath);
        if (thumbFile.exists()) {
            return String.format("file://%s", thumbFile.getAbsolutePath());
        }

        File realFile = new File(getImageStorage(), uriPath);
        if (realFile.exists()) {
            return String.format("file://%s", realFile.getAbsolutePath());
        }
        return path;
    }

    public File compressTumbImageByFile(String filePath, int maxWidth) {
        File targetFile = new File(filePath);
        Bitmap tmpBitmap = ImageUtil.compressImage(filePath);
        Bitmap thumbBitmap = ImageUtil.scaleImage(tmpBitmap, maxWidth * 0.3f, ImageUtil.getImageDegree(filePath));
        try {
            targetFile = ImageUtil.convertBitmap2File(thumbBitmap, String.format("%s/%s", getThumbImageStorage(), targetFile.getName()), 50);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!tmpBitmap.isRecycled()) {
            tmpBitmap.recycle();
        }
        if (!thumbBitmap.isRecycled()) {
            thumbBitmap.recycle();
        }

        return targetFile;
    }

    public File compressImageByFile(String filePath) {
        File compressedFile;
        try {
            Bitmap tmpBitmap = ImageUtil.compressImage(filePath);
            Bitmap resultBitmap = ImageUtil.scaleImage(tmpBitmap, tmpBitmap.getWidth(), ImageUtil.getImageDegree(filePath));
            compressedFile = ImageUtil.convertBitmap2File(
                    resultBitmap, String.format("%s/%s", getImageStorage(), String.valueOf(System.currentTimeMillis())), 80);
            if (!tmpBitmap.isRecycled()) {
                tmpBitmap.recycle();
            }

            if (!resultBitmap.isRecycled()) {
                resultBitmap.recycle();
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            return null;
        }
        return compressedFile;
    }
}
