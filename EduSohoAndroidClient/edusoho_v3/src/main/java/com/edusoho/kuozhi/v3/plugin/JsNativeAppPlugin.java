package com.edusoho.kuozhi.v3.plugin;

import android.util.Log;

import com.edusoho.kuozhi.v3.util.annotations.JsAnnotation;
import com.edusoho.kuozhi.v3.view.webview.bridgeadapter.bridge.BaseBridgePlugin;
import com.edusoho.kuozhi.v3.view.webview.bridgeadapter.bridge.BridgeCallback;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by su on 2015/12/9.
 */
public class JsNativeAppPlugin extends BaseBridgePlugin {

    @JsAnnotation
    public void startup(JSONArray args, BridgeCallback callbackContext) throws JSONException {
        Log.d("JsNativeAppPlugin", "startup");
        callbackContext.success("");
    }

    @Override
    public String getName() {
        return "App";
    }
}
