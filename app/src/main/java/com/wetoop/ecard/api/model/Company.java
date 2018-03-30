package com.wetoop.ecard.api.model;

import java.io.Serializable;

/**
 * @author Parck.
 * @date 2017/12/19.
 * @desc
 */

public class Company implements Serializable{

    private static final long serialVersionUID = 9136256983194720960L;

    private String value;
    private Boolean update;

    public Company(String value, Boolean update) {
        this.value = value;
        this.update = update;
    }

    public Company() {
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
