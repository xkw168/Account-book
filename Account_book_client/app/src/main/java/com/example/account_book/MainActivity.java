package com.example.account_book;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.account_book.util.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "MainActivity";
    /**
     *  constant value
     */
    private final static String ACCOUNTS_INFO = "accounts";
    private final static int ADD_ACCOUNT = 0x0001;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        isRefresh = false;
        isLoadMore = false;

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

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
        getMenuInflater().inflate(R.menu.main_menu,menu);
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
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ACCOUNT){
            //增加新的订单
            if (resultCode == 1){
                final Account newAccount = new Account();
                newAccount.setContent(data.getStringExtra(AddAccountActivity.CONTENT));
                newAccount.setNumber(data.getDoubleExtra(AddAccountActivity.NUMBER, 0.0));
                newAccount.setPerson(data.getStringExtra(AddAccountActivity.PERSON));
                newAccount.setCreateTime(data.getStringExtra(AddAccountActivity.TIME));
                showToast("成功添加订单...");
                mAccountAdapter.addAccount(newAccount);
            }else {
                showToast("添加账单失败，请检查网络连接...");
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
            Intent intent = new Intent(this, AddAccountActivity.class);
            startActivityForResult(intent, ADD_ACCOUNT);
        });
    }

    private void reloadData(){
        mAccountAdapter.clear();
        OFFSET = 0;
        getAccountInfo();
    }

    private void getAccountInfo(){
        String str = HttpUtils.QUERY_ALL_ACCOUNT + OFFSET + "/" + LIMIT;
        HttpUtils.sendRequestGetAsy(str, new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, e.toString());
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showToast("获取账单信息失败，请检查网络连接...");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try{
                    assert response.body() != null;
                    String str = response.body().string();
                    JSONObject jsonObject = new JSONObject(str);
                    //子线程不能更新UI相关内容
                    updateAccountUI((ArrayList<Account>)
                            HttpUtils.parseAllAccount(jsonObject.getString(ACCOUNTS_INFO)));
                    if (isRefresh){
                        runOnUiThread(() -> {
                            swipeRefreshLayout.setRefreshing(false);
                            isRefresh = false;
                            new Handler().postDelayed(() -> showToast("数据更新完毕"), 300);
                        });
                    }
                }catch (JSONException e){
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    private void deleteAccount(final int position){
        Account account = mAccountAdapter.getItem(position);
        HttpUtils.sendRequestGetAsy(HttpUtils.DELETE_ACCOUNT + account.getId(), new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, e.toString());
                showToastUI("删除失败，请检查网络连接...");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    mAccountAdapter.removeItem(position);
                    mAccountAdapter.notifyDataSetChanged();
                    showToast("账单已删除");
                });
            }
        });
    }

    private void updateAccount(){

    }

    public void updateAccountUI(final ArrayList<Account> accounts){
        if (accounts != null){
            runOnUiThread(() -> {
                mAccountAdapter.addAccounts(accounts);
                if (isLoadMore){
//                    if (accounts.size() == LIMIT){
//                        mAccountAdapter.changeMoreStatus(AccountAdapter.STATE_COMPLETE);
//                    }else {
//                        mAccountAdapter.changeMoreStatus(AccountAdapter.STATE_NOMORE);
//                    }
                    isLoadMore = false;
                }
            });
        }
    }

    private void initRecycleView(){
        // initialize the adapter of RecyclerView
        mAccountAdapter = new AccountAdapter();
        mAccountAdapter.setListener(this::deleteAccount);
        RecyclerView rvOrder = (RecyclerView) findViewById(R.id.rv_order);
        rvOrder.setLayoutManager(new LinearLayoutManager(this));
        rvOrder.setAdapter(mAccountAdapter);

//        rvOrder.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState ==RecyclerView.SCROLL_STATE_IDLE &&
//                        ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition() + 1 == mAccountAdapter.getItemCount()) {
////                    mAccountAdapter.changeMoreStatus(AccountAdapter.STATE_LOADING);
//                    OFFSET += LIMIT;
//                    isLoadMore = true;
//                    getOrderInfo();
//                }
//            }
//        });
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

