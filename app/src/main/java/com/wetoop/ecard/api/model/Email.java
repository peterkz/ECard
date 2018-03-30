package com.wetoop.ecard.api.model;

import java.io.Serializable;

/**
 * @author Parck.
 * @date 2017/11/1.
 * @desc
 */

public class Email implements Serializable {

    private static final long serialVersionUID = -7721105487421729662L;

    private String email;
    private String type;
    private Boolean update;
    private Status status;
    private String old;
    private int stateType;
    private String oldString;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }

    public Boolean isUpdate() {
        return update;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOld() {
        return old;
    }

    public void setOld(String old) {
        this.old = old;
    }

    public int getStateType() {
        return stateType;
    }

    public void setStateType(int stateType) {
        this.stateType = stateType;
    }

    public String getOldString() {
        return oldString;
    }

    public void setOldString(String oldString) {
        this.oldString = oldString;
    }
}

