package com.edusoho.kuozhi;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.SystemInfo;
import com.edusoho.kuozhi.v3.model.result.SchoolResult;
import com.edusoho.kuozhi.v3.model.sys.AppConfig;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.M3U8Util;
import com.edusoho.kuozhi.v3.view.dialog.PopupDialog;
import com.edusoho.kuozhi.v3.view.webview.ESCordovaWebViewFactory;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.edusoho.kuozhi.v3.ui.QrSchoolActivity;


public class CustomStartActivity extends com.edusoho.kuozhi.v3.ui.StartActivity {


    /**
     * 处理网校异常dlg
     */
    protected void showSchoolErrorDlg() {
       PopupDialog popupDialog = PopupDialog.createMuilt(
               mContext,
               "无法连接到服务器",
               "可能是由于网络连接异常或服务器故障! \n请检查网络设置",
               new PopupDialog.PopupClickListener() {
                   @Override
                   public void onClick(int button) {
                       if (button == PopupDialog.OK) {
                           finish();
                       }
                   }
               }

        );
        popupDialog.setOkText("退出");
        popupDialog.show();
    }
}

