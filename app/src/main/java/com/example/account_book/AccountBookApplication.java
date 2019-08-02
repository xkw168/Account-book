package com.example.account_book;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class AccountBookApplication extends Application {

    private static final String TAG = AccountBookApplication.class.getSimpleName();

    private static Context context;
    private static String primaryCurrency;
    private static String secondaryCurrency;

    @Override
    public void onCreate() {
        super.onCreate();

        AccountBookApplication.context = getApplicationContext();
        loadData();
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
}
