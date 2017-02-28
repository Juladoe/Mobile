package com.edusoho.kuozhi.v3.util;

import android.content.Intent;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.BaseStudyDetailActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zhang on 2016/12/14.
 */

public class ClassroomUtil {

    public static void collectClassroom(String classroomId, final OnCollectSucceeListener onCollectSucceeListener) {
        if (EdusohoApp.app.loginUser == null) {
            notLogin();
            return;
        }
        EdusohoApp.app.getUrl(EdusohoApp.app.bindUrl(Const.FAVORITE + "?classroomId=" + classroomId, true)
                , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response == null) {
                            if (EdusohoApp.app.loginUser == null) {
                                notLogin();
                                return;
                            }
                        } else if (response.equals("true")) {
                            if (onCollectSucceeListener != null) {
                                onCollectSucceeListener.onCollectSuccee();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        CommonUtil.shortToast(EdusohoApp.app, "网络异常");
                    }
                });
    }

    public static void uncollectClassroom(String classroomId, final OnCollectSucceeListener onCollectSucceeListener) {
        if (EdusohoApp.app.loginUser == null) {
            notLogin();
            return;
        }
        EdusohoApp.app.getUrl(EdusohoApp.app.bindUrl(Const.UNFAVORITE + "?classroomId=" + classroomId, true)
                , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response == null) {
                            if (EdusohoApp.app.loginUser == null) {
                                notLogin();
                                return;
                            }
                        } else if (response.equals("true")) {
                            if (onCollectSucceeListener != null) {
                                onCollectSucceeListener.onCollectSuccee();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        CommonUtil.shortToast(EdusohoApp.app, "网络异常");
                    }
                });
    }

    public static class ClassroomParamsBuilder {
        Map<String, String> params = new HashMap<>();
        private String payment;
        private String payPassword;
        private String totalPrice;
        private String couponCode;
        private String targetType;
        private String targetId;

        public ClassroomParamsBuilder setPayment(String payment) {
            this.payment = payment;
            params.put("payment", payment);
            return this;
        }

        public ClassroomParamsBuilder setPayPassword(String payPassword) {
            this.payPassword = payPassword;
            params.put("payPassword", payPassword);
            return this;
        }

        public ClassroomParamsBuilder setTotalPrice(String totalPrice) {
            this.totalPrice = totalPrice;
            params.put("totalPrice", totalPrice);
            return this;
        }

        public ClassroomParamsBuilder setCouponCode(String couponCode) {
            this.couponCode = couponCode;
            params.put("couponCode", couponCode);
            return this;
        }

        public ClassroomParamsBuilder setTargetType(String targetType) {
            this.targetType = targetType;
            params.put("targetType", "classroom");
            return this;
        }

        public ClassroomParamsBuilder setTargetId(String targetId) {
            this.targetId = targetId;
            params.put("targetId", targetId);
            return this;
        }

        public Map<String, String> build() {
            return params;
        }
    }

    public static void addClassroom(final ClassroomParamsBuilder builder, final OnAddClassroomListener
            onAddclassroomListener) {
        if (EdusohoApp.app.loginUser == null) {
            notLogin();
            return;
        }
        RequestUrl url = EdusohoApp.app.bindUrl(Const.CREATE_ORDER, true);
        url.setParams(builder.build());
        EdusohoApp.app.postUrl(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response == null) {
                    notLogin();
                    return;
                }
                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String paid = jsonObject.getString("paid");
                        if (paid.equals("false")) {
                            final String url = String.format(
                                    Const.MOBILE_APP_URL,
                                    EdusohoApp.app.schoolHost,
                                    String.format("main#/coursepay/%s/%s",
                                            builder.targetId
                                            , "classroom")
                            );
                            EdusohoApp.app.mEngine.runNormalPluginForResult("WebViewActivity"
                                    , EdusohoApp.app.mActivity
                                    , BaseStudyDetailActivity.RESULT_REFRESH
                                    ,new PluginRunCallback() {
                                        @Override
                                        public void setIntentDate(Intent startIntent) {
                                            startIntent.putExtra(Const.WEB_URL, url);
                                        }
                                    });
                            return;
                        }
                        if (status.equals("ok")) {
                            if (onAddclassroomListener != null) {
                                onAddclassroomListener.onAddClassroomSuccee(response);
                            }
                        } else {
                            if (onAddclassroomListener != null) {
                                onAddclassroomListener.onAddClassroomError(status);
                            }
                        }
                    } catch (JSONException e) {
                        if (onAddclassroomListener != null) {
                            onAddclassroomListener.onAddClassroomError("JsonError");
                        }
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (onAddclassroomListener != null) {
                    onAddclassroomListener.onAddClassroomError("volleyError");
                }
            }
        });
    }

    public static void addClassroomVip(int classroomId, final OnAddClassroomListener
            onAddclassroomListener) {
        if (EdusohoApp.app.loginUser == null) {
            notLogin();
            return;
        }
        RequestUrl url = EdusohoApp.app.bindUrl(String.format(Const.VIP_ORDER_CLASSROOM, classroomId), true);
        EdusohoApp.app.getUrl(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (onAddclassroomListener != null) {
                            onAddclassroomListener.onAddClassroomSuccee(response);
                        }
                    }
                }

                , new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (onAddclassroomListener != null) {
                            onAddclassroomListener.onAddClassroomError("volleyError");
                        }
                    }
                }

        );
    }

    public interface OnCollectSucceeListener {
        void onCollectSuccee();
    }

    public interface OnAddClassroomListener {
        void onAddClassroomSuccee(String response);

        void onAddClassroomError(String response);
    }

    private static void notLogin() {

        EdusohoApp.app.mEngine.runNormalPluginForResult("LoginActivity", EdusohoApp.app.mActivity
                , BaseStudyDetailActivity.RESULT_LOGIN, new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {

                    }
                });
    }


}
