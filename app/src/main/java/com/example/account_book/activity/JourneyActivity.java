package com.example.account_book.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.account_book.Journey;
import com.example.account_book.Journey;
import com.example.account_book.JourneyAdapter;
import com.example.account_book.R;
import com.example.account_book.util.DBHelper;

import java.util.ArrayList;
import java.util.Objects;

public class JourneyActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = "JourneyActivity";
    /**
     *  constant value
     */
    private final static int ADD_ACCOUNT = 0x0001;
    private final static int NEW_JOURNEY = 0x0002;

    /**
     * UI variable
     */
    private JourneyAdapter mJourneyAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     *  variable
     */
    private boolean isRefresh;
    private boolean isLoadMore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_journey);

        isRefresh = false;
        isLoadMore = false;

        loadData();

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_journey);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("我的旅程");

        initRecycleView();
        initUI();

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.srl_journey_account);
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
        getMenuInflater().inflate(R.menu.journey_menu, menu);
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
//            case R.id.action_new_journey:
//                showNewJourneyAlertDialog();
//                break;
//            case R.id.action_edit_journey:
//                editJourney();
//                break;
            case R.id.action_summary:
                Intent intent2 = new Intent(JourneyActivity.this, SummaryActivity.class);
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
//            //增加新的账单
//            if (resultCode == 1){
//                final Journey newJourney = new Journey();
//                newJourney.setContent(data.getStringExtra(AddAccountActivity.CONTENT));
//                newJourney.setAmount(data.getDoubleExtra(AddAccountActivity.AMOUNT, 0.0));
//                newJourney.setCurrencyType(data.getStringExtra(AddAccountActivity.PERSON));
//                newJourney.setCreateTime(data.getStringExtra(AddAccountActivity.TIME));
//                showToast("成功添加账单");
//                mJourneyAdapter.addJourney(newJourney);
//            }else {
//                showToast("未添加账单");
//            }
        }else if (requestCode == NEW_JOURNEY){
            if (resultCode == 1){
//                destination = data.getStringExtra(NewEditJourneyActivity.DES);
//                Objects.requireNonNull(getSupportActionBar()).setTitle(destination);
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
        mJourneyAdapter.filter(s);
        return true;
    }

    private void initUI(){
        FloatingActionButton fab = findViewById(R.id.fab_main);
        DrawableCompat.setTintList(DrawableCompat.wrap(fab.getDrawable()), ColorStateList.valueOf(Color.parseColor("#000000")));
        DrawableCompat.setTintList(DrawableCompat.wrap(fab.getBackground()), ColorStateList.valueOf(Color.parseColor("#3F51B5")));
        fab.setOnClickListener(listener -> {
//            if (destination.equals("记账本")){
//                showToast("请先添加旅程");
//            }else {
//                Intent intent = new Intent(JourneyActivity.this, NewEditJourneyActivity.class);
//                startActivityForResult(intent, ADD_ACCOUNT);
//            }
        });
    }

    private void reloadData(){
        mJourneyAdapter.clear();
        getAccountInfo();
    }

    private void getAccountInfo(){
        DBHelper db = new DBHelper(JourneyActivity.this);
        updateOrderUI((ArrayList<Journey>)db.queryJourney());
        if (isRefresh){
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                isRefresh = false;
                new Handler().postDelayed(() -> showToast("数据更新完毕"), 300);
            });
        }
    }

//    private void deleteOrder(final int position){
//        Journey Journey = mJourneyAdapter.getItem(position);
//        DBHelper db = new DBHelper(JourneyActivity.this);
//        db.deleteAccount(Journey.getId());
//        mJourneyAdapter.removeItem(position);
//        mJourneyAdapter.notifyDataSetChanged();
//        showToast("账单已删除");
//    }

    private void updateAccount(){

    }

    public void updateOrderUI(final ArrayList<Journey> journeys){
        if (journeys != null){
            runOnUiThread(() -> {
                mJourneyAdapter.addJourneys(journeys);
                if (isLoadMore){
                    isLoadMore = false;
                }
            });
        }
    }

    private void initRecycleView(){
        // initialize the adapter of RecyclerView
        mJourneyAdapter = new JourneyAdapter();
//        mJourneyAdapter.setListener(this::deleteOrder);
        RecyclerView rvOrder = (RecyclerView) findViewById(R.id.rv_daily_account);
        rvOrder.setLayoutManager(new LinearLayoutManager(this));
        rvOrder.setAdapter(mJourneyAdapter);
    }

    private void showToastUI(String toastMessage){
        runOnUiThread(() -> showToast(toastMessage));
    }

    private void showToast(String toastMessage){
        Toast.makeText(JourneyActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
    }

    private void showNewJourneyAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("注意：新建一个旅程会自动结束该旅程");
        builder.setPositiveButton("确定", ((dialogInterface, i) -> {
            Intent intent1 = new Intent(JourneyActivity.this, NewEditJourneyActivity.class);
            intent1.putExtra("type", "newJourney");
            startActivityForResult(intent1, NEW_JOURNEY);
        }));
        builder.setNegativeButton("取消", ((dialogInterface, i) -> {}));

        builder.create().show();
    }

    private void editJourney(){
        Intent intent1 = new Intent(JourneyActivity.this, NewEditJourneyActivity.class);
        intent1.putExtra("type", "editJourney");
        startActivityForResult(intent1, NEW_JOURNEY);
    }

    private void newJourneyDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("你还未创建旅程，是否新建旅程信息？");
        builder.setPositiveButton("确定", ((dialogInterface, i) -> {
            Intent intent1 = new Intent(JourneyActivity.this, NewEditJourneyActivity.class);
            intent1.putExtra("type", "newJourney");
            startActivityForResult(intent1, NEW_JOURNEY);
        }));
        builder.setNegativeButton("取消", ((dialogInterface, i) -> {}));

        builder.create().show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void loadData() {
//        SharedPreferences preferences = getSharedPreferences("journey_data", MODE_PRIVATE);
//        destination = preferences.getString("destination", "记账本");
//        if (destination != null & destination.equals("记账本")){
//            newJourneyDialog();
//        }
    }
}
