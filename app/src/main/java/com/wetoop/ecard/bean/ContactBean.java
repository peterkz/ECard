package com.wetoop.ecard.bean;


import java.io.Serializable;

/**
 * Created by User on 2017/9/8.
 */

public class ContactBean implements Serializable {

    private static final long serialVersionUID = 8405137615426161080L;
    
    private CardBean card;
    private String spellName;
    private String spellFirst;
    private String userName;//临时放上去的，在“新的联系人里使用”，之后会删掉
    private String contactsMessage;
    private String RecordsStateType;

    public CardBean getCard() {
        return card;
    }

    public void setCard(CardBean card) {
        this.card = card;
    }

    public String getSpellName() {
        return spellName;
    }

    public void setSpellName(String spellName) {
        this.spellName = spellName;
    }

    public String getSpellFirst() {
        return spellFirst;
    }

    public void setSpellFirst(String spellFirst) {
        this.spellFirst = spellFirst;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContactsMessage() {
        return contactsMessage;
    }

    public void setContactsMessage(String contactsMessage) {
        this.contactsMessage = contactsMessage;
    }

    public String getRecordsStateType() {
        return RecordsStateType;
    }

    public void setRecordsStateType(String recordsStateType) {
        RecordsStateType = recordsStateType;
    }
}
