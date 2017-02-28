package com.edusoho.kuozhi.v3.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.edusoho.kuozhi.R;

import java.util.Timer;
import java.util.TimerTask;

public class LoadDialog extends Dialog {

    private TextView loading_txt;

    public LoadDialog(Context context) {
        super(context);
        setContentView(R.layout.load_dig_layout);
        initView();
    }

    private void initView() {
        findViewById(R.id.load_layout).setBackgroundResource(R.drawable.load_bg);
        loading_txt = (TextView) findViewById(R.id.loading_txt);
        setCanceledOnTouchOutside(false);
    }

    public LoadDialog(Context context, int theme) {
        super(context, theme);
        setContentView(R.layout.load_dig_layout);
        initView();
    }

    public void setTextVisible(int visible) {
        loading_txt.setVisibility(visible);
    }

    public static LoadDialog create(Context context) {
        return new LoadDialog(context, R.style.loadDlgTheme);
    }

    public void setMessage(String message) {
        loading_txt.setText(message);
    }

    public void showAutoHide(String message) {
        loading_txt.setText(message);
        show();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LoadDialog.this.dismiss();
            }
        }, 2000);
    }
}
