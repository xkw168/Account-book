package com.example.account_book.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.account_book.R;
import com.example.account_book.util.DBHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class NewEditJourneyActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = NewEditJourneyActivity.class.getSimpleName();
    public static final String DES = "destination";

    /**
     * UI
     */
    private TextView tvPeopleList;
    private EditText etDestination;
    private EditText etPerson;
    private Button btAdd;
    private Button btDelete;
    private Button btConfirm;

    /**
     * variable
     */
    private boolean isEdit;
    private String destination;
    private List<String> peopleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_journey);

        String type = getIntent().getExtras().getString("type");
        Log.e(TAG, type);
        isEdit = type.equals("editJourney");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_new_journey);
        setSupportActionBar(toolbar);
        if (isEdit){
            Objects.requireNonNull(getSupportActionBar()).setTitle("修改旅程信息");
        }else {
            Objects.requireNonNull(getSupportActionBar()).setTitle("新的旅程");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        peopleList = new LinkedList<>();

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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_add:
                String person1 = etPerson.getText().toString();
                if (!person1.isEmpty()){
                    if (peopleList.contains(person1)){
                        showToast("用户已存在");
                    }else {
                        peopleList.add(person1);
                        showPeopleList();
                        etPerson.setText("");
                        showToast("成功添加旅伴");
                    }
                }
                break;
            case R.id.bt_delete:
                String person2 = etPerson.getText().toString();
                if (!person2.isEmpty()){
                    if (peopleList.contains(person2)){
                        peopleList.remove(person2);
                        etPerson.setText("");
                        showPeopleList();
                    }else{
                        showToast("无此用户");
                    }
                }
                break;
            case R.id.bt_confirm:
                destination = etDestination.getText().toString();
                if (!destination.isEmpty()){
                    saveData();
                    showToast(isEdit ? "成功修改旅程" : "成功添加旅程");
                    Intent data = new Intent();
                    data.putExtra(DES, destination);
                    setResult(1, data);
                    finish();
                }else {
                    showToast("请输入旅程名");
                }
                break;
            default:
                break;
        }
    }

    private void initUI(){
        tvPeopleList = (TextView)findViewById(R.id.tv_people_list);
        etDestination = (EditText)findViewById(R.id.et_destination);
        etPerson = (EditText)findViewById(R.id.et_person);
        btAdd = (Button)findViewById(R.id.bt_add);
        btDelete = (Button)findViewById(R.id.bt_delete);
        btConfirm = (Button)findViewById(R.id.bt_confirm);

        btAdd.setOnClickListener(this);
        btDelete.setOnClickListener(this);
        btConfirm.setOnClickListener(this);

        if (isEdit){
            loadData();
            etDestination.setText(destination);
            showPeopleList();
            btConfirm.setText("确认修改");
        }
    }

    private void showPeopleList(){
        StringBuilder peopleStr = new StringBuilder();
        for(String person : peopleList){
            peopleStr.append(person).append("\n");
        }
        tvPeopleList.setText(peopleStr.toString());
    }

    private void showToast(String message){
        Toast.makeText(NewEditJourneyActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences preferences = getSharedPreferences("journey_data", MODE_PRIVATE);
        destination = preferences.getString("destination", "记账本");
        int cnt = preferences.getInt("count", 0);
        Log.e(TAG, "loadData: " + cnt);
        for (int i = 0; i < cnt; i++){
            peopleList.add(preferences.getString(String.format("person%s", i), ""));
            Log.e(TAG, peopleList.get(i));
        }
    }

    public void saveData(){
        SharedPreferences preferences = getSharedPreferences("journey_data", MODE_PRIVATE);
        //获取editor对象
        SharedPreferences.Editor editor = preferences.edit();
        //存储数据时选用对应类型的方法
        editor.putString("destination", destination);
        editor.putInt("count", peopleList.size());
        for (int i = 0; i < peopleList.size(); i++){
            editor.putString(String.format("person%s", i), peopleList.get(i));
            Log.e(TAG, peopleList.get(i));
        }
        //提交保存数据
        editor.apply();
    }
}
