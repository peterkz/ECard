package com.wetoop.ecard.api.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author Parck.
 * @date 2017/11/30.
 * @desc
 */

public class Feedback implements Serializable {

    private static final long serialVersionUID = -8612424611034520807L;

    private String id;
    private String describe;
    private List<String> image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public List<String> getImage() {
        return image;
    }

    public void setImage(List<String> image) {
        this.image = image;
    }
}
