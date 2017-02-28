package com.edusoho.kuozhi.v3.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.shard.ThirdPartyLogin;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PromiseCallback;
import com.edusoho.kuozhi.v3.model.provider.IMServiceProvider;
import com.edusoho.kuozhi.v3.model.result.UserResult;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.base.BaseNoTitleActivity;
import com.edusoho.kuozhi.v3.ui.fragment.FindPasswordByPhoneFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.InputUtils;
import com.edusoho.kuozhi.v3.util.OpenLoginUtil;
import com.edusoho.kuozhi.v3.util.Promise;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.edusoho.kuozhi.v3.util.encrypt.XXTEA;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.edusoho.kuozhi.v3.view.qr.CaptureActivity;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.edusoho.kuozhi.v3.ui.QrSchoolActivity.REQUEST_QR;

/**
 * Created by JesseHuang on 15/5/22.
 */
public class LoginActivity extends BaseNoTitleActivity {

    public static final int TYPE_LOGIN = 1;
    public static final int OK = 1003;
    public static final String FIND_PASSWORD_ACCOUNT = "find_password_account";
    private static final String EnterSchool = "enter_school";
    private static boolean isRun;
    private EditText etUsername;
    private EditText etPassword;
    private View mTvLogin;
    private ImageView ivWeibo;
    private ImageView ivQQ;
    private ImageView ivWeixin;
    private ImageView ivUserCancel;
    private ImageView ivPwCancel;
    private TextView tvMore;
    private TextView tvRegister;
    private TextView tvForgetPassword;
    private String mAuthCancel;
    private View vSao;
    private View mParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuthCancel = mContext.getResources().getString(R.string.authorize_cancelled);
        initView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            etUsername.setText(intent.getStringExtra(FIND_PASSWORD_ACCOUNT));
            etPassword.requestFocus();
            InputUtils.showKeyBoard(etPassword, mContext);
        }
    }

    @Override
    protected void initView() {
        super.initView();
        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        mTvLogin = findViewById(R.id.tv_login);
        mTvLogin.setOnClickListener(mLoginClickListener);
        ivWeibo = (ImageView) findViewById(R.id.iv_weibo);
        ivWeibo.setOnClickListener(mWeiboLoginClickListener);
        ivQQ = (ImageView) findViewById(R.id.iv_qq);
        ivQQ.setOnClickListener(mQQLoginClickListener);
        ivWeixin = (ImageView) findViewById(R.id.iv_weixin);
        ivWeixin.setOnClickListener(mWeChatLoginClickListener);
        tvMore = (TextView) findViewById(R.id.tv_more);
        tvRegister = (TextView) findViewById(R.id.tv_register);
        tvForgetPassword = (TextView) findViewById(R.id.tv_forget);
        ivPwCancel = (ImageView) findViewById(R.id.iv_password_cancel);
        ivUserCancel = (ImageView) findViewById(R.id.iv_username_cancel);
        vSao = findViewById(R.id.saoyisao);
        mParent = findViewById(R.id.parent_rlayout);
        ViewGroup.LayoutParams params = mParent.getLayoutParams();
        params.height = AppUtil.getUnrealScreenHeightPx(this);
        mParent.setLayoutParams(params);
        tvForgetPassword.setOnClickListener(getForgetPasswordClickListener());
        vSao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(mContext, "Login_scan_it");
                Intent qrIntent = new Intent();
                qrIntent.setClass(LoginActivity.this, CaptureActivity.class);
                startActivityForResult(qrIntent, REQUEST_QR);
            }
        });
        tvMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(mContext, "Login_Select_the_school");
                QrSchoolActivity.start(mActivity);
            }
        });
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(mContext, "Login_Register_an_account");
                mActivity.app.mEngine.runNormalPlugin("RegisterActivity", mActivity, null);
            }
        });
        ivPwCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPassword.setText("");
            }
        });
        ivUserCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etUsername.setText("");
            }
        });
        initEdit();
        initThirdLoginBtns();
    }

    private void initEdit() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mTvLogin.setAlpha(1f);
                mTvLogin.setEnabled(true);
                if (etUsername.getText().length() == 0) {
                    ivUserCancel.setVisibility(View.INVISIBLE);
                    mTvLogin.setAlpha(0.6f);
                    mTvLogin.setEnabled(false);
                } else {
                    ivUserCancel.setVisibility(View.VISIBLE);
                }
                if (etPassword.getText().length() == 0) {
                    ivPwCancel.setVisibility(View.INVISIBLE);
                    mTvLogin.setAlpha(0.6f);
                    mTvLogin.setEnabled(false);
                } else {
                    ivPwCancel.setVisibility(View.VISIBLE);
                }
            }
        };
        etPassword.addTextChangedListener(watcher);
        etUsername.addTextChangedListener(watcher);
    }

    private void initThirdLoginBtns() {
        List<String> types = ThirdPartyLogin.getInstance(mContext).getLoginTypes();
        for (String type : types) {
            if ("QQ".equals(type)) {
                ivQQ.setVisibility(View.VISIBLE);
            } else if ("Wechat".equals(type)) {
                ivWeixin.setVisibility(View.VISIBLE);
            } else if ("SinaWeibo".equals(type)) {
                ivWeibo.setVisibility(View.VISIBLE);
            }
        }
    }

    private View.OnClickListener getForgetPasswordClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(mContext, "Forgot_your_password");
                mActivity.app.mEngine.runNormalPlugin("ForgetPasswordActivity", mContext, null);
            }
        };
    }

    public static void startLogin(Activity activity) {
        synchronized (activity) {
            if (isRun) {
                return;
            }
            Intent intent = new Intent();
            intent.setClass(activity, LoginActivity.class);
            activity.startActivityForResult(intent, TYPE_LOGIN);
        }
    }

    private void login() {
        RequestUrl requestUrl = mActivity.app.bindUrl(Const.LOGIN, false);
        Map<String, String> params = requestUrl.getParams();
        params.put("_username", etUsername.getText().toString().trim());
        if (SchoolUtil.checkEncryptVersion(app.schoolVersion, getString(R.string.encrypt_version))) {
            params.put("encrypt_password", XXTEA.encryptToBase64String(etPassword.getText().toString(), app.domain));
        } else {
            params.put("_password", etPassword.getText().toString());
        }

        final LoadDialog loadDialog = LoadDialog.create(this);
        loadDialog.show();
        mActivity.ajaxPost(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                UserResult userResult = mActivity.parseJsonValue(response, new TypeToken<UserResult>() {
                });
                loadDialog.dismiss();
                if (userResult != null && userResult.user != null) {
                    app.saveToken(userResult);
                    setResult(LoginActivity.OK);
                    SimpleDateFormat nowfmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    String entertime = nowfmt.format(date);
                    saveEnterSchool(app.defaultSchool.name, entertime, "登录账号：" + app.loginUser.nickname, app.domain);
                    app.sendMessage(Const.LOGIN_SUCCESS, null);
                    new IMServiceProvider(getBaseContext()).bindServer(userResult.user.id, userResult.user.nickname);
                    MessageEngine.getInstance().sendMsg(Const.LOGIN_SUCCESS, null);
                    MessageEngine.getInstance().sendMsg(Const.REFRESH_MY_FRAGMENT, null);
                    mTvLogin.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mActivity.finish();
                        }
                    }, 500);
                } else {
                    if (!TextUtils.isEmpty(response)) {
                        CommonUtil.longToast(mContext, response);
                    } else {
                        CommonUtil.longToast(mContext, getResources().getString(R.string.user_not_exist));
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadDialog.dismiss();
                CommonUtil.longToast(mContext, getResources().getString(R.string.request_fail_text));
            }
        });
    }

    private View.OnClickListener mLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (TextUtils.isEmpty(username)) {
                etUsername.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.requestFocus();
                return;
            }
            login();
        }
    };

    private void loginByPlatform(String type) {
        final OpenLoginUtil openLoginUtil = OpenLoginUtil.getUtil(mActivity);
        openLoginUtil.setLoginHandler(new NormalCallback<UserResult>() {
            @Override
            public void success(UserResult obj) {
                mActivity.finish();
            }
        });

        openLoginUtil.login(type).then(new PromiseCallback<String[]>() {
            @Override
            public Promise invoke(String[] obj) {
                openLoginUtil.bindOpenUser(LoginActivity.this, obj);
                return null;
            }
        });
    }

    private View.OnClickListener mWeiboLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MobclickAgent.onEvent(mContext, "Login_weib_login");
            loginByPlatform("SinaWeibo");
        }
    };

    private View.OnClickListener mQQLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MobclickAgent.onEvent(mContext, "Login_qq_login");
            loginByPlatform("QQ");
        }
    };


    private View.OnClickListener mWeChatLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MobclickAgent.onEvent(mContext, "Login_weixin_login");
            loginByPlatform("Wechat");
        }
    };

    private View.OnClickListener mMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mActivity.app.mEngine.runNormalPlugin("SettingActivity", mContext, null);
        }
    };

    public void saveEnterSchool(String schoolname, String entertime, String loginname, String schoolhost) {
        Map map = new HashMap();
        String lable = new String();
        lable = schoolname.substring(0, 2);
        map.put("lable", lable);
        map.put("schoolname", schoolname);
        map.put("entertime", entertime);
        map.put("loginname", loginname);
        map.put("schoolhost", schoolhost);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (loadEnterSchool(EnterSchool) != null) {
            list = loadEnterSchool(EnterSchool);
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get("schoolhost").toString().equals(map.get("schoolhost"))) {
                list.remove(i);
                i--;
            }
        }
        list.add(map);
        if (list.size() > 4) {
            list.remove(0);
        }
        JSONArray mJsonArray;
        mJsonArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> itemMap = list.get(i);
            Iterator<Map.Entry<String, Object>> iterator = itemMap.entrySet().iterator();

            JSONObject object = new JSONObject();

            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                try {
                    object.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) {

                }
            }
            mJsonArray.put(object);
        }

        SharedPreferences sp = mContext.getSharedPreferences("EnterSchool", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(EnterSchool, mJsonArray.toString());
        editor.commit();
    }

    private List<Map<String, Object>> loadEnterSchool(String fileName) {
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        SharedPreferences sp = mContext.getSharedPreferences("EnterSchool", Context.MODE_PRIVATE);
        String result = sp.getString(EnterSchool, "");
        try {
            JSONArray array = new JSONArray(result);
            for (int i = 0; i < array.length(); i++) {
                JSONObject itemObject = array.getJSONObject(i);
                Map<String, Object> itemMap = new HashMap<String, Object>();
                JSONArray names = itemObject.names();
                if (names != null) {
                    for (int j = 0; j < names.length(); j++) {
                        String name = names.getString(j);
                        String value = itemObject.getString(name);
                        itemMap.put(name, value);
                    }
                }
                datas.add(itemMap);
            }
        } catch (JSONException e) {

        }

        return datas;
    }

    @Override
    public void invoke(WidgetMessage message) {
        switch (message.type.type) {
            case FIND_PASSWORD_ACCOUNT:
                etUsername.setText(message.data.getString(FindPasswordByPhoneFragment.FIND_PASSWORD_USERNAME));
                break;
        }
    }

    @Override
    public MessageType[] getMsgTypes() {
        return new MessageType[]{new MessageType(FIND_PASSWORD_ACCOUNT)};
    }

    @Override
    public void finish() {
        super.finish();
        if (app.loginUser == null) {
            setResult(DefaultPageActivity.LOGIN_CANCEL);
        }
        overridePendingTransition(R.anim.none, R.anim.up_to_down);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRun = false;
    }
}