package com.edusoho.kuozhi.homework;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.view.EduSohoButton;

import java.util.List;

/**
 * Created by Melomelon on 2015/10/19.
 */
public class HomeworkAnswerCardActivity extends ActionBarBaseActivity {

    private String mTitle = "答题卡";
    private LinearLayout mCardLayout;
    private EduSohoButton submitBtn;

    private HomeworkActivity mHomeworkActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homework_answer_card_layout);
        mHomeworkActivity = HomeworkActivity.getInstance();
        initView();
    }

    public void initView() {
        setBackMode(BACK, mTitle);
        submitBtn = (EduSohoButton) findViewById(R.id.homework_submit_btn);
        mCardLayout = (LinearLayout) findViewById(R.id.homework_answer_card_layout);

    }
}
