package com.edusoho.kuozhi.v3.model.base;

import com.edusoho.kuozhi.v3.entity.error.Error;

import java.util.List;

/**
 * Created by JesseHuang on 16/3/6.
 */
public class ApiResponse<T> {
    public Error error;
    public T data;
    public List<T> resources;
    public int start;
    public int limit;
    public int total;
}
