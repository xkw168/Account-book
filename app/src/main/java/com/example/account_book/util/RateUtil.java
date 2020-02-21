package com.example.account_book.util;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class RateUtil {

    private static final String TAG = RateUtil.class.getSimpleName();

    // query code for different currency type
    private static final String USD = "1316";
    private static final String HKD = "1315";

    private static FinishListener mListener;

    public interface FinishListener{
        void onFinishListener(double USD_RMB, double HKD_RMB);
    }

    public static void setListener(FinishListener mListener) {
        RateUtil.mListener = mListener;
    }

    public static void queryRate(){
        new Thread(() -> {
            try {
                Connection connection1 = Jsoup.connect("http://srh.bankofchina.com/search/whpj/search.jsp");
                // some query data
                connection1.data("erectDate", "");
                connection1.data("nothing", "");
                connection1.data("pjname", USD);
                // get the HTML
                Document doc1 = connection1.get();
                // locate to table
                Elements elements1 = doc1.select("div[class='BOC_main publish'] > table > tbody > tr").get(1).select("td");

                Connection connection2 = Jsoup.connect("http://srh.bankofchina.com/search/whpj/search.jsp");
                // some query data
                connection2.data("erectDate", "");
                connection2.data("nothing", "");
                connection2.data("pjname", HKD);
                // get the HTML
                Document doc2 = connection2.get();
                // locate to table
                Elements elements2 = doc2.select("div[class='BOC_main publish'] > table > tbody > tr").get(1).select("td");

                double USD_RMB = Double.valueOf(elements1.get(3).text());
                double HKD_RMB = Double.valueOf(elements2.get(3).text());

                if (mListener != null){
                    mListener.onFinishListener(USD_RMB, HKD_RMB);
                }
            }catch (IOException | IndexOutOfBoundsException e){
                Log.e(TAG, e.toString());
            }
        }).start();
    }

    public static void main(String[] args){
        setListener((USD_RMB, HKD_RMB) -> {
            System.out.print("USD - RMB: " + USD_RMB + "\n");
            System.out.print("HKD - RMB: " + HKD_RMB);
        });
        queryRate();
    }
}
