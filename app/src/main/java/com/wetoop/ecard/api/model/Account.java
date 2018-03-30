package com.wetoop.ecard.api.model;

import java.io.Serializable;

/**
 * Created by User on 2017/12/15.
 */

public class Account implements Serializable {
    private static final long serialVersionUID = 113377654724977045L;

    private String phone;
    private String user_id;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
