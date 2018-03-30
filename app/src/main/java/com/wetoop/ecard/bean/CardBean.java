package com.wetoop.ecard.bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wetoop.ecard.api.model.Address;
import com.wetoop.ecard.api.model.Card;
import com.wetoop.ecard.api.model.Custom;
import com.wetoop.ecard.api.model.Day;
import com.wetoop.ecard.api.model.Email;
import com.wetoop.ecard.api.model.Information;
import com.wetoop.ecard.api.model.Phone;
import com.wetoop.ecard.api.model.Url;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Parck.
 * @date 2017/11/1.
 * @desc
 */

public class CardBean implements Serializable, Cloneable {

    private static final long serialVersionUID = -5410790376082796809L;

    private Information information;
    private List<Day> days;
    private List<Phone> phones;
    private List<Custom> customs;
    private List<Email> emails;
    private List<Url> urls;
    private List<Address> addresses;
    private boolean select;

    public Information getInformation() {
        return information;
    }

    public void setInformation(Information information) {
        this.information = information;
    }

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    public List<Custom> getCustoms() {
        return customs;
    }

    public void setCustoms(List<Custom> customs) {
        this.customs = customs;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public List<Url> getUrls() {
        return urls;
    }

    public void setUrls(List<Url> urls) {
        this.urls = urls;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public static CardBean fromModel(Card card) {
        CardBean bean = new CardBean();
        bean.setInformation(card.getInformation() != null && card.getInformation().size() == 1 ? card.getInformation().get(0) : new Information());
        if (card.getAddress() != null)
            bean.setAddresses(card.getAddress());
        if (card.getDays() != null)
            bean.setDays(card.getDays());
        if (card.getEmail() != null)
            bean.setEmails(card.getEmail());
        if (card.getPhone() != null)
            bean.setPhones(card.getPhone());
        if (card.getCustom() != null)
            bean.setCustoms(card.getCustom());
        if (card.getUrl() != null)
            bean.setUrls(card.getUrl());
        return bean;
    }

    public Card toModel() {
        Card card = new Card();
        card.setPhone(this.getPhones());
        ArrayList<Information> informations = new ArrayList<>();
        informations.add(this.getInformation());
        card.setInformation(informations);
        card.setAddress(this.getAddresses());
        card.setCustom(this.getCustoms());
        card.setDays(this.getDays());
        card.setEmail(this.getEmails());
        card.setUrl(this.getUrls());
        return card;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
