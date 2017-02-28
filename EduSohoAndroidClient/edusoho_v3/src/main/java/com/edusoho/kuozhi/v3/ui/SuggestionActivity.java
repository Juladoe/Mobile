package com.edusoho.kuozhi.v3.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JesseHuang on 15/7/7.
 */
public class SuggestionActivity extends ActionBarBaseActivity {
    private EditText mInfoEdt;
    private EditText mContactEdt;
    private RadioGroup mFixRadioGroup;
    private View mSubmitBtn;

    private static final String[] TYPES = {"bug", "fix"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);
        initView();
        setBackMode(BACK, "意见反馈");
    }

    private void initView() {
        mInfoEdt = (EditText) findViewById(R.id.suggestion_info_edt);
        mContactEdt = (EditText) findViewById(R.id.suggestion_contact_edt);
        mFixRadioGroup = (RadioGroup) findViewById(R.id.suggestion_fix_group);
        mSubmitBtn = findViewById(R.id.suggestion_submit);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info = mInfoEdt.getText().toString();
                if (TextUtils.isEmpty(info)) {
                    CommonUtil.shortToast(mContext, "请输入反馈内容!");
                    return;
                }
                String type = TYPES[getCheckIndex()];
                mSubmitBtn.setEnabled(false);
                mSubmitBtn.setEnabled(false);
                sendSuggesion(info, type, mContactEdt.getText().toString());
            }
        });
    }

    private int getCheckIndex() {
        int count = mFixRadioGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            RadioButton radioButton = (RadioButton) mFixRadioGroup.getChildAt(i);
            if (radioButton.isChecked()) {
                return i;
            }
        }
        return 0;
    }

    private void sendSuggesion(String info, String type, String contact) {
        RequestUrl requestUrl = app.bindUrl(Const.SUGGESTION, false);
        Map<String, String> params = requestUrl.getParams();
        params.put("info", info);
        params.put("type", type);
        params.put("contact", contact);
        mActivity.ajaxPostWithLoading(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mSubmitBtn.setEnabled(true);
                mInfoEdt.setText("");
                mContactEdt.setText("");
                CommonUtil.shortToast(mContext, "提交成功！感谢反馈!");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CommonUtil.shortToast(mContext, "提交失败");
                mSubmitBtn.setEnabled(true);
            }
        }, "提交中");
    }
}
