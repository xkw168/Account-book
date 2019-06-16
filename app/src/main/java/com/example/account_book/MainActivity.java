package com.example.account_book;

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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.account_book.util.DBHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "MainActivity";
    /**
     *  constant value
     */
    private final static String ACCOUNTS_INFO = "accounts";
    private final static int ADD_ACCOUNT = 0x0001;
    private final static int NEW_JOURNEY = 0x0002;

    public static int OFFSET = 0;
    public static int LIMIT = 30;

    /**
     * UI variable
     */
    private AccountAdapter mAccountAdapter;
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
        destination = "记账本";

        loadData();

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(destination);

        initRecycleView();
        initUI();

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.srl_order);
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
            case R.id.action_new_journey:
                showNewJourneyAlertDialog();
                break;
            case R.id.action_summary:
                Intent intent2 = new Intent(MainActivity.this, SummaryActivity.class);
                startActivity(intent2);
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
                final Account newAccount = new Account();
                newAccount.setContent(data.getStringExtra(AddAccountActivity.CONTENT));
                newAccount.setNumber(data.getDoubleExtra(AddAccountActivity.NUMBER, 0.0));
                newAccount.setPerson(data.getStringExtra(AddAccountActivity.PERSON));
                newAccount.setCreateTime(data.getStringExtra(AddAccountActivity.TIME));
                showToast("成功添加账单");
                mAccountAdapter.addAccount(newAccount);
            }else {
                showToast("未添加账单");
            }
        }else if (requestCode == NEW_JOURNEY){
            if (resultCode == 1){
                destination = data.getStringExtra(NewJourneyActivity.DES);
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
        mAccountAdapter.filter(s);
        return true;
    }

    private void initUI(){
        FloatingActionButton fab = findViewById(R.id.fab);
        DrawableCompat.setTintList(DrawableCompat.wrap(fab.getDrawable()), ColorStateList.valueOf(Color.parseColor("#000000")));
        DrawableCompat.setTintList(DrawableCompat.wrap(fab.getBackground()), ColorStateList.valueOf(Color.parseColor("#3F51B5")));
        fab.setOnClickListener(listener -> {
            if (destination.equals("记账本")){
                showToast("请先添加旅程");
            }else {
                Intent intent = new Intent(MainActivity.this, AddAccountActivity.class);
                startActivityForResult(intent, ADD_ACCOUNT);
            }
        });
    }

    private void reloadData(){
        mAccountAdapter.clear();
        OFFSET = 0;
        getOrderInfo();
    }

    private void getOrderInfo(){
        DBHelper db = new DBHelper(MainActivity.this);
        updateOrderUI((ArrayList<Account>)db.queryAllAccount());
        if (isRefresh){
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                isRefresh = false;
                new Handler().postDelayed(() -> showToast("数据更新完毕"), 300);
            });
        }
    }

    private void deleteOrder(final int position){
        Account account = mAccountAdapter.getItem(position);
        DBHelper db = new DBHelper(MainActivity.this);
        db.deleteAccount(account.getId());
        mAccountAdapter.removeItem(position);
        mAccountAdapter.notifyDataSetChanged();
        showToast("账单已删除");
    }

    private void updateAccount(){

    }

    public void updateOrderUI(final ArrayList<Account> accounts){
        if (accounts != null){
            runOnUiThread(() -> {
                mAccountAdapter.addAccounts(accounts);
                if (isLoadMore){
                    isLoadMore = false;
                }
            });
        }
    }

    private void initRecycleView(){
        // initialize the adapter of RecyclerView
        mAccountAdapter = new AccountAdapter();
        mAccountAdapter.setListener(this::deleteOrder);
        RecyclerView rvOrder = (RecyclerView) findViewById(R.id.rv_order);
        rvOrder.setLayoutManager(new LinearLayoutManager(this));
        rvOrder.setAdapter(mAccountAdapter);
    }

    private void showToastUI(String toastMessage){
        runOnUiThread(() -> showToast(toastMessage));
    }

    private void showToast(String toastMessage){
        Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
    }

    private void showNewJourneyAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("注意：新建一个旅程会清空所有旧旅程数据");
        builder.setPositiveButton("确定", ((dialogInterface, i) -> {
            Intent intent1 = new Intent(MainActivity.this, NewJourneyActivity.class);
            startActivityForResult(intent1, NEW_JOURNEY);
        }));
        builder.setNegativeButton("取消", ((dialogInterface, i) -> {}));

        builder.create().show();
    }

    private void newJourneyDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("你还未创建旅程，是否新建旅程信息？");
        builder.setPositiveButton("确定", ((dialogInterface, i) -> {
            Intent intent1 = new Intent(MainActivity.this, NewJourneyActivity.class);
            startActivityForResult(intent1, NEW_JOURNEY);
        }));
        builder.setNegativeButton("取消", ((dialogInterface, i) -> {}));

        builder.create().show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void loadData() {
        SharedPreferences preferences = getSharedPreferences("journey_data", MODE_PRIVATE);
        destination = preferences.getString("destination", "记账本");
        if (destination != null & destination.equals("记账本")){
            newJourneyDialog();
        }
    }

}

