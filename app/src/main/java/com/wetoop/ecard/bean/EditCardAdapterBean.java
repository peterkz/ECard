package com.wetoop.ecard.bean;

import java.io.Serializable;

/**
 * Created by User on 2017/11/29.
 */

public class EditCardAdapterBean implements Serializable {
    private static final long serialVersionUID = -1103002064676454611L;

    private String beanType;
    private String value;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getBeanType() {
        return beanType;
    }

    public void setBeanType(String beanType) {
        this.beanType = beanType;
    }

}
