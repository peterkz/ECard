package com.wetoop.ecard.api.model;

import java.io.Serializable;

/**
 * @author Parck.
 * @date 2017/12/19.
 * @desc
 */

public class Name implements Serializable {

    private static final long serialVersionUID = -8031821296792765549L;


    private String value;
    private Boolean update;

    public Name(String value, Boolean update) {
        this.value = value;
        this.update = update;
    }

    public Name() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean isUpdate() {
        return update;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }
}
