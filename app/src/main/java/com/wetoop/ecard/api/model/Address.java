package com.wetoop.ecard.api.model;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * @author Parck.
 * @date 2017/11/1.
 * @desc
 */

public class Address implements Serializable {

    private static final long serialVersionUID = -2340441121567099626L;

    private String province = "";
    private String city = "";
    private String county = "";
    private String address = "";
    private String type = "";
    private Boolean update;
    private Status status;
    private String old;
    private int stateType;
    private String oldString;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(province)) sb.append(this.province).append("-");
        if (!TextUtils.isEmpty(city)) sb.append(this.city).append("-");
        if (!TextUtils.isEmpty(county)) sb.append(this.county).append("-");
        if (!TextUtils.isEmpty(address)) sb.append(this.address);
        return sb.toString();
    }

    public String county() {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(province)) sb.append(this.province).append("-");
        if (!TextUtils.isEmpty(city)) sb.append(this.city).append("-");
        if (!TextUtils.isEmpty(county)) sb.append(this.county);
        return sb.toString();
    }

    public Address searchAdr() {
        if (this.getProvince().contains("自治区"))
            this.setProvince(this.getProvince().substring(0, 2));
        return this;
    }
}
