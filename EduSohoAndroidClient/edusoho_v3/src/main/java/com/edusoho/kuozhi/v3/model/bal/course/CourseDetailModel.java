package com.edusoho.kuozhi.v3.model.bal.course;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.entity.course.ClassroomDetail;
import com.edusoho.kuozhi.v3.entity.course.CourseDetail;
import com.edusoho.kuozhi.v3.entity.course.CourseProgress;
import com.edusoho.kuozhi.v3.entity.course.LearningClassroom;
import com.edusoho.kuozhi.v3.entity.course.LearningCourse;
import com.edusoho.kuozhi.v3.entity.course.LearningCourse2;
import com.edusoho.kuozhi.v3.entity.course.Study;
import com.edusoho.kuozhi.v3.entity.lesson.Lesson;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.Teacher;
import com.edusoho.kuozhi.v3.model.bal.http.ModelDecor;
import com.edusoho.kuozhi.v3.model.bal.lesson.LessonModel;
import com.edusoho.kuozhi.v3.model.base.ApiResponse;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.Const;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zhang on 2016/12/13.
 */

public class CourseDetailModel implements Serializable {

    public static void getCourseDetail(int courseId,
                                       final ResponseCallbackListener<CourseDetail> callbackListener) {
        String url = String.format(Const.COURSE_GETCOURSE, courseId);
        RequestUrl requestUrl = EdusohoApp.app.bindUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                CourseDetail apiResponse = ModelDecor.getInstance().
                        decor(response, new TypeToken<CourseDetail>() {
                        });
                if (apiResponse != null) {
                    callbackListener.onSuccess(apiResponse);
                } else {
                    callbackListener.onFailure("Error", response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getClassroomDetail(int classroomId,
                                          final ResponseCallbackListener<ClassroomDetail> callbackListener) {
        String url = String.format(Const.COURSE_GETCLASSROOM, classroomId);
        RequestUrl requestUrl = EdusohoApp.app.bindUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("班级不存在")) {
                    callbackListener.onFailure("Error", response);
                    return;
                }
                ClassroomDetail apiResponse = ModelDecor.getInstance().
                        decor(response, new TypeToken<ClassroomDetail>() {
                        });
                if (apiResponse != null) {
                    callbackListener.onSuccess(apiResponse);
                } else {
                    callbackListener.onFailure("Error", response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getCourseReviews(int courseId, String limit, String start,
                                        final ResponseCallbackListener<CourseReviewDetail> callbackListener) {
        String url = String.format(Const.COURSE_GETREVIEWS, courseId, limit, start);
        RequestUrl requestUrl = EdusohoApp.app.bindUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    CourseReviewDetail apiResponse = ModelDecor.getInstance().
                            decor(response, new TypeToken<CourseReviewDetail>() {
                            });
                    if (apiResponse != null) {
                        callbackListener.onSuccess(apiResponse);
                    } else  {
                        callbackListener.onFailure("Error", response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getClassroomReviews(int classroomId, String limit, String start,
                                           final ResponseCallbackListener<ClassroomReviewDetail> callbackListener) {
        String url = String.format(Const.CLASSROOM_GETREVIEWS, classroomId, limit, start);
        RequestUrl requestUrl = EdusohoApp.app.bindUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    ClassroomReviewDetail apiResponse = ModelDecor.getInstance().
                            decor(response, new TypeToken<ClassroomReviewDetail>() {
                            });
                    if (apiResponse != null) {
                        callbackListener.onSuccess(apiResponse);
                    } else  {
                        callbackListener.onFailure("Error", response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getCourseMember(int courseId,
                                       final ResponseCallbackListener<List<CourseMember>> callbackListener) {
        String url = String.format(Const.COURSE_GETMEMBER, courseId);
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    ApiResponse<CourseMember> apiResponse =
                            ModelDecor.getInstance().decor(response,
                                    new TypeToken<ApiResponse<CourseMember>>() {
                                    });
                    if (apiResponse != null) {
                        callbackListener.onSuccess(apiResponse.resources);
                    } else  {
                        callbackListener.onFailure("Error", response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getClassroomMember(int classroomId,
                                          final ResponseCallbackListener<List<ClassroomMember>> callbackListener) {
        String url = String.format(Const.CLASSROOM_GETMEMBER, classroomId);
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    ApiResponse<ClassroomMember> apiResponse =
                            ModelDecor.getInstance().decor(response,
                                    new TypeToken<ApiResponse<ClassroomMember>>() {
                                    });
                    if (apiResponse != null) {
                        callbackListener.onSuccess(apiResponse.resources);
                    } else  {
                        callbackListener.onFailure("Error", response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }


    public static void getAllUserCourses(int limit, int start,
                                         final ResponseCallbackListener<LearningCourse2> callbackListener) {
        String url = String.format(Const.LEARNING_NEW + "?limit=%s&start=%s&relation=learn", limit, start);
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LearningCourse2 apiResponse = ModelDecor.getInstance().
                        decor(response, new TypeToken<LearningCourse2>() {
                        });
                if (apiResponse != null) {
                    callbackListener.onSuccess(apiResponse);
                } else  {
                    callbackListener.onFailure("Error", response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getAllUserClassroom(int limit, int start,
                                           final ResponseCallbackListener<LearningClassroom> callbackListener) {
        String url = String.format(Const.CLASSROOM_MY + "?limit=%s&start=%s", limit, start);
        RequestUrl requestUrl = EdusohoApp.app.bindUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LearningClassroom apiResponse = ModelDecor.getInstance().
                        decor(response, new TypeToken<LearningClassroom>() {
                        });
                if (apiResponse != null) {
                    callbackListener.onSuccess(apiResponse);
                } else  {
                    callbackListener.onFailure("Error", response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getLiveCourses(int limit, int start,
                                      final ResponseCallbackListener<LearningCourse> callbackListener) {
        String url = String.format(Const.LIVING_COURSE + "?limit=%s&start=%s", limit, start);
        RequestUrl requestUrl = EdusohoApp.app.bindUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LearningCourse apiResponse = ModelDecor.getInstance().
                        decor(response, new TypeToken<LearningCourse>() {
                        });
                if (apiResponse != null) {
                    callbackListener.onSuccess(apiResponse);
                } else  {
                    callbackListener.onFailure("Error", response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getLiveCollect(int limit, int start,
                                      final ResponseCallbackListener<LearningCourse> callbackListener) {
        String url = String.format(Const.FAV_LIVE_COURSE + "?limit=%s&start=%s", limit, start);
        RequestUrl requestUrl = EdusohoApp.app.bindUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LearningCourse apiResponse = ModelDecor.getInstance().
                        decor(response, new TypeToken<LearningCourse>() {
                        });
                if (apiResponse != null) {
                    if (apiResponse.data != null) {
                        callbackListener.onSuccess(apiResponse);
                    } else if (apiResponse.error != null) {
                        callbackListener.onFailure(apiResponse.error.name, apiResponse.error.message);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getNormalCollect(int limit, int start,
                                        final ResponseCallbackListener<LearningCourse> callbackListener) {
        String url = String.format(Const.FAV_NOR_COURSE + "?limit=%s&start=%s", limit, start);
        RequestUrl requestUrl = EdusohoApp.app.bindUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LearningCourse apiResponse = ModelDecor.getInstance().
                        decor(response, new TypeToken<LearningCourse>() {
                        });
                if (apiResponse != null) {
                    if (apiResponse.data != null) {
                        callbackListener.onSuccess(apiResponse);
                    } else if (apiResponse.error != null) {
                        callbackListener.onFailure(apiResponse.error.name, apiResponse.error.message);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getCourseProgress(List<Integer> courseIds,
                                         final ResponseCallbackListener<CourseProgress> callbackListener) {
        StringBuilder sb = new StringBuilder();
        for (Integer id : courseIds) {
            sb.append(id + ",");
        }
        if (sb.length() == 0) {
            return;
        }
        String url = String.format(Const.COURSE_PROGRESS + "?courseIds=%s", sb.substring(0, sb.length() - 1));
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                CourseProgress apiResponse = ModelDecor.getInstance().
                        decor(response, new TypeToken<CourseProgress>() {
                        });
                if (apiResponse != null) {
                    callbackListener.onSuccess(apiResponse);
                } else {
                    callbackListener.onFailure("Error", response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getStudy(final ResponseCallbackListener<Study> callbackListener) {
        String url = String.format(Const.MY_LEARNING);
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Study apiResponse = ModelDecor.getInstance().
                        decor(response, new TypeToken<Study>() {
                        });
                if (apiResponse != null) {
                    callbackListener.onSuccess(apiResponse);
                } else {
                    callbackListener.onFailure("Error", response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void getLiveLesson(final int courseId, final NormalCallback<List<Lesson>> callback) {
        String[] conditions = new String[]{
                "status", "published",
                "courseId", String.valueOf(courseId)
        };
        LessonModel.getLessonByCourseId(conditions, new ResponseCallbackListener<List<Lesson>>() {
            @Override
            public void onSuccess(List<Lesson> data) {
                callback.success(data);
            }

            @Override
            public void onFailure(String code, String message) {
                callback.success(null);
            }
        });
    }

    public static void getTeacher(int id, final ResponseCallbackListener<Teacher[]> callbackListener) {
        String url = String.format(Const.TEACHER_INFO, id);
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(url, false);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Teacher[] apiResponse = ModelDecor.getInstance().
                        decor(response, new TypeToken<Teacher[]>() {});
                if (apiResponse != null) {
                    callbackListener.onSuccess(apiResponse);
                } else  {
                    callbackListener.onFailure("Error", response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }

    public static void sendTime(int id, int watchTime, final ResponseCallbackListener<String> callbackListener) {
        RequestUrl requestUrl = EdusohoApp.app.bindNewUrl(Const.SEND_PLAY_TIME, true);
        Map<String, String> params = new HashMap<>();
        params.put("lessonId", String.valueOf(id));
        params.put("watchTime", String.valueOf(watchTime));
        requestUrl.setParams(params);
        EdusohoApp.app.postUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (callbackListener != null) {
                    callbackListener.onSuccess(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }
}
