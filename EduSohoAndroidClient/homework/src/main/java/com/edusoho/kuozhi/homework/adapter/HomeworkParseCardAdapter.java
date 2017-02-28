package com.edusoho.kuozhi.homework.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;

import java.util.List;

/**
 * Created by howzhi on 15/10/23.
 */
public class HomeworkParseCardAdapter extends HomeworkCardAdapter {

    public HomeworkParseCardAdapter(
            Context context,
            List<HomeWorkQuestion> answers,
            int resource)
    {
        super(context, answers, resource);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = super.getView(i, view, viewGroup);
        HomeWorkQuestion question = mList.get(i);
        if (! "right".equals(question.getResult().status)) {
            view.setBackgroundResource(R.drawable.hw_card_item_bg_selected);
        }
        return view;
    }
}
