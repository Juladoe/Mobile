package com.edusoho.kuozhi.v3.model.result;

import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.sys.School;

import java.io.Serializable;

public class UserResult implements Serializable {
    public User user;
    public String token;
    public School site;

    public UserResult() {

    }

    public UserResult(User user, String token, School site) {
        this.user = user;
        this.token = token;
        this.site = site;
    }
}
