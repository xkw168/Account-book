package com.example.account_book;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.account_book.util.RateUtil;

public class AccountBookApplication extends Application {

    private static final String TAG = AccountBookApplication.class.getSimpleName();

    private static Context context;
    private static String primaryCurrency;
    private static String secondaryCurrency;

    public static double USD_RMB = 6.9;
    public static double HKD_RMB = 0.8;

    @Override
    public void onCreate() {
        super.onCreate();

        AccountBookApplication.context = getApplicationContext();
        loadData();
        queryRate();
    }

    public static Context getContext() {
        return AccountBookApplication.context;
    }

    public static String getPrimaryCurrency(){
        return AccountBookApplication.primaryCurrency;
    }

    public static String getSecondaryCurrency(){
        return AccountBookApplication.secondaryCurrency;
    }

    public static void setPrimaryCurrency(String primary){
        AccountBookApplication.primaryCurrency = primary;
    }

    public static void setSecondaryCurrency(String secondary){
        AccountBookApplication.secondaryCurrency = secondary;
    }

    public void loadData(){
        SharedPreferences preferences = getSharedPreferences(ConstantValue.CACHE_DATA, MODE_PRIVATE);
        primaryCurrency = preferences.getString(ConstantValue.PRIMARY_CURRENCY, "");
        secondaryCurrency = preferences.getString(ConstantValue.SECONDARY_CURRENCY, "");
    }

    public void queryRate(){
        RateUtil.setListener((USD_RMB1, HKD_RMB1) -> {
            AccountBookApplication.USD_RMB = USD_RMB1 / 100;
            AccountBookApplication.HKD_RMB = HKD_RMB1 / 100;
        });
        RateUtil.queryRate();
    }
}
