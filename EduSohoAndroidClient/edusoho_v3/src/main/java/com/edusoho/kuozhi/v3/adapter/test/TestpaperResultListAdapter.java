package com.edusoho.kuozhi.v3.adapter.test;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.model.bal.test.Accuracy;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by howzhi on 14-10-9.
 */
public class TestpaperResultListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private int mResouce;
    private Context mContext;
    private HashMap<QuestionType, Accuracy> mList;
    private ArrayList<QuestionType> mTypeList;

    public TestpaperResultListAdapter(
            Context context,
            HashMap<QuestionType, Accuracy> list,
            ArrayList<QuestionType> typeList,
            int resource
    ) {
        mList = list;
        mTypeList = typeList;
        mContext = context;
        mResouce = resource;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getCount() {
        return mTypeList.size();
    }

    @Override
    public Object getItem(int i) {
        return mTypeList.get(i);
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        View currentView;

        QuestionType questionType = mTypeList.get(index);
        Accuracy accuracy = mList.get(questionType);

        if (view == null) {
            currentView = inflater.inflate(mResouce, null);
            holder = new ViewHolder();
            holder.mType = (TextView) currentView.findViewById(R.id.testpaper_result_type);
            holder.mRight = (TextView) currentView.findViewById(R.id.testpaper_result_right);
            holder.mTotal = (TextView) currentView.findViewById(R.id.testpaper_result_total);
            currentView.setTag(holder);
        } else {
            currentView = view;
            holder = (ViewHolder) currentView.getTag();
        }

        holder.mType.setText(questionType.title());
        setRightText(holder.mRight, accuracy.right, accuracy.all);
        setRightText(holder.mTotal, accuracy.score, accuracy.totalScore);
        return currentView;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    private void setRightText(TextView rightText, Object startObj, Object endObj) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(startObj);
        int start = stringBuffer.length();
        stringBuffer.append("/");
        stringBuffer.append(endObj);
        SpannableString spannableString = new SpannableString(stringBuffer);
        int color = mContext.getResources().getColor(R.color.action_bar_bg);
        spannableString.setSpan(
                new ForegroundColorSpan(color),
                0,
                start,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        );
        rightText.setText(spannableString);
    }

    private class ViewHolder {
        public TextView mType;
        public TextView mRight;
        public TextView mTotal;
    }
}
