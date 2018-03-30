package com.wetoop.ecard.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Parck.
 * @date 2017/10/26.
 * @desc
 */

public abstract class AbsBean implements Serializable {

    private static final long serialVersionUID = 533194636485103680L;

    private Date dateCreated = new Date();
    private Date lastUpdated = new Date();

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
