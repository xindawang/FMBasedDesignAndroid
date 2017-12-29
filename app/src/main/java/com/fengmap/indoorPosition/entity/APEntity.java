package com.fengmap.indoorPosition.entity;

import java.util.Date;

/**
 * Created by ACER on 2017/12/17.
 */

public class APEntity {
    private String apName;
    private Integer apStrength;
    private Date timestamp;

    public String getApName() {
        return apName;
    }

    public void setApName(String apName) {
        this.apName = apName;
    }

    public Integer getApStrength() {
        return apStrength;
    }

    public void setApStrength(Integer apStrength) {
        this.apStrength = apStrength;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = new Date(timestamp);
    }
}
