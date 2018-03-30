package com.wetoop.ecard.api.model;

import java.io.Serializable;

/**
 * @author Parck.
 * @date 2017/12/14.
 * @desc
 */

public class AddressSearch implements Serializable {

    private static final long serialVersionUID = 7616550021674525758L;

    private String card_id;
    private String user_id;

    public AddressSearch() {
    }

    public AddressSearch(String cardID, String userID) {
        this.card_id = cardID;
        this.user_id = userID;
    }

    public String getCard_id() {
        return card_id;
    }

    public void setCard_id(String card_id) {
        this.card_id = card_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
