package com.example.account_book;

import com.example.account_book.util.TimeUtils;

import java.io.Serializable;

public class DailyAccount implements Serializable, Comparable<DailyAccount>{

    private int id;
    private String content;
    private double amount;
    private String currencyType;
    private String createTime;
    private boolean isIncome;

    public DailyAccount(){
        this.content = "";
        this.amount = 0.0;
        this.currencyType = "";
        this.createTime = TimeUtils.now();
        this.isIncome = false;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public boolean isIncome(){
        return this.isIncome;
    }

    public void setIncome(boolean isIncome){
        this.isIncome = isIncome;
    }

    @Override
    public String toString() {
        return "Time: " + this.createTime + "\n" +
                "Content: " + this.content + "\n" +
                "Amount: " + this.amount + "\n" +
                "Type: " + this.currencyType;
    }

    public String getSimpleString() {
        return this.createTime + this.content + this.amount + this.currencyType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DailyAccount){
            DailyAccount dailyAccount = (DailyAccount)obj;
            return this.createTime.equals(dailyAccount.getCreateTime());
        }
        return false;
    }

    @Override
    public int compareTo(DailyAccount dailyAccount) {
        return this.createTime.compareTo(dailyAccount.createTime);
    }
}
