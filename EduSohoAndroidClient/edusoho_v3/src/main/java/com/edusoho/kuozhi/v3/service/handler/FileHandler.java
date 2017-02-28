package com.edusoho.kuozhi.v3.service.handler;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.m3u8.M3U8DbModel;
import com.edusoho.kuozhi.v3.model.sys.Cache;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.M3U8Util;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

import cn.trinea.android.common.util.DigestUtils;

/**
 * Created by howzhi on 14-10-25.
 */
public class FileHandler implements HttpRequestHandler {

    private static final String TAG = "FileHandler";
    private static final String HOST_TAG = "localhost:8800";

    private int mUserId;
    private String mTargetHost;
    private Context mContext;

    public FileHandler(Context context, String targetHost, int userId) {
        Uri hostUri = Uri.parse(targetHost);
        if (hostUri != null) {
            this.mTargetHost = hostUri.getHost();
        }
        this.mUserId = userId;
        this.mContext = context;
    }

    @Override
    public void handle(
            final HttpRequest httpRequest, final HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException {

        Header host = httpRequest.getFirstHeader("Host");
        if (host == null || !HOST_TAG.startsWith(host.getValue())) {
            return;
        }
        String url = httpRequest.getRequestLine().getUri();
        url = url.substring(1, url.length());
        Uri queryUri = Uri.parse(url);

        String queryName = queryUri.toString();
        Log.d(getClass().getSimpleName(), "queryName->" + queryName);

        if (queryName.startsWith("playlist")) {
            int lessonId = CommonUtil.parseInt(queryName.substring("playlist/".length(), queryName.length() - ".m3u8".length()));
            M3U8DbModel m3U8DbModel = M3U8Util.queryM3U8Model(
                    mContext, mUserId, lessonId, this.mTargetHost, M3U8Util.ALL);
            if (m3U8DbModel != null) {
                //m3U8DbModel.playList = filterUploadInfo(m3U8DbModel.playList);
                StringEntity entity = new StringEntity(m3U8DbModel.playList, "utf-8");
                entity.setContentType("application/vnd.apple.mpegurl");
                entity.setContentEncoding("utf-8");
                httpResponse.setEntity(entity);
                return;
            }
        }

        //判断是不是key
        if (queryName.startsWith("ext_x_key")) {
            SqliteUtil sqliteUtil = SqliteUtil.getUtil(mContext);
            Cache keyCache = sqliteUtil.query(
                    "select * from data_cache where key=? and type=?",
                    queryName,
                    Const.CACHE_KEY_TYPE
            );
            if (keyCache != null) {
                httpResponse.setEntity(new StringEntity(keyCache.value));
                return;
            }
        }

        //本地ts文件
        String[] tsUrl = queryName.split("[?]");
        if (tsUrl.length > 0) {
            queryName = tsUrl[0];
        }
        File videoFile = getLocalFile(queryName);
        if (videoFile.exists()) {
            Log.d(null, "cache->" + videoFile);
            FileEntity fileEntity = new WrapFileEntity(videoFile, mTargetHost);
            //httpResponse.setHeader("Content-Type", "video/mp2t; charset=UTF-8");
            httpResponse.setEntity(fileEntity);
            return;
        }

        HttpEntity entity = proxyRequest(queryUri.getHost(), queryName);
        httpResponse.setEntity(entity);
    }

    private HttpEntity proxyRequest(String host, String url) {
        try {
            Log.d(TAG, String.format("proxy host->%s, url->%s", host, url));
            Socket outsocket = new Socket(host, 80);
            DefaultHttpClientConnection conn = new DefaultHttpClientConnection();
            conn.bind(outsocket, new BasicHttpParams());

            HttpProcessor httpproc = new BasicHttpProcessor();
            HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

            HttpRequest request = new BasicHttpRequest("GET", url);
            Log.d(TAG, "proxy url->" + request.getRequestLine().getUri());
            HttpContext context = new BasicHttpContext();

            HttpHost httpHost = new HttpHost(host, 80);
            httpexecutor.preProcess(request, httpproc, context);
            HttpResponse response = httpexecutor.execute(request, conn, context);
            httpexecutor.postProcess(response, httpproc, context);

            HttpEntity entity = response.getEntity();

            String type = entity.getContentType().getValue();
            if (type.equals("application/vnd.apple.mpegurl")) {
                String entityStr = EntityUtils.toString(entity);
                entityStr = reEncodeM3U8File(entityStr);
                return new StringEntity(entityStr, /*"application/vnd.apple.mpegurl",*/ "utf-8");
            } else if (type.equals("video/mp2t")) {
                WrapInputStream wrapInput = new WrapInputStream(url, entity.getContent());
                HttpEntity wrapEntity = new InputStreamEntity(wrapInput, wrapInput.available());
                return wrapEntity;
            }

            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String reEncodeM3U8File(String text) {
        return text.replaceAll("http://", "http://localhost:5820/http://");
    }

    private class WrapFileEntity extends FileEntity {

        private String mHost;

        public WrapFileEntity(File file, String host) {
            super(file, "video/mp2t");
            this.mHost = host;
        }

        @Override
        public void writeTo(OutputStream outstream) throws IOException {
            M3U8Util.DigestInputStream instream = new M3U8Util.DigestInputStream(
                    new FileInputStream(this.file)
                    , mHost
            );
            try {
                byte[] tmp = new byte[4096];
                int l;
                while ((l = instream.read(tmp)) != -1) {
                    outstream.write(tmp, 0, l);
                }
                outstream.flush();
            } finally {
                instream.close();
            }
        }
    }

    public class WrapInputStream extends BufferedInputStream {
        private String name;
        private FileOutputStream outputStream;
        private boolean mWriteMode;

        public WrapInputStream(InputStream in) {
            super(in);
        }

        public WrapInputStream(String name, InputStream in) {
            super(in);
            this.name = name;
            try {
                String md5Name = DigestUtils.md5(name);
                File videoFile = getLocalFile(md5Name);
                outputStream = new FileOutputStream(videoFile);
                mWriteMode = true;
                Log.d(TAG, "create file->" + md5Name);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            if (mWriteMode) {
                outputStream.write(b);
            } else {
                Log.d(null, "temp read->");
            }
            return super.read(b);
        }

        @Override
        public synchronized int read(byte[] b, int off, int len)
                throws IOException {
            if (mWriteMode) {
                outputStream.write(b, off, len);
            }
            return super.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (mWriteMode) {
                outputStream.close();
            }
            Log.d(TAG, "outputStream close");
        }
    }

    private File getLocalFile(String name) {
        File videoDir = getVideoDir();
        File videoFile = new File(videoDir, name);
        return videoFile;
    }

    private File getVideoDir() {
        File workSpace = EdusohoApp.getWorkSpace();
        if (workSpace == null) {
            return null;
        }

        StringBuffer dirBuilder = new StringBuffer(workSpace.getAbsolutePath());
        dirBuilder.append("/videos/")
                .append(mUserId)
                .append("/")
                .append(mTargetHost);

        return new File(dirBuilder.toString());
    }

    private String filterUploadInfo(String playList) {
        Pattern pattern = Pattern.compile("\\?schoolId.*\\n");
        Matcher matcher = pattern.matcher(playList);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "\n");
        }
        return sb.toString() + "";
    }

    private String filterUploadUrl(String playUrl) {
        String[] url = playUrl.split("[?]");
        if (url.length > 0) {
            return url[1];
        }
        return null;
    }
}