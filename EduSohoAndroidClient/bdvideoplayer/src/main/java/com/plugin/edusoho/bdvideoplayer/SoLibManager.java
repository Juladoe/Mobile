package com.plugin.edusoho.bdvideoplayer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.baidu.cyberplayer.utils.ZipUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by su on 2015/11/30.
 */
public class SoLibManager {

    private DownProcessListener mListener;

    public SoLibManager(DownProcessListener listener) {
        this.mListener = listener;
    }

    public static interface DownProcessListener {
        public void update(int count, int process);
    }

    private File getFileFromNet(String url, Context context) {
        File target = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        FileOutputStream zipFileOutput = null;
        try {
            zipFileOutput = context.openFileOutput("libso.zip", Context.MODE_PRIVATE);
            HttpResponse response = client.execute(httpGet);
            int len = -1;
            byte[] buffer = new byte[8192];
            int downLenth = 0;
            int total = (int) response.getEntity().getContentLength();
            InputStream inputStream = response.getEntity().getContent();
            while ((len = inputStream.read(buffer)) != -1) {
                mListener.update(total, downLenth += len);
                zipFileOutput.write(buffer, 0, len);
            }

            zipFileOutput.close();
            target = context.getFileStreamPath("libso.zip");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpGet.abort();
            client.getConnectionManager().shutdown();
            try {
                if (zipFileOutput != null) {
                    zipFileOutput.close();
                }
            } catch (Exception e) {
                //nothing
            }
        }

        return target;
    }

    public boolean downPlayerSoLib(String type, Context context) {
        try {
            File target = getFileFromNet(String.format("http://download.edusoho.com/android-%svideolib-1.0.0.zip", type), context);
            if (target == null) {
                return false;
            }
            File libDir = context.getDir(type + "lib", Context.MODE_PRIVATE);
            ZipUtils.getInstance().unZip(context, target.getAbsolutePath(), libDir.getAbsolutePath() + "/");
            Log.d("SoLibManager", "zip success");
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
