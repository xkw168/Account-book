package com.example.account_book.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.account_book.ConstantValue;
import com.example.account_book.DailyAccount;
import com.example.account_book.DailyAccountAdapter;
import com.example.account_book.R;
import com.example.account_book.util.DBHelper;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "MainActivity";
    /**
     *  constant value
     */
    private final static String ACCOUNTS_INFO = "dailyAccounts";
    private final static int ADD_ACCOUNT = 0x0001;
    private final static int NEW_JOURNEY = 0x0002;

    /**
     * UI variable
     */
    private DailyAccountAdapter mDailyAccountAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     *  variable
     */
    private boolean isRefresh;
    private boolean isLoadMore;
    private String destination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        isRefresh = false;
        isLoadMore = false;
        destination = "日常记账本";

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(destination);

        initRecycleView();
        initUI();

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.srl_daily_account);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            isRefresh = true;
            reloadData();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        reloadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_summary:
                Intent intent1 = new Intent(MainActivity.this, SummaryActivity.class);
                intent1.putExtra(ConstantValue.JOURNEY_ID, ConstantValue.NONE_JOURNEY);
                startActivity(intent1);
                break;
            case R.id.action_journey_mode:
//                Intent intent2 = new Intent(MainActivity.this, JourneyActivity.class);
//                startActivity(intent2);
                showToast("敬请期待...");
                break;
            case R.id.setting:
                Intent intent3 = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent3);
                break;
            case R.id.about:
                Intent intent4 = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent4);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ACCOUNT){
            //增加新的账单
            if (resultCode == 1){
//                final DailyAccount newDailyAccount = new DailyAccount();
//                newDailyAccount.setContent(data.getStringExtra(AddAccountActivity.CONTENT));
//                newDailyAccount.setAmount(data.getDoubleExtra(AddAccountActivity.AMOUNT, 0.0));
//                newDailyAccount.setCreateTime(data.getStringExtra(AddAccountActivity.TIME));
//                newDailyAccount.setIncome(data.getBooleanExtra(AddAccountActivity.IS_INCOME, false));
                showToast("成功添加账单");
//                mDailyAccountAdapter.addAccount(newDailyAccount);
            }else {
                showToast("未添加账单");
            }
        }else if (requestCode == NEW_JOURNEY){
            if (resultCode == 1){
                destination = data.getStringExtra(NewEditJourneyActivity.DES);
                Objects.requireNonNull(getSupportActionBar()).setTitle(destination);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mDailyAccountAdapter.filter(s);
        return true;
    }

    private void initUI(){
        FloatingActionButton fab = findViewById(R.id.fab_main);
        DrawableCompat.setTintList(DrawableCompat.wrap(fab.getDrawable()), ColorStateList.valueOf(getColor(R.color.secondary_text)));
        DrawableCompat.setTintList(DrawableCompat.wrap(fab.getBackground()), ColorStateList.valueOf(getColor(R.color.colorPrimary)));
        fab.setOnClickListener(listener -> {
            Intent intent = new Intent(MainActivity.this, AddAccountActivity.class);
            intent.putExtra(ConstantValue.ACCOUNT_TYPE, ConstantValue.NEW_DAILY_ACCOUNT);
            startActivityForResult(intent, ADD_ACCOUNT);
        });
    }

    private void reloadData(){
        mDailyAccountAdapter.clear();
        getAccountInfo();
    }

    private void getAccountInfo(){
        DBHelper db = new DBHelper(MainActivity.this);
        updateOrderUI((ArrayList<DailyAccount>)db.queryAllDailyAccount());
        if (isRefresh){
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                isRefresh = false;
                new Handler().postDelayed(() -> showToast("数据更新完毕"), 300);
            });
        }
    }

    private void deleteOrder(final int position){
        DailyAccount dailyAccount = mDailyAccountAdapter.getItem(position);
        DBHelper db = new DBHelper(MainActivity.this);
        db.deleteAccount(dailyAccount.getId(), true);
        mDailyAccountAdapter.removeItem(position);
        mDailyAccountAdapter.notifyDataSetChanged();
        showToast("账单已删除");
    }

    public void updateOrderUI(final ArrayList<DailyAccount> dailyAccounts){
        if (dailyAccounts != null){
            runOnUiThread(() -> {
                mDailyAccountAdapter.addAccounts(dailyAccounts);
                if (isLoadMore){
                    isLoadMore = false;
                }
            });
        }
    }

    private void initRecycleView(){
        // initialize the adapter of RecyclerView
        mDailyAccountAdapter = new DailyAccountAdapter();
        mDailyAccountAdapter.setListener(this::deleteOrder);
        RecyclerView rvOrder = (RecyclerView) findViewById(R.id.rv_daily_account);
        rvOrder.setLayoutManager(new LinearLayoutManager(this));
        rvOrder.setAdapter(mDailyAccountAdapter);
    }

    private void showToastUI(String toastMessage){
        runOnUiThread(() -> showToast(toastMessage));
    }

    private void showToast(String toastMessage){
        Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}

