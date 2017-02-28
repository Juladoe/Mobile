package com.edusoho.kuozhi.v3.entity.register;

import java.io.Serializable;

/**
 * Created by JesseHuang on 2016/12/1.
 */

public class FindPasswordSmsCode implements Serializable {
    public String mobile;
    public String verified_token;
    public String img_code;
    public String status;
}
