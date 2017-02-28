package com.edusoho.kuozhi.v3.adapter.test;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.model.bal.test.MaterialQuestionTypeSeq;
import com.edusoho.kuozhi.v3.model.bal.test.Question;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionTypeSeq;
import com.edusoho.kuozhi.v3.util.html.EduImageGetterHandler;
import com.edusoho.kuozhi.v3.util.html.EduTagHandler;

import java.util.ArrayList;

/**
 * Created by howzhi on 14-9-29.
 */
public class MaterialQuestionAdapter extends QuestionViewPagerAdapter {


    public MaterialQuestionAdapter(
            Context context, ArrayList<MaterialQuestionTypeSeq> list) {
        super(context, null, 0);
        mList = new ArrayList<QuestionTypeSeq>();
        for (MaterialQuestionTypeSeq materialQuestionTypeSeq : list) {
            mList.add(materialQuestionTypeSeq);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        MaterialQuestionTypeSeq questionTypeSeq = (MaterialQuestionTypeSeq) mList.get(position);

        View materialView = LayoutInflater.from(mContext).inflate(
                R.layout.material_item_layout, null);
        TextView stemText = (TextView) materialView.findViewById(R.id.question_stem);
        ViewGroup viewContent = (ViewGroup) materialView.findViewById(R.id.question_content);

        View view = switchQuestionWidget(questionTypeSeq, position + 1);
        stemText.setText(getQuestionStem(questionTypeSeq, position + 1, stemText));
        viewContent.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        container.addView(materialView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return materialView;
    }

    @Override
    protected Spanned getQuestionStem(
            MaterialQuestionTypeSeq questionTypeSeq, int mIndex, TextView textView) {
        Question question = questionTypeSeq.parent.question;
        return Html.fromHtml(question.stem, new EduImageGetterHandler(mContext, textView), new EduTagHandler());
    }

    private double getMaterialScore(ArrayList<QuestionTypeSeq> itemSeqs) {
        double total = 0;
        for (QuestionTypeSeq seq : itemSeqs) {
            total += seq.question.score;
        }

        return total;
    }
}
