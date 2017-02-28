package com.edusoho.kuozhi.v3.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.entity.register.ErrorCode;
import com.edusoho.kuozhi.v3.entity.register.FindPasswordSmsCode;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.result.UserResult;
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
public class CompletePhoneActivity extends ActionBarBaseActivity {
    private EditText etPhone;
    private TextView tvNext;
    private ImageView ivBack;
    private TextView tvInfo;
    private ImageView ivClearPhone;
    private RelativeLayout rl;
    private EditText etCode;
    private ImageView ivGraph;
    private TextView tvGraph;
    private Bundle bundle;
    private String verified;
    private ImageView ivClearCode;
    private UserResult userResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideActionBar();
        setContentView(R.layout.activity_complete_phone);

        userResult = (UserResult) getIntent().getExtras().getSerializable("user");
        initView();
        initGraphContent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initCodeCoent();
    }

    private void initGraphContent() {
        rl = (RelativeLayout) findViewById(R.id.rl_graphic);
        etCode = (EditText) findViewById(R.id.et_graphic_code);
        ivGraph = (ImageView) findViewById(R.id.iv_graphic);
        tvGraph = (TextView) findViewById(R.id.tv_change);
        ivClearCode = (ImageView) findViewById(R.id.iv_clear_code);
        ivClearCode.setOnClickListener(mClearListener);
        initCodeCoent();
        tvGraph.setOnClickListener(mChangListener);
        InputUtils.addTextChangedListener(etCode, new NormalCallback<Editable>() {
            @Override
            public void success(Editable obj) {
                if (etCode.length() == 0) {
                    ivClearCode.setVisibility(View.INVISIBLE);
                } else {
                    ivClearCode.setVisibility(View.VISIBLE);
                }
                if (etCode.length() == 0 || etPhone.length() == 0) {
                    tvNext.setAlpha(0.6f);
                } else {
                    tvNext.setAlpha(1.0f);
                }
            }
        });
    }

    View.OnClickListener mChangListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RequestUrl requestUrl = app.bindNewUrl(Const.SEND_SMS, false);
            requestUrl.heads.put("Auth-Token", app.token);
            Map<String, String> params = requestUrl.getParams();
            params.put("mobile", etPhone.getText().toString().trim());
            params.put("type", "sms_bind");
            mActivity.ajaxPost(requestUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    FindPasswordSmsCode result = parseJsonValue(response, new TypeToken<FindPasswordSmsCode>() {
                    });
                    if (result != null) {
                        byte[] byteArray = Base64.decode(result.img_code, Base64.DEFAULT);
                        ivGraph.setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
                        etCode.setText("");
                        verified = result.verified_token;
                        tvNext.setAlpha(0.6f);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    CommonUtil.shortCenterToast(mContext, getResources().getString(R.string.request_fail_text));
                }
            });
        }
    };

    private void initView() {
        tvInfo = (TextView) findViewById(R.id.tv_info);
        tvInfo.setText(R.string.complete_info);
        etPhone = (EditText) findViewById(R.id.et_phone_num);
        tvNext = (TextView) findViewById(R.id.tv_next);
        tvNext.setOnClickListener(nextClickListener);
        ivClearPhone = (ImageView) findViewById(R.id.iv_clear_phone);
        ivClearPhone.setOnClickListener(mClearListener);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(mBackClickListener);
        InputUtils.showKeyBoard(etPhone, this);
        InputUtils.addTextChangedListener(etPhone, new NormalCallback<Editable>() {
            @Override
            public void success(Editable obj) {
                if (etPhone.length() == 0) {
                    ivClearPhone.setVisibility(View.INVISIBLE);
                } else {
                    ivClearPhone.setVisibility(View.VISIBLE);
                }
                if (bundle != null) {
                    if (etPhone.length() == 0 || etPhone.length() == 0) {
                        tvNext.setAlpha(0.6f);
                    } else {
                        tvNext.setAlpha(1.0f);
                    }
                } else {
                    if (etPhone.length() == 0) {
                        tvNext.setAlpha(0.6f);
                    } else {
                        tvNext.setAlpha(1.0f);
                    }
                }
            }
        });
    }

    View.OnClickListener mClearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_clear_phone) {
                etPhone.setText("");
            } else if (v.getId() == R.id.iv_clear_code) {
                etCode.setText("");
            }
        }
    };
    View.OnClickListener mBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CompletePhoneActivity.this.finish();
        }
    };
    private View.OnClickListener nextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (etPhone.length() == 0) {
                return;
            }
            if ("".equals(etPhone.getText().toString().trim())) {
                CommonUtil.shortCenterToast(mContext, getString(R.string.complete_phone_empty));
                return;
            }
            if (etCode.length() == 0 && bundle != null) {
                return;
            }
            if ("".equals(etCode.getText().toString().trim()) && bundle != null) {
                CommonUtil.shortCenterToast(mContext, getString(R.string.img_code_hint));
                return;
            }
            final String phoneNum = etPhone.getText().toString().trim();
            if (Validator.isPhone(phoneNum)) {
                final RequestUrl requestUrl = app.bindNewUrl(Const.SEND_SMS, true);
                if (bundle != null) {
                    Map<String, String> params = requestUrl.getParams();
                    params.put("mobile", phoneNum);
                    String img_code = etCode.getText().toString().trim();
                    params.put("img_code", img_code);
                    params.put("type", "sms_bind");
                    params.put("verified_token", verified);
                    app.postUrl(requestUrl, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            ErrorCode errorCode = null;
                            FindPasswordSmsCode result = null;
                            if (response.contains("message")) {
                                errorCode = parseJsonValue(response, new TypeToken<ErrorCode>() {
                                });
                            } else {
                                result = parseJsonValue(response, new TypeToken<FindPasswordSmsCode>() {
                                });
                            }
                            if (errorCode != null) {
                                if (getString(R.string.phone_registered).equals(errorCode.error.message)) {
                                    showDialog();
                                    return;
                                }
                                CommonUtil.shortCenterToast(mActivity, errorCode.error.message);
                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("user", userResult);
                                startActivityForResult(new Intent(CompletePhoneActivity.this, CompletePhoneConfActivity.class).
                                        putExtra("phoneNum", phoneNum).putExtra("verified_token", result.verified_token).putExtras(bundle), 0);
                            }
                        }
                    }, null);
                } else {
                    final Map<String, String> params = requestUrl.getParams();
                    params.put("mobile", phoneNum);
                    params.put("type", "sms_bind");
                    app.postUrl(requestUrl, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.contains("error")) {
                                ErrorCode errorCode = parseJsonValue(response, new TypeToken<ErrorCode>() {
                                });
                                if (errorCode != null) {
                                    if (getString(R.string.phone_registered).equals(errorCode.error.message)) {
                                        showDialog();
                                        return;
                                    }
                                    CommonUtil.shortCenterToast(mActivity, errorCode.error.message);
                                    return;
                                }
                            }
                            FindPasswordSmsCode result = parseJsonValue(response, new TypeToken<FindPasswordSmsCode>() {
                            });
                            if (result != null) {
                                verified = result.verified_token;
                                if ("limited".equals(result.status)) {
                                    bundle = new Bundle();
                                    byte[] byteArray = Base64.decode(result.img_code, Base64.DEFAULT);
                                    ivGraph.setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
                                    rl.setVisibility(View.VISIBLE);
                                    tvNext.setAlpha(0.6f);
                                    return;
                                }
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("user", userResult);
                                startActivityForResult(new Intent(CompletePhoneActivity.this, CompletePhoneConfActivity.class).
                                        putExtra("phoneNum", phoneNum).putExtra("verified_token", result.verified_token).putExtras(bundle), 0);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            CommonUtil.shortCenterToast(mContext, getResources().getString(R.string.request_fail_text));
                        }
                    });
                }
            } else {
                CommonUtil.shortCenterToast(mContext, getString(R.string.phone_error));
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1 && data != null) {
            bundle = data.getExtras();
            this.isBack = true;
        }
    }

    private void initCodeCoent() {
        if (bundle != null && isBack) {
            String img_code = bundle.getString("img_code");
            verified = bundle.getString("verified_token");
            rl.setVisibility(View.VISIBLE);
            etCode.setText("");
            byte[] byteArray = Base64.decode(img_code, Base64.DEFAULT);
            ivGraph.setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
        }
    }

    /**
     * 判断回退方式
     */
    private boolean isBack = false;

    @Override
    protected void onPause() {
        super.onPause();
        this.isBack = false;
    }

    private void showDialog() {
        new AlertDialog.Builder(CompletePhoneActivity.this)
                .setMessage(R.string.register_hint)
                .setPositiveButton(R.string.register_login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        CompletePhoneActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.register_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }
}

