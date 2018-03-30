package com.wetoop.ecard.api.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Parck.
 * @date 2017/11/1.
 * @desc
 */

public class Day implements Serializable {

    private static final long serialVersionUID = 992321946603033370L;

    private Date date;
    private String type;
    private Boolean update;
    private Status status;
    private Date old;
    private int stateType;
    private String oldString;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean isUpdate() {
        return update;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getOld() {
        return old;
    }

    public void setOld(Date old) {
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
