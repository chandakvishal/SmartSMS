package com.example.vinay.sms;

public class SMS {

    public String senderAddress;
    public String date;
    public String message;
    public String type;

    public SMS(String senderAddress, String date, String message, String type) {
        this.date = date;
        this.senderAddress = senderAddress;
        this.message = message;
        this.type = type;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getTitle() {
        return date;
    }

    public void setTitle(String title) {
        this.date = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}