package com.edusoho.kuozhi.homework;

import android.content.Intent;
import android.os.Bundle;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.homework.model.ExerciseModel;
import com.edusoho.kuozhi.homework.model.ExerciseResult;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.homework.ui.fragment.HomeWorkCardFragment;
import com.edusoho.kuozhi.homework.ui.fragment.HomeWorkParseCardFragment;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Melomelon on 2015/10/28.
 */
public class ExerciseParseActivity extends ExerciseActivity {
    public static final String EXERCISE_RESULT_ID = "exericseResultId";
    private int mExerciseResultId;

    @Override
    protected Intent initIntentData() {
        Intent intent = super.initIntentData();
        mExerciseId = intent.getIntExtra(EXERCISE_ID,0);
        mExerciseResultId = intent.getIntExtra(EXERCISE_RESULT_ID, 0);
        return intent;
    }

    @Override
    protected RequestUrl getRequestUrl() {
        return app.bindNewUrl(String.format(Const.EXERCISE_CONTENT_RESULT, mExerciseId), true);
    }

    @Override
    protected void showHomeWorkCard() {
        HomeWorkCardFragment cardFragment = new HomeWorkParseCardFragment();
        cardFragment.setTitle("答题卡");
        Bundle args = new Bundle();
        args.putString("type",mType);
        cardFragment.setArguments(args);
        cardFragment.show(mFragmentManager, "parseCardDialog");
    }
}
