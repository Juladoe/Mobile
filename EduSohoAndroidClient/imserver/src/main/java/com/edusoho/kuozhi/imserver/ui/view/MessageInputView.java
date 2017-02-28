package com.edusoho.kuozhi.imserver.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.imserver.R;
import com.edusoho.kuozhi.imserver.ui.listener.InputViewControllerListener;
import com.edusoho.kuozhi.imserver.ui.listener.MessageSendListener;
import com.edusoho.kuozhi.imserver.ui.util.MediaRecorderTask;
import com.edusoho.kuozhi.imserver.util.SystemUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * Created by suju on 16/8/26.
 */
public class MessageInputView extends FrameLayout implements IMessageInputView {

    protected ESIconView btnVoice;
    protected ESIconView btnKeyBoard;
    protected EditText etSend;
    protected Button btnSend;
    protected ESIconView ivAddMedia;
    protected View viewMediaLayout;
    protected View viewPressToSpeak;
    protected View viewMsgInput;
    protected TextView tvSpeak;
    protected TextView tvSpeakHint;
    protected View mViewSpeakContainer;
    protected ImageView ivRecordImage;

    private RecordAudioHandler mRecordAudioHandler;
    private MessageSendListener mMessageSendListener;
    private InputViewControllerListener mMessageControllerListener;

    public MessageInputView(Context context) {
        super(context);
        createView();
    }

