package com.wetoop.ecard.api.model;

import java.io.Serializable;

/**
 * @author Parck.
 * @date 2017/11/21.
 * @desc
 */

public class Location implements Serializable {

    private static final long serialVersionUID = -3222574494007484555L;

    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
