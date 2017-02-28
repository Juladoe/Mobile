package com.edusoho.kuozhi.imserver.util;

import java.util.Arrays;

/**
 * Created by 菊 on 2016/4/29.
 */
public class MapEntity {

    private int DEFAULT_COUNT = 8;
    private float RESIZE_COUNT = 1.5f;

    private Object[] keys;
    private Object[] values;
    private int keyIndex;
    private int valueIndex;

    public MapEntity()
    {
        this.keys = new Object[DEFAULT_COUNT];
        this.values = new Object[DEFAULT_COUNT];
        this.keyIndex = 0;
        this.valueIndex = 0;
    }

    protected void resize() {
        int newSize = (int) (keys.length * RESIZE_COUNT);
        keys = Arrays.copyOf(keys, newSize);
        values = Arrays.copyOf(values, newSize);
    }

    public void put(Object key, Object value) {
        if (keyIndex >= keys.length || valueIndex >= values.length) {
            resize();
        }

        keys[++keyIndex] = key;
        values[++valueIndex] = value;
    }

    public int getCount() {
        return keys.length;
    }

    public Object getKey(int index) {
        if (index >= keyIndex) {
            return null;
        }
        return keys[index];
    }

    public Object getValue(int index) {
        if (index >= valueIndex) {
            return null;
        }
        return values[index];
    }
}
