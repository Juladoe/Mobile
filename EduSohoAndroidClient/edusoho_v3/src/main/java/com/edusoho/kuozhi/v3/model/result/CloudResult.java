package com.edusoho.kuozhi.v3.model.result;

/**
 * Created by howzhi on 14-5-25.
 */
public class CloudResult {
    public int id;
    public String result;

    public boolean getResult() {
        return "success".equals(this.result);
    }
}
