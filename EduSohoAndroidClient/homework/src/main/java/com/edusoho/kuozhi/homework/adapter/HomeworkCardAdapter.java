package com.edusoho.kuozhi.homework.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;
import java.util.List;

/**
 * Created by howzhi on 15/10/20.
 */

public class HomeworkCardAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private int mResouce;
    private Context mContext;
    protected List<HomeWorkQuestion> mList;

    public HomeworkCardAdapter(
            Context context,
            List<HomeWorkQuestion> answers,
            int resource)
    {
        mList = answers;
        mContext = context;
        mResouce = resource;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(mResouce, null);
        }

        TextView mText = (TextView) view;
        List<String> answers = mList.get(i).getAnswer();
        boolean isNoAnswer = (answers == null)?true:isFillEmpty(answers);
        mText.setBackgroundResource(isNoAnswer ? R.drawable.hw_card_item_bg : R.drawable.hw_card_item_bg_pressed);
        mText.setTextColor(mContext.getResources().getColor(isNoAnswer ? R.color.primary : R.color.white));
        mText.setText(String.valueOf(i + 1));
        return view;
    }

    private boolean isFillEmpty(List<String> list){
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).length()!=0){
                return false;
            }
        }
        return true;
    }

}