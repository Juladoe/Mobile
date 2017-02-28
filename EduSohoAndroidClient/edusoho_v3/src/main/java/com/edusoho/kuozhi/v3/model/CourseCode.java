package com.edusoho.kuozhi.v3.model;

/**
 * Created by howzhi on 14-9-25.
 */
public class CourseCode {
    public Code useable;
    public String message;
    public double afterAmount;
    public double decreaseAmount;

    public enum Code
    {
        yes, no
    }
}
