package com.example.account_book.util;

import android.util.Log;

import com.example.account_book.Account;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HttpUtils {

    private static final String TAG = "HttpUtils";
    /**
     * 服务器端测试用URL
     */
    public static final String BASE_URL = "your_url_here";

    public static final String ADD_ACCOUNT = "addAccount";
    public static final String DELETE_ACCOUNT = "deleteAccount/";
    public static final String UPDATE_ACCOUNT = "updateAccount/";
    public static final String QUERY_ALL_ACCOUNT = "queryAllAccount/";
    public static final String QUERY_SPECIFY_ACCOUNT = "queryAccount/";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    public static void sendRequestGetAsy(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + address)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static String sendRequestGetSyn(String address){
        String str = "";
        try{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(BASE_URL + address)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() & response.body() != null){
                str = response.body().string();
            }else {
                str = "error";
            }
        }catch (Exception e){
            Log.d(TAG, "sendRequestGetSyn: " + e.toString());
        }
        return str;
    }

    public static void addNewAccount(Account account, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("content", account.getContent())
                .add("number", String.format(Locale.CHINA, "%.2f", account.getNumber()))
                .add("person", account.getPerson())
                .add("createTime", account.getCreateTime())
                .build();
        Request request = new Request.Builder()
                .url(BASE_URL + ADD_ACCOUNT)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void updateAccount(Account account, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("account_id", String.format(Locale.CHINA, "%d", account.getId()))
                .add("content", account.getContent())
                .add("number", String.format(Locale.CHINA, "%.2f", account.getNumber()))
                .add("person", account.getPerson())
                .add("createTime", account.getCreateTime())
                .build();
        Request request = new Request.Builder()
                .url(BASE_URL + UPDATE_ACCOUNT)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static List<Account> parseAllAccount(String jsonData){
        //使得可以解析Date型变量
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        return gson.fromJson(jsonData, new TypeToken<List<Account>>(){}.getType());
    }

    public static Account parseAccountDetail(String jsonData){
        //使得可以解析Date型变量
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        return gson.fromJson(jsonData, Account.class);
    }
}
