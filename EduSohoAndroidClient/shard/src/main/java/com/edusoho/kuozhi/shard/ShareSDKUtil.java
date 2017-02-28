package com.edusoho.kuozhi.shard;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.ArrayMap;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;

/**
 * Created by su on 2015/12/2.
 */
public class ShareSDKUtil {

    private HashMap<String, String> platformMap;
    private Context mContext;

    public void initSDK(Context context) {
        mContext = context;
        platformMap = new HashMap<>(10);
        ShareSDK.initSDK(context);
    }

    private static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }

        return false;
    }

    public Platform[] getPlatformList() {
        return ShareSDK.getPlatformList();
    }

    private void initDevInfo(Context context) {

        try {
            XmlPullParser parser = context.getResources().getXml(R.xml.sharesdk);
            for(int type = parser.getEventType(); type != 1; type = parser.next()) {
                if (type == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    int attrCount = parser.getAttributeCount();
                    HashMap<String,Object> hashMap = new HashMap<String, Object>();
                    for (int attriIndex = 0; attriIndex < attrCount; ++ attriIndex) {
                        String attributeName = parser.getAttributeName(attriIndex);
                        String attributeValue = parser.getAttributeValue(attriIndex).trim();
                        hashMap.put(attributeName, attributeValue);
                    }
                    platformMap.put(name, "");
                    ShareSDK.setPlatformDevInfo(name,hashMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
