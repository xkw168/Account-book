package com.example.account_book.activity;

import android.content.SharedPreferences;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.account_book.AccountBookApplication;
import com.example.account_book.ConstantValue;
import com.example.account_book.R;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private Spinner spPrimary;
    private Spinner spSecondary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("设置");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initUI();
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

    private void initUI(){
        MaterialButton btSubmit = (MaterialButton)findViewById(R.id.bt_submit);
        MaterialButton btCancel = (MaterialButton)findViewById(R.id.bt_cancel);
        btSubmit.setOnClickListener(this);
        btCancel.setOnClickListener(this);

        spPrimary = (Spinner)findViewById(R.id.sp_primary_currency);
        spSecondary = (Spinner)findViewById(R.id.sp_secondary_currency);

        spPrimary.setSelection(0);
        spSecondary.setSelection(1);
    }

    private void saveData(String primary, String secondary){
        // save to SharedPreference
        SharedPreferences preferences = getSharedPreferences(ConstantValue.CACHE_DATA, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        // store data in String format
        editor.putString(ConstantValue.PRIMARY_CURRENCY, primary);
        editor.putString(ConstantValue.SECONDARY_CURRENCY, secondary);
        // submit data
        editor.apply();

        // save to Application
        AccountBookApplication.setPrimaryCurrency(primary);
        AccountBookApplication.setSecondaryCurrency(secondary);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_submit:
                saveData(
                        spPrimary.getSelectedItem().toString(),
                        spSecondary.getSelectedItem().toString()
                );
                finish();
                break;
            case R.id.bt_cancel:
                finish();
                break;
            default:
                break;
        }
    }
}
