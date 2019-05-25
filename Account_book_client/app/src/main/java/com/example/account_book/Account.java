package com.example.account_book;

import com.example.account_book.util.TimeUtils;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Account implements Serializable, Comparable<Account>{

    @SerializedName("_account_id")
    private int id;
    @SerializedName("_content")
    private String content;
    @SerializedName("_number")
    private double number;
    @SerializedName("_person")
    private String person;
    @SerializedName("_create_time")
    private String createTime;

    public Account(){
        this.content = "";
        this.number = 0.0;
        this.person = "xkw";
        this.createTime = TimeUtils.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getNumber() {
        return number;
    }

    public void setNumber(double number) {
        this.number = number;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Time: " + this.createTime + "\n" +
                "Content: " + this.content + "\n" +
                "Number: " + this.number + "\n" +
                "Person: " + this.person;
    }

    public String getSimpleString() {
        return this.createTime + this.content + this.number + this.person;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Account){
            Account account = (Account)obj;
            return this.createTime.equals(account.getCreateTime());
        }
        return false;
    }

    @Override
    public int compareTo(Account account) {
        return this.createTime.compareTo(account.createTime);
    }
}
