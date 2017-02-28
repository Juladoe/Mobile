package com.edusoho.kuozhi.v3.ui.friend;

import com.edusoho.kuozhi.v3.model.bal.Friend;

import java.util.Comparator;

/**
 * Created by Melomelon on 2015/8/4.
 */
public class FriendComparator implements Comparator<Friend> {
    @Override
    public int compare(Friend lhs, Friend rhs) {
        if (lhs.getSortLetters().equals("@")
                || rhs.getSortLetters().equals("#")) {
            return -1;
        } else if (lhs.getSortLetters().equals("#")
                || rhs.getSortLetters().equals("@")) {
            return 1;
        } else {
            return lhs.getSortLetters().compareTo(rhs.getSortLetters());
        }
    }
}
