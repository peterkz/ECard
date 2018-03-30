package com.wetoop.ecard.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Parck.
 * @date 2017/12/19.
 * @desc
 */

public class ContactUpdate implements Serializable {

    private static final long serialVersionUID = -31302124542363358L;

    private String uuid;
    private String user_id;
    private String card_id;
    private Avatar avatar;
    private Name name;
    private Position position;
    private Company company;
    private List<Phone> phones = new ArrayList<>();
    private List<Email> emails = new ArrayList<>();
    private List<Address> addresses = new ArrayList<>();
    private List<Url> urls = new ArrayList<>();
    private List<Custom> customs = new ArrayList<>();
    private List<Day> days = new ArrayList<>();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Url> getUrls() {
        return urls;
    }

    public void setUrls(List<Url> urls) {
        this.urls = urls;
    }

    public List<Custom> getCustoms() {
        return customs;
    }

    public void setCustoms(List<Custom> customs) {
        this.customs = customs;
    }

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }
}
