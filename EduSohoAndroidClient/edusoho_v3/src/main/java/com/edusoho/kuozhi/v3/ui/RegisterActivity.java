package com.edusoho.kuozhi.v3.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.entity.register.MsgCode;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.InputUtils;
import com.edusoho.kuozhi.v3.util.Validator;
import com.google.gson.reflect.TypeToken;

import java.util.Map;


/**
 * Created by DF on 2016/11/28.
 */

public class RegisterActivity extends ActionBarBaseActivity {
    private EditText etAccount;
    private TextView tvNext;
    private ImageView ivBack;
    private TextView tvInfo;
    private ImageView ivClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        hideActionBar();
        initView();

    }

    private void initView() {
        tvInfo = (TextView) findViewById(R.id.tv_info);
        etAccount = (EditText) findViewById(R.id.et_phone_num);
        etAccount.addTextChangedListener(mTextChangeListener);
        tvNext = (TextView) findViewById(R.id.tv_next);
        tvNext.setOnClickListener(nextClickListener);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(mBackClickListener);
        ivClear = (ImageView) findViewById(R.id.iv_clear_phone);
        ivClear.setOnClickListener(mClearClickListener);

        InputUtils.showKeyBoard(etAccount, mContext);
    }

    TextWatcher mTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                tvNext.setAlpha(1f);
                ivClear.setVisibility(View.VISIBLE);
            } else {
                tvNext.setAlpha(0.6f);
                ivClear.setVisibility(View.INVISIBLE);
            }
        }
    };

    View.OnClickListener mBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RegisterActivity.this.finish();
        }
    };

    View.OnClickListener mClearClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            etAccount.setText("");

        }
    };

    private View.OnClickListener nextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (etAccount.length() == 0) {
                return;
            }
            if ("".equals(etAccount.getText().toString().trim())) {
                CommonUtil.longToast(mContext, getString(R.string.complete_phone_empty));
                return;
            }
            final String phoneNum = etAccount.getText().toString().trim();
            if (Validator.isPhone(phoneNum)) {
                RequestUrl requestUrl = app.bindUrl(Const.SMS_SEND, false);
                Map<String, String> params = requestUrl.getParams();
                params.put("phoneNumber", String.valueOf(phoneNum));
                mActivity.ajaxPost(requestUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            MsgCode result = parseJsonValue(response, new TypeToken<MsgCode>() {
                            });
                            if (result != null && result.code == 200) {
                                Intent registerIntent = new Intent(mContext, RegisterConfirmActivity.class);
                                registerIntent.putExtra("num", phoneNum);
                                startActivity(registerIntent);
//                                app.mEngine.runNormalPlugin("RegisterConfirmActivity", mContext, null);
                            } else {
                                if (response.equals(getString(R.string.registered_hint))) {
                                    showDialog();
                                } else {
                                    CommonUtil.longToast(mContext, response);
                                }
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "phone reg error");
                        }
                    }
                }, null);
            } else {
                CommonUtil.longToast(mContext, getString(R.string.register_error));
            }
        }
    };

    /**
     * 弹窗提示已注册
     */
    private void showDialog() {
        new AlertDialog.Builder(RegisterActivity.this)
                .setMessage(R.string.register_hint)
                .setPositiveButton(R.string.register_login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        RegisterActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.register_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommonUtil.longToast(mContext, getString(R.string.register_modify_phone));
                    }
                }).show();

    }

}

