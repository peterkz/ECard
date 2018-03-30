package com.wetoop.ecard.api.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Parck.
 * @date 2017/11/21.
 * @desc
 */

public class Income implements Serializable {

    private static final long serialVersionUID = 8088735446290141027L;

    private String id;
    private Date date_created = new Date();
    private String card_id;
    private String avatar;
    private String name;
    private String content;
    private String user_id;
    private String mine_id;
    private int state;//状态，0(初始状态),1,2,3

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public String getCard_id() {
        return card_id;
    }

    public void setCard_id(String card_id) {
        this.card_id = card_id;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMine_id() {
        return mine_id;
    }

    public void setMine_id(String mine_id) {
        this.mine_id = mine_id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
