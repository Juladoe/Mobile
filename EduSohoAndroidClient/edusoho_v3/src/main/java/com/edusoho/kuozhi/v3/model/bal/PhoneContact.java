package com.edusoho.kuozhi.v3.model.bal;

import android.graphics.Bitmap;

/**
 * Created by Melomelon on 2015/6/5.
 */
public class PhoneContact {

    public PhoneContact(String contactName, String contactNumber, Bitmap contactImage) {
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.contactImage = contactImage;
    }

    public String contactName;
    public String contactNumber;
    public Bitmap contactImage;
    public boolean isMember;
    public boolean isFriend;
}
