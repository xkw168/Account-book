package com.example.account_book.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.account_book.AccountBookApplication;
import com.example.account_book.ConstantValue;
import com.example.account_book.DailyAccount;
import com.example.account_book.JourneyAccount;
import com.example.account_book.R;
import com.example.account_book.util.DBHelper;
import com.example.account_book.util.TimeUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class AddAccountActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * constant value
     */
    private static final String TAG = AddAccountActivity.class.getSimpleName();
    public static final String CONTENT = "content";
    public static final String AMOUNT = "amount";
    public static final String CURRENCY_TYPE = "currency_type";
    public static final String PERSON = "person";
    public static final String TIME = "time";
    public static final String IS_INCOME = "is_income";

    /**
     * UI
     */
    private TextInputEditText etNumber;
    private TextInputEditText etContent;
    private Spinner spPerson;
    private Spinner spCurrency;

    /**
     * variable
     */
    private boolean isDailyAccount;
    private boolean isIncome;
    private List<String> peopleList = new LinkedList<>();
    private List<String> currencyList = new ArrayList<>(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        Intent intent = getIntent();
        isDailyAccount = intent.getByteExtra(ConstantValue.ACCOUNT_TYPE, ConstantValue.NEW_DAILY_ACCOUNT) == ConstantValue.NEW_DAILY_ACCOUNT;

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_new_account);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(isDailyAccount ? "新增每日账单" : "新增旅行账单");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isIncome = false;

        loadData();

        initUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_submit:
                String number = "", content = "";
                String person = "";
                if (!isDailyAccount){
                    person = spPerson.getSelectedItem().toString();
                }
                String currency_type = spCurrency.getSelectedItem().toString();
                if (etNumber.getText() != null){
                    number = etNumber.getText().toString().trim();
                }
                if (etContent.getText() != null){
                    content = etContent.getText().toString().trim();
                }

                Intent intent = new Intent();

                if (isDailyAccount){
                    DailyAccount dailyAccount = new DailyAccount();
                    dailyAccount.setAmount(Double.valueOf(number));
                    dailyAccount.setContent(content);
                    dailyAccount.setCurrencyType(currency_type);
                    dailyAccount.setIncome(isIncome);

                    intent.putExtra(CONTENT, dailyAccount.getContent());
                    intent.putExtra(AMOUNT, dailyAccount.getAmount());
                    intent.putExtra(CURRENCY_TYPE, dailyAccount.getCurrencyType());
                    intent.putExtra(TIME, dailyAccount.getCreateTime());
                    intent.putExtra(IS_INCOME, dailyAccount.isIncome());

                    DBHelper db = new DBHelper(AddAccountActivity.this);
                    db.addDailyAccount(dailyAccount);
                }else {
                    JourneyAccount journeyAccount = new JourneyAccount();
                    journeyAccount.setAmount(Double.valueOf(number));
                    journeyAccount.setContent(content);
                    journeyAccount.setCurrencyType(currency_type);
                    journeyAccount.setPerson(person);
                    journeyAccount.setIncome(isIncome);

                    intent.putExtra(CONTENT, journeyAccount.getContent());
                    intent.putExtra(AMOUNT, journeyAccount.getAmount());
                    intent.putExtra(CURRENCY_TYPE, journeyAccount.getCurrencyType());
                    intent.putExtra(TIME, journeyAccount.getCreateTime());
                    intent.putExtra(IS_INCOME, journeyAccount.isIncome());

                    DBHelper db = new DBHelper(AddAccountActivity.this);
                    db.addJourneyAccount(journeyAccount);
                }

                setResult(1, intent);
                finish();
                break;
            case R.id.bt_cancel:
                finish();
                break;
            default:
                break;
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

    public void loadData() {
        SharedPreferences preferences = getSharedPreferences("journey_data", MODE_PRIVATE);
        int cnt = preferences.getInt("count", 0);
        Log.e(TAG, "loadData: " + cnt);
        for (int i = 0; i < cnt; i++){
            peopleList.add(preferences.getString(String.format("person%s", i), ""));
            Log.e(TAG, peopleList.get(i));
        }
        currencyList.add("USD");
        currencyList.add("RMB");
        currencyList.add("HKD");
    }

    public void initUI(){
        etNumber = (TextInputEditText)findViewById(R.id.et_new_account_number);
        etContent = (TextInputEditText)findViewById(R.id.et_new_account_content);
        spPerson = (Spinner)findViewById(R.id.sp_person);
        spCurrency = (Spinner)findViewById(R.id.sp_currency);
        RadioGroup rgInOut = (RadioGroup)findViewById(R.id.rg_account_is_income);

        rgInOut.setOnCheckedChangeListener(((radioGroup, checkedId) -> {
            if (checkedId == R.id.rb_account_in){
                isIncome = true;
            }else if (checkedId == R.id.rb_account_out){
                isIncome = false;
            }
        }));

        TextView tvPerson = (TextView)findViewById(R.id.tv_person);
        TextView tvTime = (TextView) findViewById(R.id.tv_create_time);
        MaterialButton btCancel = (MaterialButton) findViewById(R.id.bt_cancel);
        MaterialButton btSubmit = (MaterialButton) findViewById(R.id.bt_submit);

        btCancel.setOnClickListener(this);
        btSubmit.setOnClickListener(this);

        DrawableCompat.setTintList(DrawableCompat.wrap(btCancel.getBackground()), ColorStateList.valueOf(getColor(R.color.colorPrimary)));
//        btCancel.getBackground().setColorFilter(getColor(R.color.colorPrimary));

        tvTime.setText(String.format("时间: %s", TimeUtils.now()));

        if (isDailyAccount){
            spPerson.setVisibility(View.GONE);
            tvPerson.setVisibility(View.GONE);
        }else {
            spPerson.setVisibility(View.VISIBLE);
            tvPerson.setVisibility(View.VISIBLE);
            ArrayAdapter<String> peopleAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, peopleList);
            spPerson.setAdapter(peopleAdapter);
        }

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, currencyList);
        spCurrency.setAdapter(currencyAdapter);

        int pos = 0;
        switch (AccountBookApplication.getPrimaryCurrency()){
            case "USD":
                pos = 0;
                break;
            case "RMB":
                pos = 1;
                break;
            case "HKD":
                pos = 2;
                break;
            default:
                break;
        }
        spCurrency.setSelection(pos);
    }
}
