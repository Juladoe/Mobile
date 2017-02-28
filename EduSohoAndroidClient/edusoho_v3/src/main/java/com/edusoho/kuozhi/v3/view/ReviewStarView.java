package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhang on 2016/12/13.
 */

public class ReviewStarView extends LinearLayout {
    public ReviewStarView(Context context) {
        super(context);
        init();
    }

    public ReviewStarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReviewStarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private int rating = 0;
    private List<EduSohoNewIconView> stars = new ArrayList<>();

    public void setRating(int rating) {
        this.rating = rating;
        if (getChildAt(0) == null) {
            return;
        }
        for (int i = 0; i < rating && i < 5; i++) {
            ((TextView) ((ViewGroup) getChildAt(0)).getChildAt(i))
                    .setTextColor(getResources().getColor(R.color.secondary2_color));
        }
        for (int i = rating; i < 5; i++) {
            ((TextView) ((ViewGroup) getChildAt(0)).getChildAt(i))
                    .setTextColor(getResources().getColor(R.color.disabled_hint_color));
        }
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_review_star, this, false);
        addView(view);
    }
}
