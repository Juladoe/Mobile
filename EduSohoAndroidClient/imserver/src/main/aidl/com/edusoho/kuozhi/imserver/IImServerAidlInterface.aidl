// IImServerAidlInterface.aidl
package com.edusoho.kuozhi.imserver;

import com.edusoho.kuozhi.imserver.SendEntity;

// Declare any non-default types here with import statements

interface IImServerAidlInterface {

    void send(in SendEntity sendEntity);

    int getIMStatus();

    void closeIMServer();

    void joinConversation(String token, String convNo);

    void requestConnect();

    void requestOfflineMsg();

    void start(int clientId, String clientName, inout String[] ignoreNosList, inout String[] hostList);
}
