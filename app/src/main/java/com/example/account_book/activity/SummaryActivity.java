package com.example.account_book.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.account_book.ConstantValue;
import com.example.account_book.DailyAccount;
import com.example.account_book.JourneyAccount;
import com.example.account_book.MyMarkerView;
import com.example.account_book.R;
import com.example.account_book.util.DBHelper;
import com.example.account_book.util.TimeUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.example.account_book.AccountBookApplication.getContext;

public class SummaryActivity extends AppCompatActivity implements OnChartValueSelectedListener, OnChartGestureListener {

    private static final String TAG = SummaryActivity.class.getSimpleName();

    /**
     * variable(Journey)
     */
    private HashMap<String, Double> result = new HashMap<>();

    /**
     * variable(Daily)
     */
    private boolean isDailySummary;
    private byte journeyID;
    private int summaryYear;
    private int summaryMonth;
    private double USD_RMB;
    private double HK_RMB;

    /**
     * UI(Daily)
     */
    private BarChart chartAmount;
    private Spinner spYear;
    private Spinner spMonth;
    private TextView tvAmountUSD;
    private TextView tvAmountHK;
    private TextView tvAmountRMB;
    private TextView tvAmountTotal;
    private TextView tvExchangeRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        journeyID = intent.getByteExtra(ConstantValue.JOURNEY_ID, ConstantValue.NONE_JOURNEY);
        isDailySummary = journeyID == -99;
        USD_RMB = 6.9;
        HK_RMB = 0.8;

