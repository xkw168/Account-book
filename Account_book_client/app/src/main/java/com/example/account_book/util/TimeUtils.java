package com.example.account_book.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    public static String now(){
        return new SimpleDateFormat(HttpUtils.DATE_FORMAT, Locale.CHINA).format(new Date());
    }

    public static int getCurrentMonth(){
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    public static int getCurrentYear(){
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static void main(String[] args){
        String a = now();
        System.out.print(a + "\n");
        System.out.print(a.substring(0, 10));
    }
}
