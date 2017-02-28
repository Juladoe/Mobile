package com.edusoho.kuozhi.homework.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import com.edusoho.kuozhi.homework.view.test.HomeworkQuestionWidget;
import com.edusoho.kuozhi.v3.model.bal.test.MaterialQuestionTypeSeq;

import java.util.ArrayList;
import java.util.List;

public class HomeworkQuestionAdapter extends PagerAdapter {

    protected LayoutInflater inflater;
    protected int mResouce;
    protected Context mContext;
    protected String mType;
    protected List<HomeWorkQuestion> mList;

    public HomeworkQuestionAdapter(
            String type, Context context, List<HomeWorkQuestion> list)
    {
        mList = list;
        mType = type;
        mContext = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    protected Spanned getQuestionStem(
            MaterialQuestionTypeSeq questionTypeSeq, int mIndex, TextView textView){
        return null;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    protected View switchQuestionWidget(HomeWorkQuestion homeWorkQuestion, int index)
    {
        HomeworkQuestionWidget widget = new HomeworkQuestionWidget(mType, mContext, homeWorkQuestion, index);
        return widget.getView();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        HomeWorkQuestion question = mList.get(position);
        View view = switchQuestionWidget(question, position + 1);
        ScrollView scrollView = new ScrollView(mContext);
        scrollView.setFillViewport(true);
        scrollView.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        container.addView(scrollView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return scrollView;
    }
}
