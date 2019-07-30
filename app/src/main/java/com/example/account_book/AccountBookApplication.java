package com.example.account_book;

import android.app.Application;
import android.content.Context;

public class AccountBookApplication extends Application {

    private static final String TAG = AccountBookApplication.class.getSimpleName();

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        AccountBookApplication.context = getApplicationContext();
    }

    public static Context getContext() {
        return AccountBookApplication.context;
    }

}
