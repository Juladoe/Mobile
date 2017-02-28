package com.edusoho.kuozhi.v3.ui.friend;

import com.edusoho.kuozhi.v3.model.bal.DiscussionGroup;

import java.util.Comparator;

/**
 * Created by Melomelon on 2015/9/15.
 */
public class GroupComparator implements Comparator<DiscussionGroup> {
    @Override
    public int compare(DiscussionGroup lGroup, DiscussionGroup rGroup) {
        if (lGroup.getSortLetters().equals("@")
                || rGroup.getSortLetters().equals("#")) {
            return -1;
        } else if (lGroup.getSortLetters().equals("#")
                || rGroup.getSortLetters().equals("@")) {
            return 1;
        } else {
            return lGroup.getSortLetters().compareTo(rGroup.getSortLetters());
        }
    }
}