    public MessageInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createView();
    }

    private void createView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_message_list_tool_layout, this, true);
        initView();
    }

    @Override
    public void setMessageSendListener(MessageSendListener listener) {
        this.mMessageSendListener = listener;
    }

    @Override
    public void setMessageControllerListener(InputViewControllerListener listener) {
        this.mMessageControllerListener = listener;
    }

    protected void initView() {
        OnClickListener onClickListener = getViewOnClickListener();
        etSend = (EditText) findViewById(R.id.et_send_content);
        etSend.addTextChangedListener(mContentTextWatcher);
        etSend.setOnEditorActionListener(getOnEditorActionListener());
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(onClickListener);
        etSend.setOnFocusChangeListener(getContentOnFocusChangeListener());
        etSend.setOnClickListener(onClickListener);
        ivAddMedia = (ESIconView) findViewById(R.id.iv_show_media_layout);
        ivAddMedia.setOnClickListener(onClickListener);
        viewMediaLayout = findViewById(R.id.ll_media_layout);
        btnVoice = (ESIconView) findViewById(R.id.btn_voice);
        btnVoice.setOnClickListener(onClickListener);
        btnKeyBoard = (ESIconView) findViewById(R.id.btn_set_mode_keyboard);
        btnKeyBoard.setOnClickListener(onClickListener);
        viewPressToSpeak = findViewById(R.id.rl_btn_press_to_speak);
        viewPressToSpeak.setOnTouchListener(getViewOnTouchListener());
        viewPressToSpeak.setOnClickListener(onClickListener);
        viewMsgInput = findViewById(R.id.rl_msg_input);

        ESIconView ivPhoto = (ESIconView) findViewById(R.id.iv_image);
        ivPhoto.setOnClickListener(onClickListener);
        ESIconView ivCamera = (ESIconView) findViewById(R.id.iv_camera);
        ivCamera.setOnClickListener(onClickListener);
        tvSpeak = (TextView) findViewById(R.id.tv_speak);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initSpeakContainer();
    }

    private TextView.OnEditorActionListener getOnEditorActionListener() {
        return new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendClick();
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mViewSpeakContainer != null) {
            mViewSpeakContainer.setEnabled(enabled);
        }
        etSend.setEnabled(enabled);
        ivAddMedia.setEnabled(enabled);
        btnVoice.setEnabled(enabled);
    }

    private void initSpeakContainer() {
        mViewSpeakContainer = LayoutInflater.from(getContext()).inflate(R.layout.view_message_record_layout, null);
        ViewParent viewParent = getParent().getParent();
        if (viewParent != null) {
            if (viewParent instanceof FrameLayout) {
                FrameLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.CENTER;
                ((FrameLayout) viewParent).addView(mViewSpeakContainer, lp);
            } else if (viewParent instanceof RelativeLayout) {
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_VERTICAL);
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                ((RelativeLayout) viewParent).addView(mViewSpeakContainer, lp);
            } else {
                ((ViewGroup) viewParent).addView(mViewSpeakContainer);
            }
        }

        tvSpeakHint = (TextView) mViewSpeakContainer.findViewById(R.id.tv_speak_hint);
        ivRecordImage = (ImageView) mViewSpeakContainer.findViewById(R.id.iv_voice_volume);
        mViewSpeakContainer.bringToFront();
    }

    protected OnFocusChangeListener getContentOnFocusChangeListener() {
        return new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    viewMediaLayout.setVisibility(View.GONE);
                    SystemUtil.setSoftKeyBoard(etSend, getContext(), SystemUtil.SHOW_KEYBOARD);
                }
            }
        };
    }

    private void sendClick() {
        //发送消息
        if (etSend.getText().length() == 0) {
            return;
        }
        mMessageSendListener.onSendMessage(etSend.getText().toString());
        etSend.setText("");
    }

    protected OnClickListener getViewOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.et_send_content) {
                    mMessageControllerListener.onInputViewFocus(true);
                } else if (v.getId() == R.id.iv_show_media_layout) {
                    //加号，显示多媒体框
                    if (viewMediaLayout.getVisibility() == View.GONE) {
                        viewMediaLayout.setVisibility(View.VISIBLE);
                        etSend.clearFocus();
                        SystemUtil.setSoftKeyBoard(etSend, getContext(), SystemUtil.HIDE_KEYBOARD);
                    } else {
                        viewMediaLayout.setVisibility(View.GONE);
                    }
                    //lvMessage.post(mListViewSelectRunnable);
                } else if (v.getId() == R.id.btn_send) {
                    sendClick();
                } else if (v.getId() == R.id.btn_voice) {
                    MobclickAgent.onEvent(getContext(), "chatWindow_voiceButton");
                    //语音
                    viewMediaLayout.setVisibility(View.GONE);
                    btnVoice.setVisibility(View.GONE);
                    viewMsgInput.setVisibility(View.GONE);
                    btnKeyBoard.setVisibility(View.VISIBLE);
                    viewPressToSpeak.setVisibility(View.VISIBLE);
                    SystemUtil.setSoftKeyBoard(etSend, getContext(), SystemUtil.HIDE_KEYBOARD);
                } else if (v.getId() == R.id.btn_set_mode_keyboard) {
                    //键盘
                    viewMediaLayout.setVisibility(View.GONE);
                    btnVoice.setVisibility(View.VISIBLE);
                    viewPressToSpeak.setVisibility(View.GONE);
                    viewMsgInput.setVisibility(View.VISIBLE);
                    btnKeyBoard.setVisibility(View.GONE);
                    etSend.requestFocus();
                } else if (v.getId() == R.id.rl_btn_press_to_speak) {
                    viewMediaLayout.setVisibility(View.GONE);
                } else if (v.getId() == R.id.iv_image) {
                    MobclickAgent.onEvent(getContext(), "chatWindow_PlusButton_picture");
                    mMessageControllerListener.onSelectPhoto();
                } else if (v.getId() == R.id.iv_camera) {
                    mMessageControllerListener.onTakePhoto();
                }
            }
        };
    }

    protected OnTouchListener getViewOnTouchListener() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mMessageSendListener.onStartRecordAudio();
                        mRecordAudioHandler = new RecordAudioHandler(getContext());
                        return mRecordAudioHandler.startRecord(event.getY());
                    case MotionEvent.ACTION_MOVE:
                        return mRecordAudioHandler.checkContinueRecord(event.getY());
                    case MotionEvent.ACTION_UP:
                        mRecordAudioHandler.stopRecord();
                        mRecordAudioHandler = null;
                        return true;
                }
                return false;
            }
        };
    }

    public class VolumeHandler extends Handler {

        protected int[] mSpeakerAnimResId = new int[]{R.drawable.record_animate_1,
                R.drawable.record_animate_2,
                R.drawable.record_animate_3,
                R.drawable.record_animate_4};

        public static final int COUNT_DOWN = 4;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COUNT_DOWN) {
                int w = ivRecordImage.getWidth();
                int h = ivRecordImage.getWidth();
                ivRecordImage.setImageBitmap(getCountDownBitmap(w, h, msg.arg1));
                return;
            }
            ivRecordImage.setImageResource(mSpeakerAnimResId[msg.what]);
        }

        private Bitmap getCountDownBitmap(int w, int h, int number) {
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setTextSize(w * 0.9f);
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);

            Rect rect = new Rect();
            paint.getTextBounds(String.valueOf(number), 0, 1, rect);
            canvas.drawText(String.valueOf(number), (w - (rect.right - rect.left)) / 2, (h - rect.bottom - rect.top) / 2, paint);
            return bitmap;
        }
    }

    private class RecordAudioHandler {

        private float mPressDownY;
        private Context mContext;
        private boolean mHandUpAndCancel;
        private MediaRecorderTask mMediaRecorderTask;

        public RecordAudioHandler(Context context) {
            this.mContext = context;
        }

        public boolean startRecord(float pressDownY) {
            try {
                this.mPressDownY = pressDownY;
                mMediaRecorderTask = new MediaRecorderTask(mContext, new VolumeHandler(), getMediaRecorderTackListener());
                mMediaRecorderTask.execute();
            } catch (Exception e) {
                mMediaRecorderTask.getAudioRecord().clear();
            }
            return false;
        }

        private MediaRecorderTask.MediaRecorderTackListener getMediaRecorderTackListener() {
            return new MediaRecorderTask.MediaRecorderTackListener() {

                @Override
                public void onCancel() {
                    mViewSpeakContainer.setVisibility(View.GONE);
                }

                @Override
                public void onReset() {
                    tvSpeak.setText(mContext.getString(R.string.hand_press_and_speak));
                    viewPressToSpeak.setPressed(false);
                }

                @Override
                public void onPreRecord() {
                    mViewSpeakContainer.setVisibility(View.VISIBLE);
                    tvSpeak.setText(mContext.getString(R.string.hand_up_and_end));
                    tvSpeakHint.setText(getResources().getString(R.string.hand_move_up_and_send_cancel));
                    tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_transparent_bg);
                    ivRecordImage.setImageResource(R.drawable.record_animate_1);
                }

                @Override
                public void onStopRecord(File audioFile) {
                    if (audioFile == null || !audioFile.exists()) {
                        tvSpeakHint.setText(mContext.getString(R.string.audio_length_too_short));
                        tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_transparent_bg);
                        ivRecordImage.setImageResource(R.drawable.record_duration_short);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mViewSpeakContainer.setVisibility(View.GONE);
                            }
                        }, 200);
                    } else {
                        sendAudioToMessage(audioFile);
                    }
                    mMessageSendListener.onStopRecordAudio();
                }

                private void sendAudioToMessage(File audioFile) {
                    mViewSpeakContainer.setVisibility(View.GONE);
                    mMessageSendListener.onSendAudio(
                            audioFile,
                            (int) mMediaRecorderTask.getAudioRecord().getAudioLength()
                    );
                }
            };
        }

        public boolean checkContinueRecord(float scrollY) {
            if (mMediaRecorderTask.getStopRecord()) {
                return true;
            }

            if (Math.abs(mPressDownY - scrollY) > SystemUtil.getScreenHeight(mContext) * 0.1) {
                tvSpeak.setText(mContext.getString(R.string.hand_up_and_exit));
                tvSpeakHint.setText(mContext.getString(R.string.hand_up_and_exit));
                tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_bg);
                ivRecordImage.setImageResource(R.drawable.record_cancel);
                mHandUpAndCancel = true;
            } else {
                if (!mMediaRecorderTask.isCountDown()) {
                    ivRecordImage.setImageResource(R.drawable.record_animate_1);
                }
                tvSpeakHint.setText(mContext.getString(R.string.hand_move_up_and_send_cancel));
                tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_transparent_bg);
                tvSpeak.setText(mContext.getString(R.string.hand_up_and_end));
                mHandUpAndCancel = false;
            }
            mMediaRecorderTask.setCancel(mHandUpAndCancel);
            return true;
        }

        public void stopRecord() {
            mMediaRecorderTask.setAudioStop(true);
        }
    }

    protected TextWatcher mContentTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!TextUtils.isEmpty(s)) {
                btnSend.setVisibility(View.VISIBLE);
                ivAddMedia.setVisibility(View.GONE);
            } else {
                ivAddMedia.setVisibility(View.VISIBLE);
                btnSend.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
}
