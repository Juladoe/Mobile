package com.edusoho.kuozhi.v3.model.provider;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.ReceiverInfo;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.listener.IMConnectStatusListener;
import com.edusoho.kuozhi.imserver.listener.IMMessageReceiver;
import com.edusoho.kuozhi.imserver.util.IMConnectStatus;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.NotificationProvider;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.service.message.CommandFactory;
import com.edusoho.kuozhi.v3.util.AppUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by 菊 on 2016/4/25.
 */
public class IMServiceProvider extends ModelProvider {

    private int mClientId;
    private String mClientName;

    public IMServiceProvider(Context context) {
        super(context);
    }

    private void setClientInfo(int clientId, String clientName) {
        this.mClientId = clientId;
        this.mClientName = clientName;
        IMClient.getClient().setClientInfo(clientId, clientName);
        IMClient.getClient().setIMDataBase(String.format("%s_%d", getDomain(), clientId));
    }

    public void unBindServer() {
        getNotificationProvider().cancelAllNotification();
        IMClient.getClient().destory();
    }

    public synchronized void reConnectServer(int clientId, String clientName) {
        setClientInfo(clientId, clientName);
        if (!getAppSettingProvider().getAppConfig().isEnableIMChat) {
            IMClient.getClient().setIMConnectStatus(IMConnectStatus.NO_READY);
            return;
        }
        int status = IMClient.getClient().getIMConnectStatus();
        if (status == IMConnectStatus.NO_READY || status == IMConnectStatus.ERROR) {
            connectServer(clientId, clientName, null);
            return;
        }

        if (status == IMConnectStatus.OPEN) {
            IMClient.getClient().sendCmd("requestOfflineMsg");
        }
    }

    private void connectServer(int clientId, String clientName, String[] ignoreServers) {
        IMClient.getClient().setIMConnectStatus(IMConnectStatus.CONNECTING);
        new SystemProvider(mContext).getImServerHosts(ignoreServers).success(new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap hostMap) {
                Log.d("IMServiceProvider", "init im service" + hostMap.size());
                if (hostMap == null
                        || hostMap.isEmpty()
                        || hostMap.values().isEmpty()
                        || hostMap.keySet().isEmpty()) {
                    errorBindImServer();
                    return;
                }
                successBindImserver(hostMap);
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                obj.printStackTrace();
                errorBindImServer();
            }
        });
    }

    private void successBindImserver(LinkedHashMap hostMap) {
        IMClient.getClient().start(
                new ArrayList(hostMap.keySet()),
                new ArrayList(hostMap.values())
        );

        IMClient.getClient().removeGlobalIMMessageReceiver();
        IMClient.getClient().removeGlobalIMConnectStatusListener();
        IMClient.getClient().addGlobalConnectStatusListener(getIMConnectStatusListener());
        IMClient.getClient().addGlobalIMMessageReceiver(new IMMessageReceiver() {
            @Override
            public boolean onReceiver(MessageEntity msg) {
                handlerMessage(false, this, msg);
                return false;
            }

            @Override
            public void onSuccess(MessageEntity messageEntity) {
                MessageBody messageBody = new MessageBody(messageEntity);
                if (messageBody == null) {
                    return;
                }
                updateMessageStatus(messageBody);
            }

            @Override
            public boolean onOfflineMsgReceiver(List<MessageEntity> messageEntities) {
                for (MessageEntity messageEntity : messageEntities) {
                    handlerMessage(true, this, messageEntity);
                }
                return false;
            }

            @Override
            public ReceiverInfo getType() {
                return new ReceiverInfo(Destination.GLOBAL, "1");
            }
        });
    }

    private IMConnectStatusListener getIMConnectStatusListener() {
        return new IMConnectStatusListener() {
            @Override
            public void onError() {
            }

            @Override
            public void onClose() {
            }

            @Override
            public void onConnect() {
            }

            @Override
            public void onOpen() {
            }

            @Override
            public void onInvalid(String[] ig) {
                reBindServer(mClientId, mClientName, ig);
            }
        };
    }

    private void errorBindImServer() {
        Log.d("IMServiceProvider", "bindServer error");
        IMClient.getClient().setIMConnectStatus(IMConnectStatus.ERROR);
    }

    public synchronized void reBindServer(int clientId, String clientName, String[] ignoreServers) {
        Log.d("IMServiceProvider", "rebind server");
        unBindServer();
        setClientInfo(clientId, clientName);
        if (!getAppSettingProvider().getAppConfig().isEnableIMChat) {
            IMClient.getClient().setIMConnectStatus(IMConnectStatus.ERROR);
            return;
        }
        int status = IMClient.getClient().getIMConnectStatus();
        Log.d("IMServiceProvider", "status:" + status);
        if (status == IMConnectStatus.OPEN || status == IMConnectStatus.CONNECTING) {
            return;
        }
        connectServer(clientId, clientName, ignoreServers);
        Log.d("IMServiceProvider", "IMService start ready");
    }

    public void bindServer(int clientId, String clientName) {
        reBindServer(clientId, clientName, null);
    }

    protected void updateMessageStatus(MessageBody messageBody) {
        ContentValues cv = new ContentValues();
        cv.put("status", MessageEntity.StatusType.SUCCESS);
        IMClient.getClient().getMessageManager().updateMessageFieldByUid(messageBody.getMessageId(), cv);
    }

    private void handlerMessage(boolean isOfflineMsg, IMMessageReceiver receiver, MessageEntity messageEntity) {
        MessageBody messageBody = new MessageBody(messageEntity);
        if (messageBody == null) {
            return;
        }
        CommandFactory.create(mContext, isOfflineMsg ? "offlineMsg" : messageEntity.getCmd(), receiver, messageBody).invoke();
    }

    protected NotificationProvider getNotificationProvider() {
        return FactoryManager.getInstance().create(NotificationProvider.class);
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }
}