        if (isDailySummary){
            setContentView(R.layout.activity_summary_daily);
        }else {
            setContentView(R.layout.activity_summary_journey);
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_summary);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(isDailySummary ? "个人账单统计" : "旅行账单统计");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        summaryYear = TimeUtils.getCurrentYear();
        summaryMonth = TimeUtils.getCurrentMonth();

        
        if (isDailySummary){
            initUIDaily();
            initChart();
            updateData();
            updateChartData();
        }else {
            initUIJourney();
        }

    }

    public static int dp2Pixel(int dps){
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    private void initUIDaily(){
        tvAmountUSD = (TextView)findViewById(R.id.summary_monthly_usd);
        tvAmountHK = (TextView)findViewById(R.id.summary_monthly_hk);
        tvAmountRMB = (TextView)findViewById(R.id.summary_monthly_rmb);
        tvAmountTotal = (TextView)findViewById(R.id.summary_monthly_total);
        tvExchangeRate = (TextView)findViewById(R.id.summary_exchange_rate);

        spMonth = (Spinner)findViewById(R.id.sp_month);
        spYear = (Spinner)findViewById(R.id.sp_year);

        tvExchangeRate.setText("参考汇率：\n美元-人民币 = 1 : 6.9\n港币-人民币 = 1 : 0.8");
        
        ArrayList<Integer> yearList = new ArrayList<>(5);
        for (int y = 2019; y < summaryYear + 2; y ++){
            yearList.add(y);
        }
        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, yearList);
        spYear.setAdapter(yearAdapter);

        spMonth.setSelection(summaryMonth - 1, true);
        // use to restrict the height of dropdown
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spMonth);

            // Set popupWindow height to 200dp
            popupWindow.setHeight(dp2Pixel(180));
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
            Log.e(TAG, e.toString());
        }


        setSpinnerListener();
    }

    private void initUIJourney(){
        TextView tv_total = (TextView)findViewById(R.id.summary_total);
        TextView tv_separate = (TextView)findViewById(R.id.summary_separate);

        DBHelper db = new DBHelper(SummaryActivity.this);
        ArrayList<JourneyAccount> journeyAccounts = (ArrayList<JourneyAccount>)db.queryJourneyAccount(journeyID);

        if (journeyAccounts != null){
            if (!journeyAccounts.isEmpty()){

                double sum = 0.0;
                double average = 0.0;

                for (DailyAccount dailyAccount : journeyAccounts) {
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
    
    private void updateData(){
        // get the dailyAccount during corresponding time
        DBHelper db = new DBHelper(SummaryActivity.this);
        ArrayList<DailyAccount> dailyAccounts = (ArrayList<DailyAccount>)db.queryDailyAccount(summaryYear, summaryMonth);
        double amountUSD = 0, amountHK = 0, amountRMB = 0;
        if (dailyAccounts != null){
            for (int i = 0; i < dailyAccounts.size(); i++){
                DailyAccount account = dailyAccounts.get(i);
                switch (account.getCurrencyType()){
                    case "USD":
                        amountUSD += account.getAmount();
                        break;
                    case "HK":
                        amountHK += account.getAmount();
                        break;
                    case "RMB":
                        amountRMB += account.getAmount();
                        break;
                    default:
                        break;
                }
            }
        }
        tvAmountUSD.setText(String.format(Locale.CHINA, "%.2f", amountUSD));
        tvAmountHK.setText(String.format(Locale.CHINA, "%.2f", amountHK));
        tvAmountRMB.setText(String.format(Locale.CHINA, "%.2f", amountRMB));
        tvAmountTotal.setText(String.format(Locale.CHINA, "%.3f", amountUSD * USD_RMB + amountHK * HK_RMB + amountRMB));
    }

    private void setSpinnerListener(){
        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                summaryYear = Integer.valueOf(parent.getItemAtPosition(position).toString());
                updateData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                summaryMonth = Integer.valueOf(parent.getItemAtPosition(position).toString());
                updateData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateChartData(){
        DBHelper dbHelper = new DBHelper(SummaryActivity.this);

        ArrayList<String> xVals = new ArrayList<>(6);
        ArrayList<Double> yVals = new ArrayList<>(6);

        int year = summaryYear;
        int month = summaryMonth;
        for (int i = 0; i < 6; i++){
            List<DailyAccount> accounts = dbHelper.queryDailyAccount(year, month);
            xVals.add(year + "-" + month);
            yVals.add(calculateTotalAmount(accounts));
            month -= 1;
            if (month <= 0){
                month = month + 12;
                year -= 1;
            }
        }
        Collections.reverse(xVals);
        Collections.reverse(yVals);
        updateAmountChart(xVals, yVals);
    }

    private double calculateTotalAmount(List<DailyAccount> accounts){
        if (accounts != null && accounts.size() > 0){
            double temp = 0.0;
            for (DailyAccount a : accounts){
                switch (a.getCurrencyType()){
                    case "USD":
                        temp += a.getAmount() * USD_RMB;
                        break;
                    case "HK":
                        temp += a.getAmount() * HK_RMB;
                        break;
                    case "RMB":
                        temp += a.getAmount();
                        break;
                    default:
                        break;
                }
            }
            return temp;
        }
        return 0.0;
    }

    private void initChart(){
        chartAmount = (BarChart)findViewById(R.id.barChart_amount);

        chartAmount.setOnChartGestureListener(this);
        chartAmount.setOnChartValueSelectedListener(this);
        chartAmount.setDrawGridBackground(false);
        // no description text
        chartAmount.getDescription().setEnabled(false);
//        MyMarkerView mv1 = new MyMarkerView(this, R.layout.custom_marker_view);
//        mv1.setChartView(chartAmount);
//        chartAmount.setMarker(mv1);

        // enable touch gestures
        chartAmount.setTouchEnabled(true);

        // enable scaling and dragging
        chartAmount.setDragEnabled(true);
        chartAmount.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chartAmount.setPinchZoom(true);

        XAxis xAxis1 = chartAmount.getXAxis();
        xAxis1.setGranularity(1);
        xAxis1.setGranularityEnabled(true);
        xAxis1.setPosition(XAxis.XAxisPosition.BOTTOM);

        // remove the right side y-Axis
        YAxis yAxisRight1 = chartAmount.getAxisRight();
        yAxisRight1.setEnabled(false);
    }

    private void updateAmountChart(List<String> xVals, List<Double> amountList){
        double maxValCnt = 0.0;
        if (amountList.size() != 0){
            // calculate the data range of data
            maxValCnt = amountList.get(0);
            for (Double amount : amountList){
                maxValCnt = amount > maxValCnt ? amount : maxValCnt;
            }
        }

        ArrayList<BarEntry> showVal = new ArrayList<>();
        for (int i = 0;i < amountList.size();i ++){
            showVal.add(new BarEntry(i, amountList.get(i).floatValue()));
        }

        BarDataSet set1;

        if (chartAmount.getData() != null && chartAmount.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chartAmount.getData().getDataSetByIndex(0);
            set1.setValues(showVal);
            set1.setValueTextSize(dp2Pixel(5));
            chartAmount.getData().notifyDataChanged();
            chartAmount.notifyDataSetChanged();

        } else {
            // create DataSet
            set1 = new BarDataSet(showVal, "每月总花费（rmb）");
            set1.setColor(Color.rgb(104, 241, 175));
            set1.setValueTextSize(dp2Pixel(5));
            BarData data = new BarData(set1);
            data.setValueFormatter(new LargeValueFormatter());

            chartAmount.setData(data);
        }

        updateAxis(xVals, (int) maxValCnt);
    }

    private void updateAxis(List<String> xVals, int maxValCnt){
        // set the range of xAxis
        chartAmount.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xVals));
//        chartAmount.getXAxis().setAxisMinimum(-0.5f);
//        chartAmount.getXAxis().setAxisMaximum(xVals.size());

        // set the range of yAxis
        chartAmount.getAxisLeft().setAxisMinimum(0);
        chartAmount.getAxisLeft().setAxisMaximum(maxValCnt + 1000);

        chartAmount.getBarData().setBarWidth(0.5f);

        // use when there at least two bar data set
        // (0.4 + 0.05) * 2 + 0.1 = 1
        /*float barWidth = 0.4f;
        float barSpace = 0.05f;
        float groupSpace = 0.1f;
        // specify the width each bar should have
        chartAmount.getBarData().setBarWidth(barWidth);

        chartAmount.groupBars(0, groupSpace, barSpace);
        chartAmount.setFitBars(true);*/

        chartAmount.invalidate();

        //animation
        chartAmount.animateXY(2000, 2000);
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
