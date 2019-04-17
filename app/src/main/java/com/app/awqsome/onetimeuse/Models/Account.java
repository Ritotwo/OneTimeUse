package com.app.awqsome.onetimeuse.Models;

import java.io.Serializable;
import java.util.Arrays;

public class Account implements Serializable {

    String label, user;
    byte[] encryptedData, encryptediv;
    long id;

    public Account() {
    }

    public Account(long id, String label, String user, byte[] encryptedData, byte[] encryptediv) {
        this.id = id;
        this.label = label;
        this.user = user;
        this.encryptedData = encryptedData;
        this.encryptediv = encryptediv;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;

    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }

    public byte[] getEncryptediv() {
        return encryptediv;
    }

    public void setEncryptediv(byte[] encryptediv) {
        this.encryptediv = encryptediv;
    }

    public long getMyId() {
        return id;
    }

    public void setMyId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Account{" +
                "label='" + label + '\'' +
                ", user='" + user + '\'' +
                ", encryptedData=" + Arrays.toString(encryptedData) +
                ", encryptediv=" + Arrays.toString(encryptediv) +
                ", id=" + id +
                '}';
    }
}
