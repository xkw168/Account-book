package com.example.account_book.activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.account_book.DailyAccount;
import com.example.account_book.R;
import com.example.account_book.util.DBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class SummaryActivity extends AppCompatActivity {

    private static final String TAG = SummaryActivity.class.getSimpleName();

    private HashMap<String, Double> result = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_summary);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("金额统计");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tv_total = (TextView)findViewById(R.id.summary_total);
        TextView tv_separate = (TextView)findViewById(R.id.summary_separate);

        DBHelper db = new DBHelper(SummaryActivity.this);
        ArrayList<DailyAccount> dailyAccounts = (ArrayList<DailyAccount>)db.queryDailyAccount();

        if (dailyAccounts != null){
            if (!dailyAccounts.isEmpty()){

                loadPersonList();

                double sum = 0.0;
                double average = 0.0;

                for (DailyAccount dailyAccount : dailyAccounts) {
                    String key = dailyAccount.getCurrencyType();
                    if (result.containsKey(key)){
                        result.replace(key, Objects.requireNonNull(result.get(key)) + dailyAccount.getAmount());
                    }else {
                        result.put(key, dailyAccount.getAmount());
                    }
                    sum += dailyAccount.getAmount();
                }

                average = sum / result.keySet().size();

                StringBuilder separate = new StringBuilder();

                for (String key : result.keySet()) {
                    separate
                            .append(key)
                            .append("\n")
                            .append("已付:")
                            .append(result.get(key))
                            .append("（")
                            .append(String.format(Locale.CHINA, "%.2f", Objects.requireNonNull(result.get(key)) - average))
                            .append("）")
                            .append("\n\n");
                }

                String total = "总计:" +
                        sum +
                        "\n" +
                        "人均:" +
                        average;
                tv_total.setText(total);
                tv_separate.setText(separate.toString());
            }
        }else {
            tv_total.setText("无数据，请尝试添加你的第一条账单数据～");
            tv_separate.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void loadPersonList() {
        SharedPreferences preferences = getSharedPreferences("journey_data", MODE_PRIVATE);
        int cnt = preferences.getInt("count", 0);
        Log.e(TAG, "loadData: " + cnt);
        for (int i = 0; i < cnt; i++){
            result.put(preferences.getString(String.format("person%s", i), ""), 0.0);
        }
    }
}
