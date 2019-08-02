package com.example.account_book;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class DailyAccountAdapter extends SectionedRecyclerViewAdapter {

    private final static String TAG = DailyAccountAdapter.class.getSimpleName();

    /**
     * constant value
     */
    // 三种状态
    private final static int STATE_LOADING = 0;  //加载中
    private final static int STATE_COMPLETE = 1; //加载完成
    private final static int STATE_NOMORE = 2;  //没有更多了
    // 两种view
    private static final int TYPE_ITEM = 3;  //普通Item View
    private static final int TYPE_FOOTER = 4;  //顶部FootView


    private ArrayList<DailyAccount> mDailyAccounts;
    private ArrayList<DailyAccount> filteredDailyAccounts;
    private DailyAccountAdapter mAdapter;
    private int loadMoreStatue;

    private DeleteListener mListener;

    public interface DeleteListener{
        void onDeleteListener(int position);
    }

    public void setListener(DeleteListener mListener) {
        this.mListener = mListener;
    }

    public DailyAccountAdapter(){
        super();
        mDailyAccounts = new ArrayList<DailyAccount>();
        filteredDailyAccounts = new ArrayList<DailyAccount>();
        mAdapter = this;
        mListener = null;
    }

    public DailyAccountAdapter(ArrayList<DailyAccount> dailyAccounts){
        mDailyAccounts = dailyAccounts;
        filteredDailyAccounts = dailyAccounts;
        mAdapter = this;
        mListener = null;
    }

    public void addAccount(DailyAccount dailyAccount){
        if (!mDailyAccounts.contains(dailyAccount)){
            mDailyAccounts.add(dailyAccount);
        }
        if (!filteredDailyAccounts.contains(dailyAccount)){
            filteredDailyAccounts.add(dailyAccount);
        }
        updateSections();
        notifyDataSetChanged();
    }

    public void addAccounts(List<DailyAccount> dailyAccounts){
        mDailyAccounts.addAll(dailyAccounts);
        filteredDailyAccounts.addAll(dailyAccounts);
        updateSections();
        notifyDataSetChanged();
    }

    public DailyAccount getItem(int index){
        return filteredDailyAccounts.get(index);
    }

    public void removeItem(int index){
        mDailyAccounts.remove(filteredDailyAccounts.get(index));
        filteredDailyAccounts.remove(index);
        updateSections();
        notifyDataSetChanged();
    }

    public void clear(){
        mDailyAccounts.clear();
        filteredDailyAccounts.clear();
        updateSections();
        notifyDataSetChanged();
    }

    private void updateSections(){
        mAdapter.removeAllSections();
        Collections.sort(filteredDailyAccounts);
        Collections.reverse(filteredDailyAccounts);
        String lastTime = "";
        List<DailyAccount> dailyAccountSameDate = new LinkedList<>();
        for (DailyAccount dailyAccount : filteredDailyAccounts){
            // get the month
            String time = dailyAccount.getCreateTime().substring(0, 7);
            Log.e(TAG, time);
            // a new time
            if (!time.equals(lastTime) && !dailyAccountSameDate.isEmpty()){
                mAdapter.addSection(new ExpandableAccountSection(lastTime, dailyAccountSameDate));
                dailyAccountSameDate = new LinkedList<>();
            }
            dailyAccountSameDate.add(dailyAccount);
            lastTime = time;
        }
        if (!dailyAccountSameDate.isEmpty()){
            mAdapter.addSection(new ExpandableAccountSection(lastTime, dailyAccountSameDate));
        }
        notifyDataSetChanged();
    }

    public class ExpandableAccountSection extends StatelessSection {

        String title;
        List<DailyAccount> dailyAccounts;
        boolean expanded = true;

        private ExpandableAccountSection(String title, List<DailyAccount> dailyAccounts){
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.listitem_daily_account)
                    .headerResourceId(R.layout.listitem_account_header)
                    .build());

            this.title = title;
            this.dailyAccounts = dailyAccounts;
        }

        @Override
        public int getContentItemsTotal() {
            return expanded ? dailyAccounts.size() : 0;
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new MyAccountHolder(view);
        }


        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyAccountHolder){
                MyAccountHolder viewHolder = (MyAccountHolder) holder;
                DailyAccount dailyAccount = dailyAccounts.get(position);
                // don't show year
                viewHolder.accountDate.setText(dailyAccount.getCreateTime().substring(5, 16));
                viewHolder.accountNumber.setText(String.format("%s", dailyAccount.getAmount()));
                viewHolder.accountContent.setText(dailyAccount.getContent());
                viewHolder.accountLayout.setOnLongClickListener((view -> {
                    showPopWindows(viewHolder.accountLayout, position);
                    return false;
                }));
                viewHolder.accountCurrencyType.setText(dailyAccount.getCurrencyType());
                viewHolder.accountInOut.setText(dailyAccount.isIncome() ? R.string.account_in : R.string.account_out);
                viewHolder.accountInOut.setTextColor(
                        dailyAccount.isIncome() ?
                                AccountBookApplication.getContext().getColor(R.color.colorPrimary) : Color.RED);
            }else if (holder instanceof FooterViewHolder){
                FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
                switch (loadMoreStatue){
                    case STATE_LOADING:
                        footerViewHolder.item.setVisibility(View.VISIBLE);
                        footerViewHolder.progressBar.setVisibility(View.VISIBLE);
                        footerViewHolder.loadMoreHint.setText(R.string.loading);
                        break;
                    case STATE_COMPLETE:
                        footerViewHolder.item.setVisibility(View.GONE);
                        break;
                    case STATE_NOMORE:
                        footerViewHolder.item.setVisibility(View.VISIBLE);
                        footerViewHolder.progressBar.setVisibility(View.GONE);
                        footerViewHolder.loadMoreHint.setText(R.string.load_failed);
                        break;
                    default:
                        break;
                }
            }
        }

        private void showPopWindows(View v, int position) {

            /* pop view */
            View mPopView = LayoutInflater.from(AccountBookApplication.getContext()).inflate(R.layout.menu_delete_popup_window, null);
            final PopupWindow mPopWindow = new PopupWindow(mPopView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            /* set */
            mPopWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            /* 这个很重要 ,获取弹窗的长宽度 */
            mPopView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int popupWidth = mPopView.getMeasuredWidth();
            int popupHeight = mPopView.getMeasuredHeight();
            /* 获取父控件的位置 */
            int[] location = new int[2];
            v.getLocationOnScreen(location);
            /* 显示位置 */
            mPopWindow.showAtLocation(v, Gravity.NO_GRAVITY, (location[0] + v.getWidth() / 2) - popupWidth / 2, location[1]
                    +popupHeight/2);
            mPopWindow.update();

            mPopView.findViewById(R.id.copy).setOnClickListener(view -> {
                Log.e(TAG, "onClick: delete");
                if (mListener != null){
                    mListener.onDeleteListener(position);
                }

                if (mPopWindow != null) {
                    mPopWindow.dismiss();
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new MyHeadHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            MyHeadHolder headerHolder = (MyHeadHolder) holder;

            headerHolder.accountTime.setText(title);

            headerHolder.expand.setOnClickListener(view -> {
                headerHolder.expand.setRotation(expanded ? 0 : 180);
                expanded = !expanded;
                mAdapter.notifyDataSetChanged();
            });
        }
    }

    public static class MyHeadHolder extends RecyclerView.ViewHolder {

        TextView accountTime;
        ImageView expand;

        MyHeadHolder(View itemView) {
            super(itemView);
            accountTime = itemView.findViewById(R.id.account_time);
            expand = itemView.findViewById(R.id.expand_arrow);
        }

    }

    public static class MyAccountHolder extends RecyclerView.ViewHolder {
        MaterialCardView accountLayout;
        TextView accountDate;
        TextView accountNumber;
        TextView accountContent;
        TextView accountCurrencyType;
        TextView accountInOut;
        MyAccountHolder(View itemView) {
            super(itemView);
            accountLayout = itemView.findViewById(R.id.daily_account_layout);
            accountDate = itemView.findViewById(R.id.account_date);
            accountNumber = itemView.findViewById(R.id.account_amount);
            accountContent = itemView.findViewById(R.id.account_content);
            accountCurrencyType = itemView.findViewById(R.id.account_currency_type);
            accountInOut = itemView.findViewById(R.id.account_in_out);
        }

    }

    public class FooterViewHolder extends RecyclerView.ViewHolder{
        TextView loadMoreHint;
        ProgressBar progressBar;
        View item;

        FooterViewHolder(View itemView) {
            super(itemView);
            loadMoreHint = (TextView)itemView.findViewById(R.id.tv_load_more_hint);
            progressBar = (ProgressBar)itemView.findViewById(R.id.progress_loading);
            item = itemView;
        }
    }

    public void filter(String filterKeyword){
        filteredDailyAccounts.clear();
        if (filterKeyword.isEmpty()){
            filteredDailyAccounts.addAll(mDailyAccounts);
        }else {
            for (DailyAccount dailyAccount : mDailyAccounts){
                if (dailyAccount.getSimpleString().toLowerCase().contains(filterKeyword)){
                    filteredDailyAccounts.add(dailyAccount);
                }
            }
        }
        updateSections();
        notifyDataSetChanged();
    }

//    private boolean isFooter(int position) {
//        return position == getItemCount() - 1;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        if (isFooter(position)){
//            return TYPE_FOOTER;
//        }else {
//            return TYPE_ITEM;
//        }
//    }
//
//    /**
//     * //上拉加载更多
//     * STATUES_LOADING
//     * //正在加载中
//     * LOADING_MORE=1;
//     * //加载完成已经没有更多数据了
//     * NO_MORE_DATA=2;
//     * @param status statues
//     *
//     */
//    public void changeMoreStatus(int status){
//        loadMoreStatue = status;
//        updateSections();
//        notifyDataSetChanged();
//    }
}
