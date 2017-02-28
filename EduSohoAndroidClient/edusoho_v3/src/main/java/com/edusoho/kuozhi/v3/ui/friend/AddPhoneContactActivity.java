package com.edusoho.kuozhi.v3.ui.friend;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.model.bal.PhoneContact;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Melomelon on 2015/6/5.
 */
public class AddPhoneContactActivity extends ActionBarBaseActivity {
    public static final String TAG = "AddPhoneContactActivity";
    //**phone数据库字段 名字 号码 照片 联系人ID
    private static final String[] PHONES_PROJECTION = new String[]{Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID};
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
    private static final int PHONES_NUMBER_INDEX = 1;
    private static final int PHONES_PHOTO_ID_INDEX = 2;
    private static final int PHONES_CONTACT_ID_INDEX = 3;

    public static final int SHOW = 0x01;
    public static final int LOAD = 0x02;

    private LoadDialog loadDialog;

    private ArrayList<PhoneContact> mContactList;
    private ArrayList<PhoneContact> mTempList;
    private AddPhoneContactAdapter mAddAdapter;

    private ListView mList;

    private LoadHandler mLoadHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackMode(BACK, "添加手机好友");
        setContentView(R.layout.add_phone_contact_layout);
        mList = (ListView) findViewById(R.id.add_phone_contact_list);

        mAddAdapter = new AddPhoneContactAdapter();
        mList.setAdapter(mAddAdapter);

        mLoadHandler = new LoadHandler(this);

        loadDialog = LoadDialog.create(this);
        loadDialog.setMessage("请等待…");
        loadDialog.show();

        new Thread(new GetContactRunnable()).start();
    }

    class GetContactRunnable implements Runnable {
        @Override
        public void run() {
            getPhoneContact();
            mLoadHandler.sendEmptyMessage(SHOW);
        }
    }

    public void getPhoneContact() {

        ContentResolver contentResolver = mContext.getContentResolver();

        Cursor phoneCursor = contentResolver.query(Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null);

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                if (TextUtils.isEmpty(phoneNumber)) {
                    continue;
                } else if (phoneNumber.length() < 11) {
                    continue;
                }
                String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                Long contactId = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);
                Long photoId = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);
                Bitmap contactPhoto;

                if (photoId > 0) {
                    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                    InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, uri);
                    contactPhoto = BitmapFactory.decodeStream(inputStream);
                } else {
                    contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);
                }

                PhoneContact phoneContact = new PhoneContact(contactName, phoneNumber, contactPhoto);
                mAddAdapter.addItem(phoneContact);
            }
        }
        mAddAdapter.addItems(mTempList);
        phoneCursor.close();
    }

    public class AddPhoneContactAdapter extends BaseAdapter {

        public AddPhoneContactAdapter() {
            mContactList = new ArrayList();
            mTempList = new ArrayList();
        }

        @Override
        public int getCount() {
            return mContactList.size();
        }

        @Override
        public Object getItem(int position) {
            return mContactList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemHolder itemHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.add_phone_contact_item, null);
                itemHolder = new ItemHolder();
                itemHolder.mContactName = (TextView) convertView.findViewById(R.id.phone_contact_Name);
                itemHolder.mContactNumber = (TextView) convertView.findViewById(R.id.phone_contact_number);
                itemHolder.mContactImage = (CircleImageView) convertView.findViewById(R.id.phone_contact_image);
                itemHolder.mTag = (ImageView) convertView.findViewById(R.id.add_contact_tag);
                convertView.setTag(itemHolder);
            } else {
                itemHolder = (ItemHolder) convertView.getTag();
            }
            itemHolder.mContactName.setText(mContactList.get(position).contactName);
            itemHolder.mContactNumber.setText(mContactList.get(position).contactNumber);
            itemHolder.mContactImage.setImageBitmap(mContactList.get(position).contactImage);

            mContactList.get(position).isFriend = false;
            if (mContactList.get(position).isFriend) {
                itemHolder.mTag.setImageResource(R.drawable.have_add_friend_true);
            } else {
                itemHolder.mTag.setImageResource(R.drawable.add_friend_selector);
            }
            itemHolder.mTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO
                }
            });
            return convertView;
        }

        public void addItem(PhoneContact phoneContact) {
            mTempList.add(phoneContact);
        }

        public void addItems(ArrayList<PhoneContact> list) {
            mContactList.addAll(list);
            mLoadHandler.sendEmptyMessage(LOAD);
        }

        public class ItemHolder {
            private CircleImageView mContactImage;
            private TextView mContactName;
            private TextView mContactNumber;
            private ImageView mTag;
        }
    }

    public static class LoadHandler extends Handler {
        private WeakReference<AddPhoneContactActivity> mActivity;

        public LoadHandler(AddPhoneContactActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final AddPhoneContactActivity addPhoneContactActivity = mActivity.get();
            if (mActivity != null) {
                if (msg.what == SHOW) {
                    if (addPhoneContactActivity.loadDialog != null) {
                        addPhoneContactActivity.loadDialog.dismiss();
                    }
                    super.handleMessage(msg);
                } else if (msg.what == LOAD) {
                    addPhoneContactActivity.mAddAdapter.notifyDataSetChanged();
                    super.handleMessage(msg);
                }
            }
        }
    }

}
