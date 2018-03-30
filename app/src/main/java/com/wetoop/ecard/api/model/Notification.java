package com.wetoop.ecard.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * @author Parck.
 * @date 2017/10/30.
 * @desc
 */

public class Notification implements Serializable {

    private static final long serialVersionUID = -8550110742225774732L;

    private String message_id;
    private Date date_created = new Date();
    private Set<String> receiverUIDs;
    private String sender_id;
    private String receiver_id;
    private Message message;

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public Set<String> getReceiverUIDs() {
        return receiverUIDs;
    }

    public void setReceiverUIDs(Set<String> receiverUIDs) {
        this.receiverUIDs = receiverUIDs;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public static class Message implements Serializable {

        private static final long serialVersionUID = -5844395845734329823L;

        private int type;//
        private int state;//0未读状态、1已读状态
        private String card_id;
        private String content;
        private String name;
        private String avatar;
        private String mine_id;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public String getCard_id() {
            return card_id;
        }

        public void setCard_id(String card_id) {
            this.card_id = card_id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getMine_id() {
            return mine_id;
        }

        public void setMine_id(String mine_id) {
            this.mine_id = mine_id;
        }
    }
}