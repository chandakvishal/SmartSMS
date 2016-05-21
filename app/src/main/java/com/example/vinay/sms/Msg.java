package com.example.vinay.sms;

public class Msg {
    private String email, message;
    private boolean leMien;
    private String attach;
    private String cle;
    private  String teleAttach;
    private  String heure;
    private  String date;

    public Msg(String cle,String email, String message,String attach,boolean leMien,String teleAttach,String heure,String date) {
        this.email = email;
        this.message = message;
        this.leMien = leMien;
        this.attach = attach;
        this.cle=cle;
        this.teleAttach = teleAttach;
        this.heure = heure;
        this.date=date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getleMien() {
        return leMien;
    }

    public void setleMien(boolean leMien) {
        this.leMien = leMien;
    }
    public String getAttach() {
        return attach ;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }
    public String getCle() {
        return cle ;
    }

    public void setCle(String cle) {
        this.cle = cle;
    }


    public void setTeleAttach(String teleAttach) {
        this.teleAttach = teleAttach;
    }
    public String getTeleAttach() {
        return teleAttach ;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }
    public String getHeure() {
        return heure ;
    }
    public String getDate() {
        return date ;
    }

    public void setDate(String date) {
        this.date = date;
    }

}