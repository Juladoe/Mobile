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

public class CourseUtil {
    public static void reviewCourse(int courseId, int rating, String content
            , final OnReviewCourseListener onReviewCourseListener) {
        if (EdusohoApp.app.loginUser == null) {
            notLogin();
            return;
        }
        String url = String.format(Const.COURSE_COMMITCOURSE_NEW,
                courseId);
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(url, true);
        Map<String, String> params = new HashMap<>();
        params.put("rating", String.valueOf(rating));
        params.put("content", content);
        requestUrl.setParams(params);
        EdusohoApp.app.postUrl(requestUrl
                , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (onReviewCourseListener != null) {
                            onReviewCourseListener.onReviewCourseSuccess(response);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (onReviewCourseListener != null) {
                            onReviewCourseListener.onReviewCourseError(error.getMessage());
                        }
                    }
                });
    }

    public static void collectCourse(int courseId, final OnCollectSuccessListener onCollectSuccessListener) {
        if (EdusohoApp.app.loginUser == null) {
            notLogin();
            return;
        }
        RequestUrl requestUrl = EdusohoApp.app.bindUrl(Const.FAVORITE, true);
        requestUrl.setParams(new String[] {"courseId", String.valueOf(courseId)});
        EdusohoApp.app.postUrl(requestUrl
                , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response == null) {
                            if (EdusohoApp.app.loginUser == null) {
                                notLogin();
                                return;
                            }
                        } else if (response.equals("true")) {
                            if (onCollectSuccessListener != null) {
                                onCollectSuccessListener.onCollectSuccess();
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

    public static void uncollectCourse(int courseId, final OnCollectSuccessListener onCollectSuccessListener) {
        if (EdusohoApp.app.loginUser == null) {
            notLogin();
            return;
        }
        RequestUrl requestUrl = EdusohoApp.app.bindUrl(Const.UNFAVORITE, true);
        requestUrl.setParams(new String[] {"courseId", String.valueOf(courseId)});
        EdusohoApp.app.postUrl(requestUrl
                , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response == null) {
                            if (EdusohoApp.app.loginUser == null) {
                                notLogin();
                                return;
                            }
                        } else if (response.equals("true")) {
                            if (onCollectSuccessListener != null) {
                                onCollectSuccessListener.onCollectSuccess();
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

    public static class CourseParamsBuilder {
        Map<String, String> params = new HashMap<>();
        private String payment;
        private String payPassword;
        private String totalPrice;
        private String couponCode;
        private String targetType;
        private String targetId;

        public CourseParamsBuilder setPayment(String payment) {
            this.payment = payment;
            params.put("payment", payment);
            return this;
        }

        public CourseParamsBuilder setPayPassword(String payPassword) {
            this.payPassword = payPassword;
            params.put("payPassword", payPassword);
            return this;
        }

        public CourseParamsBuilder setTotalPrice(String totalPrice) {
            this.totalPrice = totalPrice;
            params.put("totalPrice", totalPrice);
            return this;
        }

        public CourseParamsBuilder setCouponCode(String couponCode) {
            this.couponCode = couponCode;
            params.put("couponCode", couponCode);
            return this;
        }

        public CourseParamsBuilder setTargetType(String targetType) {
            this.targetType = targetType;
            params.put("targetType", targetType);
            return this;
        }

        public CourseParamsBuilder setTargetId(String targetId) {
            this.targetId = targetId;
            params.put("targetId", targetId);
            return this;
        }

        public Map<String, String> build() {
            return params;
        }
    }

    public static void addCourse(final CourseParamsBuilder builder, final OnAddCourseListener
            onAddCourseListener) {
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
                                            , "course")
                            );
                            EdusohoApp.app.mEngine.runNormalPluginForResult("WebViewActivity"
                                    , EdusohoApp.app.mActivity, BaseStudyDetailActivity.RESULT_REFRESH
                                    , new PluginRunCallback() {
                                        @Override
                                        public void setIntentDate(Intent startIntent) {
                                            startIntent.putExtra(Const.WEB_URL, url);
                                        }
                                    });
                            return;
                        }
                        if (status.equals("ok")) {
                            if (onAddCourseListener != null) {
                                onAddCourseListener.onAddCourseSuccess(response);
                            }
                        } else {
                            if (onAddCourseListener != null) {
                                onAddCourseListener.onAddCourseError(status);
                            }
                        }
                    } catch (JSONException e) {
                        if (onAddCourseListener != null) {
                            onAddCourseListener.onAddCourseError("JsonError");
                        }
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (onAddCourseListener != null) {
                    onAddCourseListener.onAddCourseError("volleyError");
                }
            }
        });
    }

    public static void addCourseVip(int courseId, final OnAddCourseListener
            onAddCourseListener) {
        if (EdusohoApp.app.loginUser == null) {
            notLogin();
            return;
        }
        RequestUrl url = EdusohoApp.app.bindUrl(String.format(Const.VIP_ORDER_COURSE, courseId), true);
        EdusohoApp.app.getUrl(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (onAddCourseListener != null) {
                            onAddCourseListener.onAddCourseSuccess(response);
                        }
                    }
                }

                , new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (onAddCourseListener != null) {
                            onAddCourseListener.onAddCourseError("volleyError");
                        }
                    }
                }

        );
    }

    public static void deleteCourse(int courseId, final CallBack callBack) {
        RequestUrl url = EdusohoApp.app.bindUrl(
                String.format(Const.COURSE_UNLEARNCOURSE, courseId), true);
        EdusohoApp.app.getUrl(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (callBack != null) {
                            callBack.onSuccess(response);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (callBack != null) {
                            callBack.onError("volleyError");
                        }
                    }
                }
        );
    }

    public static void deleteClassroom(int classroomId, final CallBack callBack) {
        RequestUrl url = EdusohoApp.app.bindUrl(
                String.format(Const.CLASSROOM_UNLEARN + "?targetType=classroom&classRoomId=%s", classroomId), true);
        EdusohoApp.app.getUrl(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (callBack != null) {
                            callBack.onSuccess(response);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (callBack != null) {
                            callBack.onError("volleyError");
                        }
                    }
                }
        );
    }

    public interface CallBack {
        void onSuccess(String response);

        void onError(String response);
    }


    public interface OnCollectSuccessListener {
        void onCollectSuccess();
    }

    public interface OnAddCourseListener {
        void onAddCourseSuccess(String response);

        void onAddCourseError(String response);
    }

    public interface OnReviewCourseListener {
        void onReviewCourseSuccess(String response);

        void onReviewCourseError(String response);
    }

    public static void notLogin() {
        EdusohoApp.app.mEngine.runNormalPluginForResult("LoginActivity", EdusohoApp.app.mActivity
                , BaseStudyDetailActivity.RESULT_LOGIN, new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {

                    }
                });
    }
}
