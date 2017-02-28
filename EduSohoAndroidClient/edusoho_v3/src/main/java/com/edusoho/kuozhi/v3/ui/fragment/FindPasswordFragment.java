package com.edusoho.kuozhi.v3.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.ui.util.ImageUtil;
import com.edusoho.kuozhi.v3.entity.error.Error;
import com.edusoho.kuozhi.v3.entity.register.FindPasswordSmsCode;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.http.ModelDecor;
import com.edusoho.kuozhi.v3.model.base.ApiResponse;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.ForgetPasswordActivity;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.InputUtils;
import com.edusoho.kuozhi.v3.util.ToastUtil;
import com.edusoho.kuozhi.v3.util.Validator;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by JesseHuang on 2016/11/25.
 */

public class FindPasswordFragment extends BaseFragment {

    public static final String SMS_TOKEN = "sms_token";
    private TextView tvNext;
    private EditText etPhoneOrMail;
    private EditText etImgCode;
    private ImageView ivPhoneOrEmailErase;
    private ImageView ivImgCodeErase;
    private ImageView ivImgCode;
    private TextView tvImgCodeChange;
    private RelativeLayout rlImgCode;
    private Bitmap mImgCodeBitmap;
    private String mCurrentVerifiedToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.fragment_find_password);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        tvNext = (TextView) view.findViewById(R.id.tv_next);
        etPhoneOrMail = (EditText) view.findViewById(R.id.et_phone_or_mail);
        etImgCode = (EditText) view.findViewById(R.id.et_img_code);
        ivPhoneOrEmailErase = (ImageView) view.findViewById(R.id.iv_phone_or_mail_erase);
        ivImgCodeErase = (ImageView) view.findViewById(R.id.iv_img_code_erase);
        ivImgCode = (ImageView) view.findViewById(R.id.iv_img_code);
        tvImgCodeChange = (TextView) view.findViewById(R.id.tv_change_img_code);
        rlImgCode = (RelativeLayout) view.findViewById(R.id.rl_img_code);
        tvNext.setOnClickListener(getNextClickListener());
        ivPhoneOrEmailErase.setOnClickListener(getEraseInfoClickListener());
        ivImgCodeErase.setOnClickListener(getEraseInfoClickListener());
        tvImgCodeChange.setOnClickListener(getChangeSmsCodeClickListener());

        etPhoneOrMail.requestFocus();
        InputUtils.showKeyBoard(etPhoneOrMail, mContext);
        InputUtils.showKeyBoard(etImgCode, mContext);
        InputUtils.addTextChangedListener(etPhoneOrMail, new NormalCallback<Editable>() {
            @Override
            public void success(Editable editable) {
                if (editable.length() == 0) {
                    ivPhoneOrEmailErase.setVisibility(View.INVISIBLE);
                } else {
                    ivPhoneOrEmailErase.setVisibility(View.VISIBLE);
                }
                if (editable.length() == 0 || (rlImgCode.getVisibility() == View.VISIBLE && etImgCode.length() == 0)) {
                    tvNext.setAlpha(0.6f);
                } else {
                    tvNext.setAlpha(1.0f);
                }
            }
        });

        InputUtils.addTextChangedListener(etImgCode, new NormalCallback<Editable>() {
            @Override
            public void success(Editable editable) {
                if (editable.length() == 0) {
                    ivImgCodeErase.setVisibility(View.INVISIBLE);
                } else {
                    ivImgCodeErase.setVisibility(View.VISIBLE);
                }
                if (editable.length() == 0 || etPhoneOrMail.length() == 0) {
                    tvNext.setAlpha(0.6f);
                } else {
                    tvNext.setAlpha(1.0f);
                }
            }
        });
    }

    private View.OnClickListener getNextClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etPhoneOrMail.length() == 0) {
                    return;
                }
                if ("".equals(etPhoneOrMail.getText().toString().trim())) {
                    Toast.makeText(mContext, getString(R.string.find_password_text_not_null), Toast.LENGTH_LONG).show();
                    return;
                }
                if (rlImgCode.getVisibility() == View.VISIBLE && etImgCode.length() == 0) {
                    Toast.makeText(mContext, getString(R.string.img_code_cannot_null), Toast.LENGTH_LONG).show();
                    return;
                }
                if (getActivity() != null && getActivity() instanceof ForgetPasswordActivity) {
                    final ForgetPasswordActivity forgetPasswordActivity = (ForgetPasswordActivity) getActivity();
                    final Bundle bundle = new Bundle();
                    if (Validator.isMail(etPhoneOrMail.getText().toString().trim())) {
                        bundle.putString(ForgetPasswordActivity.RESET_INFO, getResetInfo());
                        forgetPasswordActivity.switchFragment("FindPasswordByMailFragment", bundle);
                    } else if (Validator.isPhone(etPhoneOrMail.getText().toString().trim())) {
                        if (rlImgCode.getVisibility() != View.VISIBLE) {
                            sendSmsToPhone(new NormalCallback<String>() {
                                @Override
                                public void success(String response) {
                                    if (response != null) {
                                        FindPasswordSmsCode smsCode = ModelDecor.getInstance().decor(response, new TypeToken<FindPasswordSmsCode>() {
                                        });
                                        if (smsCode != null && smsCode.status != null) {
                                            if ("ok".equals(smsCode.status)) {
                                                ToastUtils.show(mContext, getString(R.string.sms_code_success), Toast.LENGTH_LONG);
                                                bundle.putString(ForgetPasswordActivity.RESET_INFO, getResetInfo());
                                                bundle.putString(SMS_TOKEN, smsCode.verified_token);
                                                forgetPasswordActivity.switchFragment("FindPasswordByPhoneFragment", bundle);
                                            } else if ("limited".equals(smsCode.status)) {
                                                //图形验证码
                                                mCurrentVerifiedToken = smsCode.verified_token;
                                                rlImgCode.setVisibility(View.VISIBLE);
                                                mImgCodeBitmap = ImageUtil.decodeBase64(smsCode.img_code);
                                                ivImgCode.setImageBitmap(mImgCodeBitmap);
                                                etImgCode.requestFocus();
                                                tvNext.setAlpha(0.6f);
                                            }
                                        } else {
                                            ApiResponse<Error> error = ModelDecor.getInstance().decor(response, new TypeToken<ApiResponse<Error>>() {
                                            });
                                            if (error.error != null) {
                                                ToastUtils.show(mContext, error.error.message, Toast.LENGTH_LONG);
                                            }
                                        }
                                    }
                                }
                            });
                        } else {
                            sendSmsToPhoneViaImgCode(new NormalCallback<String>() {
                                @Override
                                public void success(String response) {
                                    ApiResponse<Error> error = ModelDecor.getInstance().decor(response, new TypeToken<ApiResponse<Error>>() {
                                    });
                                    if (error.error != null) {
                                        ToastUtils.show(mContext, error.error.message, Toast.LENGTH_LONG);
                                        return;
                                    }
                                    FindPasswordSmsCode smsCode = ModelDecor.getInstance().decor(response, new TypeToken<FindPasswordSmsCode>() {
                                    });
                                    Log.d("smsCode", "success: " + smsCode.status);
                                    if ("ok".equals(smsCode.status)) {
                                        ToastUtils.show(mContext, getString(R.string.sms_code_success), Toast.LENGTH_LONG);
                                        bundle.putString(ForgetPasswordActivity.RESET_INFO, getResetInfo());
                                        bundle.putString(SMS_TOKEN, smsCode.verified_token);
                                        forgetPasswordActivity.switchFragment("FindPasswordByPhoneFragment", bundle);
                                    }
                                }
                            });
                        }
                    } else {
                        Toast.makeText(mContext, getString(R.string.phone_or_mail_format_error), Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }

    private View.OnClickListener getChangeSmsCodeClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSmsToPhone(new NormalCallback<String>() {
                    @Override
                    public void success(String response) {
                        FindPasswordSmsCode smsCode = ModelDecor.getInstance().decor(response, new TypeToken<FindPasswordSmsCode>() {
                        });
                        if (smsCode != null && "limited".equals(smsCode.status)) {
                            clearCurrentBitmap();
                            mCurrentVerifiedToken = smsCode.verified_token;
                            mImgCodeBitmap = ImageUtil.decodeBase64(smsCode.img_code);
                            ivImgCode.setImageBitmap(ImageUtil.decodeBase64(smsCode.img_code));
                        }
                    }
                });
            }
        };
    }

    private View.OnClickListener getEraseInfoClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.iv_phone_or_mail_erase) {
                    etPhoneOrMail.setText("");
                } else if (v.getId() == R.id.iv_img_code_erase) {
                    etImgCode.setText("");
                }
            }
        };
    }

    private void sendSmsToPhone(final NormalCallback<String> callback) {
        RequestUrl requestUrl = app.bindNewUrl(Const.SMS_CODES, false);
        Map<String, String> params = requestUrl.getParams();
        params.put("type", "sms_change_password");
        params.put("mobile", etPhoneOrMail.getText().toString().trim());
        app.postUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.success(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String as = new String(error.networkResponse.data);
                callback.success(as);
            }
        });
    }

    private void sendSmsToPhoneViaImgCode(final NormalCallback<String> callback) {
        RequestUrl requestUrl = app.bindNewUrl(Const.SMS_CODES, false);
        Map<String, String> params = requestUrl.getParams();
        params.put("type", "sms_change_password");
        params.put("verified_token", mCurrentVerifiedToken);
        params.put("mobile", etPhoneOrMail.getText().toString().trim());
        params.put("img_code", etImgCode.getText().toString());
        app.postUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.success(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String as = new String(error.networkResponse.data);
                callback.success(as);
            }
        });
    }

    @Override
    public void invoke(WidgetMessage message) {
        if (message.type.type.equals(FindPasswordByPhoneFragment.RESEND_IMG_CODE)) {
            FindPasswordSmsCode smsCode = (FindPasswordSmsCode) message.data.getSerializable(FindPasswordByPhoneFragment.SMS_CODES_OBJECT);
            mCurrentVerifiedToken = smsCode.verified_token;
            clearCurrentBitmap();
            mImgCodeBitmap = ImageUtil.decodeBase64(smsCode.img_code);
            ivImgCode.setImageBitmap(ImageUtil.decodeBase64(smsCode.img_code));
            etImgCode.setText("");
            rlImgCode.setVisibility(View.VISIBLE);
            Log.d("FindPasswordSmsCode", "invoke: FindPasswordSmsCode");
        }
    }

    @Override
    public MessageType[] getMsgTypes() {
        return new MessageType[]{new MessageType(FindPasswordByPhoneFragment.RESEND_IMG_CODE)};
    }

    public String getResetInfo() {
        return etPhoneOrMail.getText().toString().trim();
    }

    private void clearCurrentBitmap() {
        if (mImgCodeBitmap != null) {
            mImgCodeBitmap.recycle();
            mImgCodeBitmap = null;
        }
    }
}
