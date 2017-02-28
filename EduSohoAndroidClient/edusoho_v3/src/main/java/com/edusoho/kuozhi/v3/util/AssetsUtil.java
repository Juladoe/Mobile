package com.edusoho.kuozhi.v3.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by su on 2015/12/2.
 */
public class AssetsUtil {

    public static InputStream open(Context context, String name) throws IOException {
        return context.getAssets().open(name);
        /*
        InputStream inputStream = context.getAssets().open(name);
        M3U8Util.DigestInputStream digestInputStream = new M3U8Util.DigestInputStream(
                inputStream, context.getPackageName(), false);

        return digestInputStream;
        */
    }
}
