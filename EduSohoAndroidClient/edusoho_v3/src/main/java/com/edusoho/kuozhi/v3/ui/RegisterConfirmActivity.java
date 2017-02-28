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
import com.edusoho.kuozhi.v3.entity.register.MsgCode;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.result.UserResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.InputUtils;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.edusoho.kuozhi.v3.util.encrypt.XXTEA;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by DF on 2016/11/24.
 */

public class RegisterConfirmActivity extends ActionBarBaseActivity {

    private TextView tvShow;
    private EditText etPwd;
    private EditText etAuth;
    private TextView tvSend;

    private int mClockTime;
    private Timer mTimer;
    private SmsCodeHandler mSmsCodeHandler;
    private String num;
    private ImageView ivShowPwd;
    private TextView tvConfirm;
    private String mCookie = "";
    private ImageView ivBack;
    private ImageView ivClearAuth;
    private ImageView ivClearPwd;
    private TextView tvTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_confirm);
        initView();
    }

    private void initView() {
        tvShow = (TextView) findViewById(R.id.tv_show);
        tvSend = (TextView) findViewById(R.id.tv_send);
        tvSend.setOnClickListener(mSmsSendClickListener);
        etAuth = (EditText) findViewById(R.id.et_auth);
        etPwd = (EditText) findViewById(R.id.et_pwd);
        ivShowPwd = (ImageView) findViewById(R.id.iv_show_pwd);
        ivShowPwd.setOnClickListener(nShowPwdClickListener);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);
        tvConfirm.setOnClickListener(mConfirmRegClickListener);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(mBackClickListener);
        ivClearAuth = (ImageView) findViewById(R.id.iv_clear_auth);
        ivClearAuth.setOnClickListener(mClearContentListener);
        ivClearPwd = (ImageView) findViewById(R.id.iv_clear_pwd);
        ivClearPwd.setOnClickListener(mClearContentListener);
        tvTime = (TextView) findViewById(R.id.tv_show_time);

        num = getIntent().getStringExtra("num");
        tvShow.setText(getString(R.string.phone_code_input_hint) + num);

        initTextChange();

        InputUtils.showKeyBoard(etAuth, mContext);
        mSmsCodeHandler = new SmsCodeHandler(this);
        sendSms();
    }


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
            RegisterConfirmActivity.this.finish();
        }
    };

    View.OnClickListener mClearContentListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_clear_auth) {
                etAuth.setText("");
            } else {
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
            MobclickAgent.onEvent(mContext, "Set_the_password_for_the_eye_btb");
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
     * 注册账号
     */
    View.OnClickListener mConfirmRegClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (etAuth.length() == 0 || etPwd.length() == 0) {
                return;
            }
            RequestUrl url = app.bindUrl(Const.REGIST, false);
            Map<String, String> params = url.getParams();
            params.put("registeredWay", "android");

            params.put("phone", num);

            String strCode = etAuth.getText().toString().trim();
            if (TextUtils.isEmpty(strCode)) {
                CommonUtil.longToast(mContext, getString(R.string.reg_code_hint));
                return;
            } else {
                params.put("smsCode", strCode);
            }
            String strPass = etPwd.getText().toString().trim();
            if (TextUtils.isEmpty(strPass)) {
                CommonUtil.longToast(mContext, getString(R.string.register_password_hint));
                return;
            }
            if (strPass.length() < 5 || strPass.length() > 20) {
                CommonUtil.shortCenterToast(mContext, getString(R.string.password_more_than_six_digit_number));
                return;
            }
            if (SchoolUtil.checkEncryptVersion(app.schoolVersion, getString(R.string.encrypt_version))) {
                params.put("encrypt_password", XXTEA.encryptToBase64String(strPass, app.domain));
            } else {
                params.put("password", strPass);
            }
            Map<String, String> headers = url.getHeads();
            if (!TextUtils.isEmpty(mCookie)) {
                headers.put("Cookie", mCookie);
            }

            final LoadDialog loadDialog = LoadDialog.create(RegisterConfirmActivity.this);
            loadDialog.show();
            mActivity.ajaxPost(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        loadDialog.dismiss();
                        UserResult userResult = mActivity.parseJsonValue(
                                response, new TypeToken<UserResult>() {
                                });
                        if (userResult != null && userResult.user != null) {
                            app.saveToken(userResult);
                            app.sendMessage(Const.LOGIN_SUCCESS, null);
                            tvConfirm.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    app.mEngine.runNormalPlugin("DefaultPageActivity", mContext, null, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                }
                            }, 500);
                        } else {
                            if (!TextUtils.isEmpty(response)) {
                                CommonUtil.longToast(mContext, response);
                            }
                        }
                    } catch (Exception e) {
                        loadDialog.dismiss();
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadDialog.dismiss();
                    CommonUtil.longToast(mContext, getResources().getString(R.string.request_fail_text));
                }
            });
        }
    };

    View.OnClickListener mSmsSendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MobclickAgent.onEvent(mContext, "Retrieve_the_verification_code");
            RequestUrl requestUrl = app.bindUrl(Const.SMS_SEND, false);
            Map<String, String> params = requestUrl.getParams();
            params.put("phoneNumber", String.valueOf(num));
            mActivity.ajaxPost(requestUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        MsgCode result = parseJsonValue(response, new TypeToken<MsgCode>() {
                        });
                        if (result != null && result.code == 200) {
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
                            CommonUtil.longToast(mContext, result.msg);
                        } else {
                            CommonUtil.longToast(mContext, response);
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "phone reg error");
                    }
                }
            }, null);
        }
    };

    public static class SmsCodeHandler extends Handler {
        WeakReference<RegisterConfirmActivity> mWeakReference;
        RegisterConfirmActivity mActivity;

        public SmsCodeHandler(RegisterConfirmActivity activity) {
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
