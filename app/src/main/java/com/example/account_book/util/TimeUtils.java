package com.example.account_book.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    public static String now(){
        return new SimpleDateFormat(DATE_FORMAT, Locale.CHINA).format(new Date());
    }

    public static String today(){
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
    }

    public static int getCurrentMonth(){
        // since Calendar.Month starts from zero, plus 1 and return
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    public static int getCurrentYear(){
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static void main(String[] args){
        String a = now();
        System.out.print(a + "\n");
        System.out.print(a.substring(0, 10) + "\n");
        System.out.print(getCurrentMonth() + "\n");
        System.out.print(getCurrentYear() + "\n");
    }
}
