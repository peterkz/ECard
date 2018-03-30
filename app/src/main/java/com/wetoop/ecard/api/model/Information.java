package com.wetoop.ecard.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Parck.
 * @date 2017/11/1.
 * @desc
 */

public class Information implements Serializable {

    private static final long serialVersionUID = 168116901525276296L;

    private Date dateUpdated;
    private String avatar;
    private String name;
    private String company;
    private String department;
    private String position;
    private boolean privacy = true;
    private String user_id;
    private String card_id;
    private List<String> mine_id = new ArrayList<>();
    private String card_label;
    private String note;
    private int type;//0我的e卡名片，1联系人名片，2我创建的联系人名片

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    public boolean isPrivacy() {
        return privacy;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCard_id() {
        return card_id;
    }

    public void setCard_id(String card_id) {
        this.card_id = card_id;
    }

    public String getCard_label() {
        return card_label;
    }

    public List<String> getMine_id() {
        return mine_id;
    }

    public void setMine_id(List<String> mine_id) {
        this.mine_id = mine_id;
    }

    public void setCard_label(String card_label) {
        this.card_label = card_label;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
