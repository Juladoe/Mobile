package com.edusoho.kuozhi.homework.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.v3.model.bal.test.QuestionType;
import java.util.LinkedHashMap;

/**
 * Created by howzhi on 15/10/21.
 */
public class HomeWorkResultListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context mContext;
    private QuestionType[] mQuestionTypes;
    private LinkedHashMap<QuestionType, Integer> mList;

    public HomeWorkResultListAdapter(Context context, LinkedHashMap<QuestionType, Integer> list)
    {
        mList = list;
        mQuestionTypes = new QuestionType[mList.size()];
        mList.keySet().toArray(mQuestionTypes);

        mContext = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getCount() {
        return mQuestionTypes.length;
    }

    @Override
    public Object getItem(int i) {
        return mQuestionTypes[i];
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        View currentView;
        QuestionType questionType = mQuestionTypes[index];

        if (view == null) {
            currentView = inflater.inflate(R.layout.homework_result_list_item, null);
            holder = new ViewHolder();
            holder.mType = (TextView) currentView.findViewById(R.id.hw_result_type);
            holder.mTotal = (TextView) currentView.findViewById(R.id.hw_result_total);
            currentView.setTag(holder);
        } else {
            currentView = view;
            holder = (ViewHolder) currentView.getTag();
        }

        currentView.setEnabled(index % 2 == 0);
        holder.mType.setText(questionType.name);
        int result = mList.get(questionType);
        setRightText(holder.mTotal, result >> 4, result & 0x0f);
        return currentView;
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
        public TextView mTotal;
    }
}
