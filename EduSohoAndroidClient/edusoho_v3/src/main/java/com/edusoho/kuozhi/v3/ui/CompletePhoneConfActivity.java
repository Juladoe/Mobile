package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.edusoho.kuozhi.v3.util.OpenLoginUtil;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.edusoho.kuozhi.v3.util.encrypt.XXTEA;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DF on 2016/11/28.
 */
public class CompletePhoneConfActivity extends ActionBarBaseActivity {

    private int mClockTime;
    private TextView tvShow;
    private EditText etPwd;
    private EditText etAuth;
    private TextView tvSend;
    private Timer mTimer;
    private SmsCodeHandler mSmsCodeHandler;
    private String num;
    private ImageView ivShowPwd;
    private TextView tvConfirm;
    private String mCookie = "";
    private ImageView ivBack;
    private TextView tvInfo;
    private ImageView ivClearPwd;
    private ImageView ivClearAuth;
    private TextView tvTime;
    private String phone;
    private String verified_token;
    private UserResult userResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideActionBar();
        setContentView(R.layout.activity_complete_phone_conf);
        initView();
    }

    private void initView() {
        tvInfo = (TextView) findViewById(R.id.tv_info);
        tvShow = (TextView) findViewById(R.id.tv_show);
        tvSend = (TextView) findViewById(R.id.tv_send);
        etAuth = (EditText) findViewById(R.id.et_auth);
        etPwd = (EditText) findViewById(R.id.et_pwd);
        ivShowPwd = (ImageView) findViewById(R.id.iv_show_pwd);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivClearAuth = (ImageView) findViewById(R.id.iv_clear_auth);
        tvTime = (TextView) findViewById(R.id.tv_show_time);
        ivClearPwd = (ImageView) findViewById(R.id.iv_clear_pwd);
        tvInfo.setText("完善信息");
        tvSend.setOnClickListener(mSmsSendClickListener);
        ivShowPwd.setOnClickListener(nShowPwdClickListener);
        tvConfirm.setOnClickListener(mConfirmRegClickListener);
        ivBack.setOnClickListener(mBackClickListener);
        ivClearAuth.setOnClickListener(mClearContent);
        ivClearPwd.setOnClickListener(mClearContent);

        num = getIntent().getStringExtra("phoneNum");
        tvShow.setText(getString(R.string.phone_code_input_hint) + num);

        initTextChange();
        phone = getIntent().getStringExtra("phoneNum");
        verified_token = getIntent().getStringExtra("verified_token");
        userResult = (UserResult) getIntent().getExtras().getSerializable("user");
        InputUtils.showKeyBoard(etAuth, mContext);
        mSmsCodeHandler = new SmsCodeHandler(this);
        sendSms();
    }

    private void initTextChange() {
        InputUtils.addTextChangedListener(etAuth, new NormalCallback<Editable>() {
            @Override
            public void success(Editable editable) {
                if (etAuth.length() == 0) {
                    ivClearAuth.setVisibility(View.INVISIBLE);
                } else {
                    ivClearAuth.setVisibility(View.VISIBLE);
                }
                if (etAuth.length() == 0 || etPwd.length() == 0) {
                    tvConfirm.setAlpha(0.6f);
                } else {
                    tvConfirm.setAlpha(1.0f);
                }
            }
        });

        InputUtils.addTextChangedListener(etPwd, new NormalCallback<Editable>() {
            @Override
            public void success(Editable editable) {
                if (etPwd.length() == 0) {
                    ivClearPwd.setVisibility(View.INVISIBLE);
                } else {
                    ivClearPwd.setVisibility(View.VISIBLE);
                }
                if (etAuth.length() == 0 || etPwd.length() == 0) {
                    tvConfirm.setAlpha(0.6f);
                } else {
                    ivClearPwd.setVisibility(View.VISIBLE);
                    tvConfirm.setAlpha(1.0f);
                }
            }
        });
    }

    View.OnClickListener mBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CompletePhoneConfActivity.this.finish();
        }
    };

    View.OnClickListener mClearContent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.iv_clear_auth) {
                etAuth.setText("");
            } else if (id == R.id.iv_clear_pwd) {
                etPwd.setText("");
            }
        }
    };

    /**
     * 处理隐藏pwd
     */
    private boolean isShowPwd = true;

    View.OnClickListener nShowPwdClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isShowPwd) {
                etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivShowPwd.setImageResource(R.drawable.pwd_unshow);
            } else {
                etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivShowPwd.setImageResource(R.drawable.pwd_show);
            }
            isShowPwd = !isShowPwd;
            etPwd.setSelection(etPwd.getText().toString().length());
        }
    };

    /**
     * 绑定账号
     */
    View.OnClickListener mConfirmRegClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RequestUrl url = app.bindNewUrl(Const.CHANGE_PASSWORD, false);
            url.heads.put("Auth-Token", app.token);
            Map<String, String> params = url.getParams();
            params.put("type", "sms");
            params.put("mobile", phone);
            params.put("verified_token", verified_token);
            String strCode = etAuth.getText().toString().trim();
            if (TextUtils.isEmpty(strCode)) {
                CommonUtil.shortCenterToast(mContext, getString(R.string.reg_code_hint));
                return;
            } else {
                params.put("sms_code", strCode);
            }
            String strPass = etPwd.getText().toString();
            if (TextUtils.isEmpty(strPass)) {
                CommonUtil.shortCenterToast(mContext, getString(R.string.register_password_hint));
                return;
            }
            if (strPass.length() < 5 || strPass.length() > 20) {
                CommonUtil.shortCenterToast(mContext, getString(R.string.password_more_than_six_digit_number));
                return;
            }
            params.put("password", XXTEA.encryptToBase64String(strPass, app.domain));
            final LoadDialog loadDialog = LoadDialog.create(CompletePhoneConfActivity.this);
            loadDialog.show();
            app.postUrl(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    loadDialog.dismiss();
                    if (response.contains("error")) {
                        ErrorCode errorCode = mActivity.parseJsonValue(response, new TypeToken<ErrorCode>() {
                        });
                        if (errorCode != null) {
                            CommonUtil.shortCenterToast(CompletePhoneConfActivity.this, errorCode.error.message);
                        }
                    } else {
                        tvConfirm.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //绑定成功后直接进到网校
                                OpenLoginUtil openLoginUtil = OpenLoginUtil.getUtil(mActivity);
                                openLoginUtil.completeInfo(CompletePhoneConfActivity.this, userResult);
                                CommonUtil.shortCenterToast(CompletePhoneConfActivity.this, getString(R.string.complete_success));
                                app.mEngine.runNormalPlugin("DefaultPageActivity", mContext, null, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            }
                        }, 500);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadDialog.dismiss();
                    Log.d(TAG, "onErrorResponse: " + new String(error.networkResponse.data).toString());
                    CommonUtil.shortCenterToast(mContext, getResources().getString(R.string.request_fail_text));
                }
            });
        }
    };

    /**
     * 处理验证码
     */
    private void sendSms() {
        tvSend.setEnabled(false);
        mClockTime = 120;
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = mSmsCodeHandler.obtainMessage();
                message.what = 0;
                mSmsCodeHandler.sendMessage(message);
            }
        }, 0, 1000);
    }

    View.OnClickListener mSmsSendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RequestUrl requestUrl = app.bindNewUrl(Const.SEND_SMS, true);
            Map<String, String> params = requestUrl.getParams();
            params.put("mobile", num);
            params.put("type", "sms_bind");
            app.postUrl(requestUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    FindPasswordSmsCode result = parseJsonValue(response, new TypeToken<FindPasswordSmsCode>() {
                    });
                    if (response.contains("limited")) {
                        Bundle bundle = new Bundle();
                        bundle.putString("img_code", result.img_code);
                        bundle.putString("verified_token", result.verified_token);
                        setResult(1, new Intent().putExtras(bundle));
                        CompletePhoneConfActivity.this.finish();
                    } else {
                        if (result != null) {
                            verified_token = result.verified_token;
                            tvTime.setVisibility(View.VISIBLE);
                            tvSend.setEnabled(false);
                            mClockTime = 120;
                            mTimer = new Timer();
                            mTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Message message = mSmsCodeHandler.obtainMessage();
                                    message.what = 0;
                                    mSmsCodeHandler.sendMessage(message);
                                }
                            }, 0, 1000);
                        } else {
                            CommonUtil.shortCenterToast(mContext, response);
                        }
                    }
                }
            }, null);
        }
    };

    public static class SmsCodeHandler extends Handler {
        WeakReference<CompletePhoneConfActivity> mWeakReference;
        CompletePhoneConfActivity mActivity;

        public SmsCodeHandler(CompletePhoneConfActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            mActivity = mWeakReference.get();
            mActivity.tvSend.setText(mActivity.mClockTime + "s");
            mActivity.mClockTime--;
            if (mActivity.mClockTime < 0) {
                mActivity.mTimer.cancel();
                mActivity.mTimer = null;
                mActivity.tvSend.setText(mActivity.getResources().getString(R.string.reg_send_code));
                mActivity.tvSend.setEnabled(true);
                mActivity.tvTime.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }


}
