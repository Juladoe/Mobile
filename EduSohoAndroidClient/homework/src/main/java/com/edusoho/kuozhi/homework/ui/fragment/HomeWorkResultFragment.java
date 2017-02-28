package com.edusoho.kuozhi.homework.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.homework.HomeworkActivity;
import com.edusoho.kuozhi.homework.HomeworkSummaryActivity;
import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.adapter.HomeWorkResultListAdapter;
import com.edusoho.kuozhi.homework.model.HomeWorkItemResult;
import com.edusoho.kuozhi.homework.model.HomeWorkResult;
import com.edusoho.kuozhi.homework.model.HomeworkProvider;
import com.edusoho.kuozhi.homework.HomeWorkParseActivity;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.Const;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by howzhi on 15/10/20.
 */
public class HomeWorkResultFragment extends BaseFragment implements View.OnClickListener {

    private ListView mResultListView;
    private TextView mResultView;
    private View mResultParseBtn;
    private View mResultReDoBtn;
    private View mResultBtnLayout;
    private int mLessonId;
    private Bundle mBundle;
    private HomeWorkResult mHomeWorkResult;
    private FrameLayout mLoading;

    private HomeworkProvider mHomeworkProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.homework_result_layout);
        ModelProvider.init(getActivity().getBaseContext(), this);

        mBundle = getArguments();
        mLessonId = mBundle == null ? 0 : mBundle.getInt(Const.LESSON_ID);
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        mResultBtnLayout = view.findViewById(R.id.hw_result_btn_layout);
        mResultListView = (ListView) view.findViewById(R.id.hw_result_listview);
        mResultView = (TextView) view.findViewById(R.id.hw_result_total);
        mResultParseBtn = view.findViewById(R.id.hw_result_parse);
        mResultReDoBtn = view.findViewById(R.id.hw_result_redo);
        mLoading = (FrameLayout) view.findViewById(R.id.load_layout );

        loadHomeWorkResult();
    }

    private void loadHomeWorkResult() {
        String url = String.format(Const.HOMEWORK_RESULT, mLessonId);
        RequestUrl requestUrl = app.bindNewUrl(url, true);
        mLoading.setVisibility(View.VISIBLE);
        mHomeworkProvider.getHomeWorkResult(requestUrl, true).success(new NormalCallback<HomeWorkResult>() {
            @Override
            public void success(HomeWorkResult homeWorkResult) {
                mLoading.setVisibility(View.GONE);
                mHomeWorkResult = homeWorkResult;
                renderView(homeWorkResult);
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                mLoading.setVisibility(View.GONE);
            }
        });
    }

    private LinkedHashMap<QuestionType, Integer> coverResultItemList(List<HomeWorkItemResult> items) {

        LinkedHashMap<QuestionType, Integer> itemResultMap = new LinkedHashMap<>();
        for (HomeWorkItemResult itemResult : items) {
            QuestionType questionType = QuestionType.value(itemResult.questionType);
            if (questionType == QuestionType.empty) {
                continue;
            }
            if (! itemResultMap.containsKey(questionType)) {
                itemResultMap.put(questionType, 0);
            }

            int result = itemResultMap.get(questionType);
            int right = result >> 4;
            int total = result & 0x0f;

            total ++;
            if ("right".equals(itemResult.status)) {
                right ++;
            }

            result = (right << 4) | total;
            itemResultMap.put(questionType, result);
        }
        return itemResultMap;
    }

    private void renderView(HomeWorkResult homeWorkResult) {

        HomeWorkResultListAdapter resultListAdapter = new HomeWorkResultListAdapter(
                mContext, coverResultItemList(homeWorkResult.items));
        mResultListView.setAdapter(resultListAdapter);

        if ("reviewing".equals(homeWorkResult.status)) {
            mResultBtnLayout.setVisibility(View.INVISIBLE);
            mResultView.setText("已提交，等待老师批阅");
            return;
        }

        if ("finished".equals(homeWorkResult.status)) {
            mResultParseBtn.setOnClickListener(this);
            mResultReDoBtn.setOnClickListener(this);
            mResultView.setText(parsePassedStatus(homeWorkResult.passedStatus));
        }
    }

    private String parsePassedStatus(String passedStatus) {
        if (TextUtils.isEmpty(passedStatus)) {
            return "合格";
        }
        switch (passedStatus) {
            case "passed":
                return "合格";
            case "unpassed":
                return "不合格";
            case "good":
                return "良好";
            case "excellent":
                return "优秀";
        }

        return "合格";
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.hw_result_redo) {
            reDoHomeWork();
            return;
        } else if (v.getId() == R.id.hw_result_parse) {
            showHomeWorkParse();
        }
    }

    private void showHomeWorkParse() {
        Intent intent = new Intent(getActivity().getBaseContext(), HomeWorkParseActivity.class);
        intent.putExtra(HomeWorkParseActivity.HOMEWORK_RESULTID, mHomeWorkResult.id);
        intent.putExtra(HomeworkSummaryActivity.TYPE, mBundle.getString("type"));
        startActivity(intent);
    }

    private void reDoHomeWork() {
        Intent intent = new Intent(getActivity().getBaseContext(), HomeworkActivity.class);
        intent.putExtra(HomeworkActivity.HOMEWORK_ID, mHomeWorkResult.homeworkId);
        intent.putExtra(HomeworkSummaryActivity.TYPE, mBundle.getString("type"));
        getActivity().startActivityForResult(intent, HomeworkSummaryActivity.REQUEST_DO);
    }
}
