package com.edusoho.kuozhi.v3.model.bal.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 用于新Api解析
 * Created by JesseHuang on 16/1/22.
 */
public class ModelDecor {
    private Gson gson;
    public static ModelDecor mModelDecor;

    private ModelDecor() {
        if (gson == null) {
            gson = new Gson();
        }
    }

    public static ModelDecor getInstance() {
        if (mModelDecor == null) {
            mModelDecor = new ModelDecor();
        }
        return mModelDecor;
    }

    public <T> T decor(String json, TypeToken<T> typeToken) {
        try {
            return gson.fromJson(
                    json, typeToken.getType());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
