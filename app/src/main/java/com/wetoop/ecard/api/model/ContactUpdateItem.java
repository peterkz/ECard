package com.wetoop.ecard.api.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Parck.
 * @date 2017/12/19.
 * @desc
 */

public class ContactUpdateItem implements Serializable {

    private static final long serialVersionUID = 663482120415161607L;

    private String id;
    private Date date_created = new Date();
    private String moth;
    private String action;
    private String old;
    private String content;
    private String card_id;

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOld() {
        return old;
    }

    public void setOld(String old) {
        this.old = old;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCard_id() {
        return card_id;
    }

    public void setCard_id(String card_id) {
        this.card_id = card_id;
    }

    public String getMoth() {
        return moth;
    }

    public void setMoth(String moth) {
        this.moth = moth;
    }
}
