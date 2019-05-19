package com.example.account_book;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.account_book.util.DBHelper;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class SummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_summary);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("金额统计");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DBHelper db = new DBHelper(SummaryActivity.this);
        ArrayList<Account> accounts = (ArrayList<Account>)db.queryAllAccount();

        HashMap<String, Double> result = new HashMap<>();

        double sum = 0.0;
        double average = 0.0;

        for (Account account : accounts) {
            String key = account.getPerson();
            if (result.containsKey(key)){
                result.replace(key, result.get(key) + account.getNumber());
            }else {
                result.put(key, account.getNumber());
            }
            sum += account.getNumber();
        }

        average = sum / result.keySet().size();

        StringBuilder total = new StringBuilder()
                .append("总计:")
                .append(sum)
                .append("\n")
                .append("人均:")
                .append(average);

        StringBuilder separate = new StringBuilder();

        for (String key : result.keySet()) {
            separate
                    .append(key)
                    .append("\n")
                    .append("已付:")
                    .append(result.get(key))
                    .append("  应付:")
                    .append(String.format(Locale.CHINA, "%.2f", result.get(key) - average))
                    .append("\n\n");
        }

        TextView tv_total = (TextView)findViewById(R.id.summary_total);
        TextView tv_separate = (TextView)findViewById(R.id.summary_separate);

        tv_total.setText(total.toString());
        tv_separate.setText(separate.toString());
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
}
