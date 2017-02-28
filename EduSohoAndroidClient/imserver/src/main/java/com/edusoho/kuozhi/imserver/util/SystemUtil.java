package com.edusoho.kuozhi.imserver.util;

import android.app.ActivityManager;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by suju on 16/8/20.
 */
public class SystemUtil {

    public static final int SHOW_KEYBOARD = 1;
    public static final int HIDE_KEYBOARD = 2;

    public static boolean isServiceRunning(Context context, Class serviceClass) {
        if (serviceClass == null) {
            return false;
        }
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void setSoftKeyBoard(View editText, Context context, int status) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getApplicationContext().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (SHOW_KEYBOARD == status) {
            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        } else if (HIDE_KEYBOARD == status) {
            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static void toast(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }

    public static <T> int searchInArray(T[] array, T search) {
        if (array == null || array.length == 0) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (search.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }
}
