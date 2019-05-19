package com.example.account_book;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.account_book.util.DBHelper;
import com.example.account_book.util.TimeUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class AddAccountActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * constant value
     */
    private static final String TAG = com.example.account_book.AddAccountActivity.class.getSimpleName();
    public static final String CONTENT = "content";
    public static final String NUMBER = "number";
    public static final String PERSON = "person";
    public static final String TIME = "time";

    /**
     * UI
     */
    private TextInputEditText etNumber;
    private TextInputEditText etContent;
    private Spinner spPerson;

    /**
     * variable
     */
    private List<String> peopleList = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_new_account);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("新增账单");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadData();

        etNumber = (TextInputEditText)findViewById(R.id.et_new_account_number);
        etContent = (TextInputEditText)findViewById(R.id.et_new_account_content);
        spPerson = (Spinner)findViewById(R.id.sp_person);
        TextView tvTime = (TextView) findViewById(R.id.tv_create_time);
        MaterialButton btCancel = (MaterialButton) findViewById(R.id.bt_cancel);
        MaterialButton btSubmit = (MaterialButton) findViewById(R.id.bt_submit);
        btCancel.setOnClickListener(this);
        btSubmit.setOnClickListener(this);

        tvTime.setText(String.format("时间: %s", TimeUtils.now()));
        ArrayAdapter<String> peopleAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, peopleList);
        spPerson.setAdapter(peopleAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_submit:
                String number = "", content = "";
                String person = spPerson.getSelectedItem().toString();
                if (etNumber.getText() != null){
                    number = etNumber.getText().toString().trim();
                }
                if (etContent.getText() != null){
                    content = etContent.getText().toString().trim();
                }

                Account account = new Account();
                account.setNumber(Double.valueOf(number));
                account.setContent(content);
                account.setPerson(person);

                Intent intent = new Intent();
                intent.putExtra(CONTENT, account.getContent());
                intent.putExtra(NUMBER, account.getNumber());
                intent.putExtra(PERSON, account.getPerson());
                intent.putExtra(TIME, account.getCreateTime());

                DBHelper db = new DBHelper(AddAccountActivity.this);
                db.addAccount(account);

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
    }
}
