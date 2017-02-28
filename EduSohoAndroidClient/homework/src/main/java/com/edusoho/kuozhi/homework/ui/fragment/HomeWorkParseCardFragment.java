package com.edusoho.kuozhi.homework.ui.fragment;

import android.view.View;
import android.widget.BaseAdapter;

import com.edusoho.kuozhi.homework.R;
import com.edusoho.kuozhi.homework.adapter.HomeworkCardAdapter;
import com.edusoho.kuozhi.homework.adapter.HomeworkParseCardAdapter;
import com.edusoho.kuozhi.homework.model.HomeWorkQuestion;

import java.util.List;

/**
 * Created by howzhi on 15/10/23.
 */
public class HomeWorkParseCardFragment extends HomeWorkCardFragment {

    @Override
    protected void initView(View view) {
        super.initView(view);
        mSubmitBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    protected BaseAdapter getCardAdapter(List<HomeWorkQuestion> questionList) {
        return new HomeworkParseCardAdapter(
                getActivity().getBaseContext(), questionList, R.layout.hw_card_item
        );
    }
}
