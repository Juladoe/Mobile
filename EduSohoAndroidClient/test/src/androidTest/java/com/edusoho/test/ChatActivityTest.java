package com.edusoho.test;

import android.content.Intent;
import android.os.SystemClock;
import android.test.UiThreadTest;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.edusoho.kuozhi.v3.ui.ChatActivity;
import com.edusoho.kuozhi.v3.view.EduSohoIconView;
import com.edusoho.test.base.BaseActivityUnitTestCase;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * Created by JesseHuang on 15/8/18.
 */
public class ChatActivityTest extends BaseActivityUnitTestCase<ChatActivity> {

    public ChatActivityTest() {
        super(ChatActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                ChatActivity.class);
    }

    @UiThreadTest
    public void testChatActivity() {
        ChatActivity mChatActivity = getActivity();
        assertNotNull(mChatActivity);
    }

    @UiThreadTest
    public void testInitUI() {
        ChatActivity mChatActivity = getActivity();
        ListView listView = (ListView) mChatActivity.findViewById(R.id.lv_messages);
        assertNotNull(listView);

        PtrClassicFrameLayout ptrClassicFrameLayout = (PtrClassicFrameLayout) mChatActivity.findViewById(R.id.rotate_header_list_view_frame);
        assertNotNull(ptrClassicFrameLayout);

        View viewSpeakContainer = mChatActivity.findViewById(R.id.recording_container);
        assertNotNull(viewSpeakContainer);
        assertEquals(View.GONE, viewSpeakContainer.getVisibility());
        ImageView ivRecordImage = (ImageView) mChatActivity.findViewById(R.id.iv_voice_volume);
        assertNotNull(ivRecordImage);
        TextView tvSpeakHint = (TextView) mChatActivity.findViewById(R.id.tv_speak_hint);
        assertNotNull(tvSpeakHint);

        //语音图标
        EduSohoIconView btnVoice = (EduSohoIconView) mChatActivity.findViewById(R.id.btn_voice);
        assertNotNull(btnVoice);
        assertEquals(View.VISIBLE, btnVoice.getVisibility());
        assertEquals(mChatActivity.getResources().getString(R.string.font_chat_voice), btnVoice.getText());
        //键盘图标
        EduSohoIconView btnKeyBoard = (EduSohoIconView) mChatActivity.findViewById(R.id.btn_set_mode_keyboard);
        assertNotNull(btnKeyBoard);
        assertEquals(mChatActivity.getResources().getString(R.string.font_chat_keyboard), btnKeyBoard.getText());
        assertEquals(View.GONE, btnKeyBoard.getVisibility());
        //按住说话按钮
        View viewPressToSpeak = mChatActivity.findViewById(R.id.rl_btn_press_to_speak);
        assertNotNull(viewPressToSpeak);
        assertEquals(View.GONE, viewPressToSpeak.getVisibility());
        TextView tvSpeak = (TextView) mChatActivity.findViewById(R.id.tv_speak);
        assertNotNull(tvSpeak);
        assertEquals(mChatActivity.getResources().getString(R.string.hand_press_and_speak), tvSpeak.getText());
        View viewMsgInput = mChatActivity.findViewById(R.id.rl_msg_input);
        assertNotNull(viewMsgInput);
        assertEquals(View.VISIBLE, viewMsgInput.getVisibility());
        EditText etSend = (EditText) mChatActivity.findViewById(R.id.et_send_content);
        assertNotNull(etSend);
        assertEquals(View.VISIBLE, etSend.getVisibility());
        EduSohoIconView ivAddMedia = (EduSohoIconView) mChatActivity.findViewById(R.id.iv_show_media_layout);
        assertNotNull(ivAddMedia);
        assertEquals(mChatActivity.getResources().getString(R.string.font_chat_add_media), ivAddMedia.getText());
        Button btnSend = (Button) mChatActivity.findViewById(R.id.btn_send);
        assertNotNull(btnSend);
        assertEquals(View.GONE, btnSend.getVisibility());

        View viewMediaLayout = mChatActivity.findViewById(R.id.ll_media_layout);
        assertEquals(View.GONE, viewMediaLayout.getVisibility());
    }

//    @UiThreadTest
//    public void testProfileMenu() {
//        mChatActivity = getActivity();
//        InvocationHandler handler = new InvocationHandler() {
//            @Override
//            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                return null;
//            }
//        };
//        final Menu menu = (Menu) Proxy.newProxyInstance(mChatActivity.getClassLoader(), new Class<?>[]{Menu.class}, handler);
//        if (!mChatActivity.onCreateOptionsMenu(menu)) {
//            throw new AssertionError("onCreateOptionsMenu returned false -> it requested the menu not to be shown");
//        }
//        if (!mChatActivity.onPrepareOptionsMenu(menu)) {
//            throw new AssertionError("onPrepareOptionsMenu returned false -> it requested the menu not to be shown");
//        }
//
//        assertNotNull(mChatActivity);
//    }

    @UiThreadTest
    public void testMsgButton() {
        ChatActivity mChatActivity = getActivity();
        EditText etSend = (EditText) mChatActivity.findViewById(R.id.et_send_content);
        etSend.setText("This is a text.");
        assertEquals(View.VISIBLE, etSend.getVisibility());
        EduSohoIconView ivAddMedia = (EduSohoIconView) mChatActivity.findViewById(R.id.iv_show_media_layout);
        assertEquals(View.GONE, ivAddMedia.getVisibility());
    }

    @UiThreadTest
    public void testMediaButtonShow() {
        ChatActivity mChatActivity = getActivity();
        EduSohoIconView ivAddMedia = (EduSohoIconView) mChatActivity.findViewById(R.id.iv_show_media_layout);
        ivAddMedia.performClick();
        View viewMediaLayout = mChatActivity.findViewById(R.id.ll_media_layout);
        assertEquals(View.VISIBLE, viewMediaLayout.getVisibility());

        EduSohoIconView ivPhoto = (EduSohoIconView) mChatActivity.findViewById(R.id.iv_image);
        assertNotNull(ivPhoto);
        EduSohoIconView ivCamera = (EduSohoIconView) mChatActivity.findViewById(R.id.iv_camera);
        assertNotNull(ivCamera);
    }

    @UiThreadTest
    public void testVoiceSpeaker() throws Throwable {
        ChatActivity mChatActivity = getActivity();
        EduSohoIconView btnVoice = (EduSohoIconView) mChatActivity.findViewById(R.id.btn_voice);
        btnVoice.performClick();
        assertEquals(View.GONE, btnVoice.getVisibility());
        EduSohoIconView btnKeyBoard = (EduSohoIconView) mChatActivity.findViewById(R.id.btn_set_mode_keyboard);
        assertEquals(View.VISIBLE, btnKeyBoard.getVisibility());
        final View viewPressToSpeak = mChatActivity.findViewById(R.id.rl_btn_press_to_speak);
        assertEquals(View.VISIBLE, viewPressToSpeak.getVisibility());

        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = 0.0f;
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_DOWN,
                x,
                y,
                metaState
        );
        viewPressToSpeak.dispatchTouchEvent(motionEvent);
        View viewSpeakContainer = mChatActivity.findViewById(R.id.recording_container);
        assertEquals(View.VISIBLE, viewSpeakContainer.getVisibility());
    }
}
