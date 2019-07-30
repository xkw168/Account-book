package com.example.account_book;

import com.example.account_book.util.TimeUtils;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Journey implements Serializable, Comparable<Journey>{

    private int id;
    private String startDate;
    private String endDate;
    private String destination;
    private double totalAmount;
    private String members;

    public Journey(){
        this.destination = "";
        this.totalAmount = 0.0;
        this.members = "";
        this.startDate = TimeUtils.today();
        this.endDate = TimeUtils.today();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getMember() {
        return members;
    }

    public void setMember(String members) {
        this.members = members;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "Destination: " + this.destination + "\n" +
                "Start Date: " + this.startDate + "\n" +
                "Member: " + this.getMember() + "\n" +
                "Total Amount: " + this.getTotalAmount();
    }

    public String getSimpleString() {
        return this.destination + this.startDate + this.members + this.totalAmount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Journey){
            Journey journey = (Journey)obj;
            return this.startDate.equals(journey.getStartDate()) && this.destination.equals(journey.getDestination());
        }
        return false;
    }

    @Override
    public int compareTo(Journey journey) {
        return this.startDate.compareTo(journey.getStartDate());
    }
}
