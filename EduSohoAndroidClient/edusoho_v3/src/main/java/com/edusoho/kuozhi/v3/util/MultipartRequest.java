package com.edusoho.kuozhi.v3.util;

import android.util.Log;
import android.webkit.MimeTypeMap;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.httpentity.FileBody;
import com.edusoho.kuozhi.v3.util.httpentity.FormBodyPart;
import com.edusoho.kuozhi.v3.util.httpentity.MultipartEntity;
import com.edusoho.kuozhi.v3.util.volley.BaseVolleyRequest;
import org.apache.http.HttpEntity;
import org.apache.http.entity.FileEntity;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by JesseHuang on 15/6/28.
 */
public class MultipartRequest extends BaseVolleyRequest<String> {
    /**
     * 对应于服务端get('file')
     * requestUrl.setMuiltParams(new Object[]{"file", imageFile});
     */
    public static final String KEY = "file";
    public static final String TAG = "MutlipartRequest";
    private HttpEntity mHttpEntity;
    private RequestUrl mRequestUrl;

    public MultipartRequest(int method, RequestUrl requestUrl, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, requestUrl, listener, errorListener);
        mRequestUrl = requestUrl;
        mHttpEntity = buildEntity();
        mIsCache = CACHE_NONE;
    }

    private HttpEntity buildEntity() {
        if (getMethod() == Method.PUT) {
            return buildFileEntity();
        } else {
            return buildMultipartEntity();
        }
    }

    private HttpEntity buildFileEntity() {
        Iterator iterator = mRequestUrl.getAllParams().entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            File file = (File) entry.getValue();

            String extension = MimeTypeMap.getFileExtensionFromUrl(file.getName());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            return new FileEntity(file, ContentType.parse(mimeType).getMimeType());
        }
        return null;
    }

    private HttpEntity buildMultipartEntity() {
        MultipartEntity entity = new MultipartEntity();
        Iterator iterator = mRequestUrl.getAllParams().entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            File file = (File) entry.getValue();
            String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            FormBodyPart part = new FormBodyPart(
                    "file", new FileBody(file, ContentType.parse(mimeType).getMimeType()));

            entity.addPart(part);
        }

        return entity;
    }

    @Override
    public String getBodyContentType() {
        return mHttpEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mHttpEntity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected String getResponseData(NetworkResponse response) {
        String data = null;
        try {
            data = new String(response.data, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, String.format("Couldn'type API parse JSON response. NetworkResponse:%s", response.toString()), e);
        }
        return data;
    }
}
