package com.example.account_book;

import java.io.Serializable;

public class JourneyAccount extends DailyAccount implements Serializable{

    private int journeyId;
    private String person;

    public JourneyAccount(){
        super();
        person = "";
    }

    public JourneyAccount(int journeyId){
        super();
        this.journeyId = journeyId;
    }

    public String getPerson(){
        return person;
    }

    public void setPerson(String person){
        this.person = person;
    }

    public int getJourneyId(){
        return journeyId;
    }

    public void setJourneyId(int journeyId){
        this.journeyId = journeyId;
    }
}
