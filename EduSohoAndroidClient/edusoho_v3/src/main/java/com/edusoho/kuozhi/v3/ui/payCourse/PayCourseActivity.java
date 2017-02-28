package com.edusoho.kuozhi.v3.ui.payCourse;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.CourseCode;
import com.edusoho.kuozhi.v3.model.PayStatus;
import com.edusoho.kuozhi.v3.model.bal.Member;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.FragmentPageActivity;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.google.gson.reflect.TypeToken;

/**
 * Created by howzhi on 14-9-4.
 * <p/>
 * 需要传递传递参数：
 * "price"
 * "title"
 * "payurl"
 * "courseId"
 */
public class PayCourseActivity extends ActionBarBaseActivity
        implements MessageEngine.MessageCallback {

    private TextView mPriceView;
    private TextView mTitleView;
    private TextView mCardNumber;
    private TextView mCardEndPrice;
    private View mCodeCheckBtn;
    private View mPayBtn;
    private EditText mCodeView;

    private String mTitle;
    private int mCourseId;
    private double mPrice;

    public static final int PAY_SUCCESS = 001;
    public static final int PAY_EXIT = 002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_course);
        initView();
    }

    @Override
    public void invoke(WidgetMessage message) {
        int type = message.type.code;
        switch (type) {
            case PAY_SUCCESS:
                Log.d(null, "pay->success");
                CommonUtil.longToast(mActivity, "支付完成");
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                setResult(5);
                /**原文件中 CourseDetailsActivity.PAY_COURSE_SUCCESS 为 0005**/
//                setResult(CourseDetailsActivity.PAY_COURSE_SUCCESS);
                finish();
                break;
            case PAY_EXIT:
                Log.d(null, "pay->exit");
                checkPayResult();
                break;
        }
    }

    /**
     * 更新课程会员信息
     */
    private void checkPayResult() {
        RequestUrl url = app.bindUrl(Const.COURSE_MEMBER, true);
        url.setParams(new String[]{
                "courseId", String.valueOf(mCourseId)
        });

        final LoadDialog loadDialog = LoadDialog.create(mActivity);
        loadDialog.show();
        ajaxPost(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadDialog.dismiss();
                Member member = parseJsonValue(
                        response, new TypeToken<Member>() {
                        }
                );
                if (member == null) {
                    CommonUtil.longToast(mActivity, "支付课程失败!");
                    return;
                }
                CommonUtil.longToast(mActivity, "支付完成");
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                setResult(5);
                /**原文件中 CourseDetailsActivity.PAY_COURSE_SUCCESS 为 0005**/
//                setResult(CourseDetailsActivity.PAY_COURSE_SUCCESS);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

    }

    @Override
    public MessageType[] getMsgTypes() {
        String source = this.getClass().getSimpleName();
        MessageType[] messageTypes = new MessageType[]{
                new MessageType(PAY_SUCCESS, source),
                new MessageType(PAY_EXIT, source)
        };
        return messageTypes;
    }

    private void initView() {
        Intent data = getIntent();
        mTitle = data.getStringExtra("title");
        mCourseId = data.getIntExtra("courseId", 0);
        mPrice = data.getDoubleExtra("price", 0.0);

        setBackMode(BACK, "购买课程");

        mPayBtn = findViewById(R.id.pay_course_pay_btn);
        mTitleView = (TextView) findViewById(R.id.pay_course_title);
        mPriceView = (TextView) findViewById(R.id.pay_course_price);
        mCardNumber = (TextView) findViewById(R.id.pay_course_card_number);
        mCodeCheckBtn = findViewById(R.id.pay_course_checkcard_btn);
        mCodeView = (EditText) findViewById(R.id.pay_course_card_edt);
        mCardEndPrice = (TextView) findViewById(R.id.pay_course_card_end_price);

        setViewData();

        mCodeCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = mCodeView.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    CommonUtil.longToast(mActivity, "请输入优惠码!");
                    return;
                }
                checkCode(code);
            }
        });

        mPayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestUrl url = app.bindUrl(Const.PAYCOURSE, true);
                url.setParams(new String[]{
                        "payment", "alipay",
                        "courseId", mCourseId + ""
                });
                ajaxPostWithLoading(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        final PayStatus payStatus = parseJsonValue(
                                response, new TypeToken<PayStatus>() {
                                });

                        if (payStatus == null) {
                            CommonUtil.longToast(mActivity, "购买课程失败!");
                            return;
                        }
                        app.mEngine.runNormalPlugin("FragmentPageActivity", mActivity, new PluginRunCallback() {
                            @Override
                            public void setIntentDate(Intent startIntent) {
                                startIntent.putExtra(FragmentPageActivity.FRAGMENT, "AlipayFragment");
                                startIntent.putExtra(Const.ACTIONBAR_TITLE, "支付课程-" + mTitle);
                                startIntent.putExtra("payurl", payStatus.payUrl);
                            }
                        });
                    }
                }, null, "");

            }
        });
    }

    private void checkCode(String code) {
        RequestUrl requestUrl = app.bindUrl(Const.COURSE_CODE, false);
        requestUrl.setParams(new String[]{
                Const.COURSE_ID, mCourseId + "",
                "type", "course",
                "code", code
        });
        setProgressBarIndeterminateVisibility(true);
        ajaxPost(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                setProgressBarIndeterminateVisibility(false);
                CourseCode result = parseJsonValue(
                        response, new TypeToken<CourseCode>() {
                        });
                if (result != null) {
                    if (result.useable == CourseCode.Code.yes) {
                        setColorText(
                                mCardNumber,
                                "优惠:",
                                result.decreaseAmount + "元",
                                getResources().getColor(R.color.pay_course_end_price)
                        );
                        double newPrice = mPrice - result.decreaseAmount > 0
                                ? mPrice - result.decreaseAmount : 0;
                        mPrice = newPrice;
                        setColorText(
                                mCardEndPrice,
                                "优惠后价格:",
                                newPrice + "元",
                                getResources().getColor(R.color.pay_course_end_price)
                        );
                        mCodeCheckBtn.setEnabled(false);
                        CommonUtil.longToast(mActivity, "优惠:" + result.decreaseAmount);
                    } else {
                        CommonUtil.longToast(mActivity, result.message);
                    }
                } else {
                    CommonUtil.longToast(mActivity, "验证错误");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private void setViewData() {
        setColorText(mPriceView, "价格:", mPrice + "元", getResources().getColor(R.color.pay_course_old_price));
        setColorText(mCardEndPrice, "优惠后价格:", mPrice + "元", getResources().getColor(R.color.pay_course_end_price));
        setColorText(mCardNumber, "优惠:", "0.00元", getResources().getColor(R.color.pay_course_end_price));
        mTitleView.setText("课程名称：  " + mTitle);
    }

    private void setColorText(TextView view, String base, String text, int color) {
        StringBuilder oldText = new StringBuilder(base);
        int start = oldText.length();
        oldText.append(text);
        Spannable spannable = new SpannableString(oldText);
        spannable.setSpan(
                new ForegroundColorSpan(color), start, oldText.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        view.setText(spannable);
    }
}
