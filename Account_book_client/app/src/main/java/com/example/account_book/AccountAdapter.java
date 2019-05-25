package com.example.account_book;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

class AccountAdapter extends SectionedRecyclerViewAdapter {

    public final static String TAG = AccountAdapter.class.getSimpleName();

    /**
     * constant value
     */
    // 三种状态
    public final static int STATE_LOADING = 0;  //加载中
    public final static int STATE_COMPLETE = 1; //加载完成
    public final static int STATE_NOMORE = 2;  //没有更多了
    // 两种view
    private static final int TYPE_ITEM = 3;  //普通Item View
    private static final int TYPE_FOOTER = 4;  //顶部FootView


    private ArrayList<Account> mAccounts;
    private ArrayList<Account> filteredAccounts;
    private AccountAdapter mAdapter;
    private int loadMoreStatue;

    private DeleteListener mListener;

    public interface DeleteListener{
        void onDeleteListener(int position);
    }

    public void setListener(DeleteListener mListener) {
        this.mListener = mListener;
    }

    AccountAdapter(){
        super();
        mAccounts = new ArrayList<Account>();
        filteredAccounts = new ArrayList<Account>();
        mAdapter = this;
        mListener = null;
    }

    AccountAdapter(ArrayList<Account> accounts){
        mAccounts = accounts;
        filteredAccounts = accounts;
        mAdapter = this;
        mListener = null;
    }

    void addAccount(Account account){
        if (!mAccounts.contains(account)){
            mAccounts.add(account);
        }
        if (!filteredAccounts.contains(account)){
            filteredAccounts.add(account);
        }
        updateSections();
        notifyDataSetChanged();
    }

    void addAccounts(List<Account> accounts){
        mAccounts.addAll(accounts);
        filteredAccounts.addAll(accounts);
        updateSections();
        notifyDataSetChanged();
    }

    Account getItem(int index){
        return filteredAccounts.get(index);
    }

    void removeItem(int index){
        mAccounts.remove(filteredAccounts.get(index));
        filteredAccounts.remove(index);
        updateSections();
        notifyDataSetChanged();
    }

    void clear(){
        mAccounts.clear();
        filteredAccounts.clear();
        updateSections();
        notifyDataSetChanged();
    }

    void updateSections(){
        mAdapter.removeAllSections();
        Collections.sort(filteredAccounts);
        Collections.reverse(filteredAccounts);
        String lastTime = "";
        double totalPrice = 0.0;
        List<Account> accountSameDate = new LinkedList<>();
        for (Account account : filteredAccounts){
            String time = account.getCreateTime().substring(0, 10);
            Log.e(TAG, time);
            // a new time
            if (!time.equals(lastTime) && !accountSameDate.isEmpty()){
                mAdapter.addSection(new ExpandableAccountSection(lastTime + "(" + totalPrice + ")", accountSameDate));
                accountSameDate = new LinkedList<>();
                totalPrice = 0.0;
            }
            accountSameDate.add(account);
            totalPrice += account.getNumber();
            lastTime = time;
        }
        if (!accountSameDate.isEmpty()){
            mAdapter.addSection(new ExpandableAccountSection(lastTime + "(" + totalPrice + ")", accountSameDate));
        }
        notifyDataSetChanged();
    }

    public class ExpandableAccountSection extends StatelessSection {

        String title;
        List<Account> accounts;
        boolean expanded = true;

        private ExpandableAccountSection(String title, List<Account> accounts){
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.listitem_account)
                    .headerResourceId(R.layout.listitem_account_header)
                    .build());

            this.title = title;
            this.accounts = accounts;
        }

        @Override
        public int getContentItemsTotal() {
            return expanded ? accounts.size() : 0;
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new MyAccountHolder(view);
        }


        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyAccountHolder){
                MyAccountHolder viewHolder = (MyAccountHolder) holder;
                Account account = accounts.get(position);
                viewHolder.accountDate.setText(account.getCreateTime());
                viewHolder.accountNumber.setText(String.format("%s", account.getNumber()));
                viewHolder.accountContent.setText(account.getContent());
                viewHolder.accountPerson.setText(account.getPerson());
                viewHolder.delete.setOnClickListener((view) -> {
                    if (mListener != null){
                        mListener.onDeleteListener(position);
                    }
                });
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
        ImageView delete;
        TextView accountDate;
        TextView accountNumber;
        TextView accountContent;
        TextView accountPerson;
        MyAccountHolder(View itemView) {
            super(itemView);
            delete = itemView.findViewById(R.id.delete_icon);
            accountDate = itemView.findViewById(R.id.account_date);
            accountNumber = itemView.findViewById(R.id.account_number);
            accountContent = itemView.findViewById(R.id.account_content);
            accountPerson = itemView.findViewById(R.id.account_person);
        }

    }

    class FooterViewHolder extends RecyclerView.ViewHolder{
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

    void filter(String filterKeyword){
        filteredAccounts.clear();
        if (filterKeyword.isEmpty()){
            filteredAccounts.addAll(mAccounts);
        }else {
            for (Account account : mAccounts){
                if (account.getSimpleString().toLowerCase().contains(filterKeyword)){
                    filteredAccounts.add(account);
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
