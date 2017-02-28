package com.edusoho.kuozhi.homework.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.homework.ExerciseActivity;
import com.edusoho.kuozhi.homework.HomeworkActivity;
import com.edusoho.kuozhi.homework.HomeworkSummaryActivity;
import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.adapter.HomeworkCardAdapter;
import com.edusoho.kuozhi.homework.listener.IHomeworkQuestionResult;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.homework.model.HomeworkProvider;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import java.util.List;

/**
 * Created by howzhi on 15/10/20.
 */
public class HomeWorkCardFragment extends DialogFragment implements
        View.OnClickListener, AdapterView.OnItemClickListener {

    protected View mBackView;
    protected View mSubmitBtn;
    private TextView mHeadTitleView;
    private GridView mCardItemGridView;
    private String mTitle;
    private HomeworkProvider mHomeworkProvider;

    private String mType;

    private IHomeworkQuestionResult mIHomeworkQuestionResult;

    public void setTitle(String title) {
        this.mTitle = title;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.HomeworkCardDialogTheme);
        ModelProvider.init(getActivity().getBaseContext(), this);
        mType = getArguments().getString("type");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mIHomeworkQuestionResult = (IHomeworkQuestionResult) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homework_card_layout, container, false);
        initView(view);
        return view;
    }

    protected void initView(View view) {

        mBackView = view.findViewById(R.id.home);
        mSubmitBtn = view.findViewById(R.id.hw_card_submit);
        mHeadTitleView = (TextView) view.findViewById(R.id.homework_title);
        mCardItemGridView = (GridView) view.findViewById(R.id.hw_card_gridview);

        mHeadTitleView.setText(mTitle);
        mSubmitBtn.setOnClickListener(this);
        mBackView.setOnClickListener(this);

        List<HomeWorkQuestion> questionList = mIHomeworkQuestionResult.getQuestionList();
        mCardItemGridView.setAdapter(getCardAdapter(questionList));
        mCardItemGridView.setOnItemClickListener(this);
    }

    protected BaseAdapter getCardAdapter(List<HomeWorkQuestion> questionList) {
        return new HomeworkCardAdapter(
                getActivity().getBaseContext(), questionList, R.layout.hw_card_item
        );
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.home) {
            dismiss();
        } else if (v.getId() == R.id.hw_card_submit) {
            if (HomeworkSummaryActivity.HOMEWORK.equals(mType)){
                MessageEngine.getInstance().sendMsgToTaget(
                    HomeworkActivity.SUBMIT_HOMEWORK, null, HomeworkActivity.class);
            }else {
                MessageEngine.getInstance().sendMsgToTaget(
                        ExerciseActivity.SUBMIT_EXERCISE, null, ExerciseActivity.class);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mIHomeworkQuestionResult.setCurrentQuestionIndex(position);
        dismiss();
    }
}
