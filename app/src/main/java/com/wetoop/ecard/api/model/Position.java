package com.wetoop.ecard.api.model;

import java.io.Serializable;

/**
 * @author Parck.
 * @date 2017/12/19.
 * @desc
 */

public class Position implements Serializable{

    private static final long serialVersionUID = -7612077144675260867L;

    private String value;
    private Boolean update;

    public Position(String value, Boolean update) {
        this.value = value;
        this.update = update;
    }

    public Position() {
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
