package com.example.datalibrary.model;

/**
 * 二维码
 */
public class QR {
    private int id;
    /**
     * 人名
     */
    private String qrName = "";
    /**
     * 卡号
     */
    private String qrCard = "";
    /**
     * 楼层号
     */
    private String qrfloor = "";
    private long ctime;
    private long updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQrName() {
        return qrName;
    }

    public void setQrName(String qrName) {
        this.qrName = qrName;
    }

    public String getQrCard() {
        return qrCard;
    }

    public void setQrCard(String qrCard) {
        this.qrCard = qrCard;
    }

    public String getQrfloor() {
        return qrfloor;
    }

    public void setQrfloor(String qrfloor) {
        this.qrfloor = qrfloor;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
