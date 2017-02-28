package com.edusoho.kuozhi.imserver.ui.entity;

import java.util.HashMap;

/**
 * Created by JesseHuang on 15/7/24.
 */
public class UpYunUploadResult {

    public String putUrl;
    public String getUrl;

    public String[] headers;

    public HashMap<String, String> getHeaders() {
        HashMap<String, String> hashMap = new HashMap<>();
        for (String header : headers) {
            int separator = header.indexOf(':');
            hashMap.put(header.substring(0, separator), header.substring(separator + 1));
        }
        return hashMap;
    }

    public String getPutUrl() {
        return putUrl;
    }

    public void setPutUrl(String putUrl) {
        this.putUrl = putUrl;
    }

    public String getGetUrl() {
        return getUrl;
    }

    public void setGetUrl(String getUrl) {
        this.getUrl = getUrl;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }
}
