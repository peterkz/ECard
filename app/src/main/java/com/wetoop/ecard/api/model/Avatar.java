package com.wetoop.ecard.api.model;

import java.io.Serializable;

/**
 * @author Parck.
 * @date 2017/12/19.
 * @desc
 */

public class Avatar implements Serializable {

    private static final long serialVersionUID = 5909984261274252827L;

    private String value;
    private Boolean update;

    public Avatar(String value, Boolean update) {
        this.value = value;
        this.update = update;
    }

    public Avatar() {
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
