package com.edusoho.test.homework;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.test.UiThreadTest;
import android.view.View;
import android.widget.FrameLayout;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.homework.ExerciseActivity;
import com.edusoho.kuozhi.homework.model.ExerciseModel;
import com.edusoho.kuozhi.homework.model.ExerciseProvider;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import com.edusoho.kuozhi.v3.model.provider.ProviderListener;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.test.base.BaseActivityUnitTestCase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Melomelon on 2015/11/10.
 */
public class ExerciseActivityTest extends BaseActivityUnitTestCase<ExerciseActivity> {

    private ExerciseModel mExerciseModel;
    public ExerciseActivityTest() {
        super(ExerciseActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                ExerciseActivity.class);
    }

    @UiThreadTest
    public void testGetActivity() {
        ExerciseActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testViewLayout() {
        ExerciseActivity mActivity = getActivity();
        FrameLayout loading = (FrameLayout) mActivity.findViewById(com.edusoho.kuozhi.R.id.load_layout);
        assertNotNull(loading);
    }

    @UiThreadTest
    public void testData(){
        ExerciseActivity mActivity = getActivity();
        fillData(mActivity);

        while (mExerciseModel!=null){
            assertEquals(1, mExerciseModel.getId());
            assertEquals("2", mExerciseModel.getCourseId());
            assertEquals("Course title",mExerciseModel.getCourseTitle());
        }

    }

    private void fillData(ExerciseActivity activity){
        RequestUrl requestUrl = new RequestUrl();
        ExerciseProvider mExerciseProvider = new ExerciseProvider(activity);
        mExerciseProvider.getExercise(requestUrl);

        ExerciseModel exerciseModel = new ExerciseModel();

        mExerciseProvider.getExercise(requestUrl).success(new NormalCallback<ExerciseModel>() {
            @Override
            public void success(ExerciseModel exerciseModel) {
            }

        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                ExerciseModel exerciseModel = new ExerciseModel();
                exerciseModel.setId(1);
                exerciseModel.setCourseId("2");
                exerciseModel.setCourseTitle("Course title");
                exerciseModel.setCourseTitle("Description");
                exerciseModel.setItemCount("3");
                exerciseModel.setLessonId("1");
                exerciseModel.setLessonTitle("Lesson title");
                exerciseModel.setItems(new LinkedList<HomeWorkQuestion>());

                mExerciseModel = exerciseModel;
            }
        });
    }


}
