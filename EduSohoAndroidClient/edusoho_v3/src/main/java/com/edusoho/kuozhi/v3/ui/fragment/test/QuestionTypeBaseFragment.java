package com.edusoho.kuozhi.v3.ui.fragment.test;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionTypeSeq;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.ui.test.TestpaperBaseActivity;
import com.edusoho.kuozhi.v3.util.Const;

import java.util.ArrayList;

/**
 * Created by howzhi on 14-9-24.
 */
public abstract class QuestionTypeBaseFragment extends BaseFragment {

    protected TextView mQuestionType;
    protected TextView mQuestionScore;
    protected TextView mQuestionNumber;
    protected TestpaperBaseActivity mTestpaperActivity;

    protected int mCurrentIndex;

    @Override
    public String getTitle() {
        return "";
    }

    /**
     *
     * @param title
     * @param seqs
     */
    protected void setQuestionTitle(
            String title, ArrayList<QuestionTypeSeq> seqs)
    {
        mQuestionType.setText(title);
        mQuestionScore.setText(
                String.format("共%.1f分",
                        getQuestionScore(seqs)
                )
        );
    }

    private double getQuestionScore(ArrayList<QuestionTypeSeq> questionTypeSeqs)
    {
        double total = 0;
        for (QuestionTypeSeq seq: questionTypeSeqs) {
            total += seq.score;
        }

        return total;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTestpaperActivity = (TestpaperBaseActivity) activity;
    }

    @Override
    public MessageType[] getMsgTypes() {
        return new MessageType[] {
                new MessageType(Const.TESTPAPER_REFRESH_DATA)
        };
    }

    @Override
    public void invoke(WidgetMessage message) {
        MessageType messageType = message.type;
        if (Const.TESTPAPER_REFRESH_DATA.equals(messageType.type)) {
            refreshViewData();
        }
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mQuestionNumber = (TextView) view.findViewById(R.id.question_number);
        mQuestionType = (TextView) view.findViewById(R.id.question_type);
        mQuestionScore = (TextView) view.findViewById(R.id.question_score);

        mCurrentIndex = 1;
    }

    protected abstract void refreshViewData();

    public ArrayList<QuestionTypeSeq> getQuestion(QuestionType type)
    {
        return mTestpaperActivity.getQuesions(type);
    }
}
