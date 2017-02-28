package com.edusoho.kuozhi.v3.util.json;

/**
 * Created by suju on 16/11/30.
 */

public interface GsonEnum<E> {

    String serialize();

    E deserialize(String jsonEnum);
}
