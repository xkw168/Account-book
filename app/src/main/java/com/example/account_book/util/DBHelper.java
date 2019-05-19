package com.example.account_book.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.account_book.Account;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper{

    private static final String TAG = DBHelper.class.getSimpleName();

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "ACCOUNT_DATA";

    // field for task table
    private static final String TABLE_NAME = "account";
    private static final String ID = "id";
    private static final String CREATE_TIME = "create_time";
    private static final String PERSON = "person";
    private static final String NUMBER = "number";
    private static final String CONTENT = "content";

    private static final String[] COLUMNS = {ID, CREATE_TIME, PERSON, NUMBER, CONTENT};

    //Constructor
    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String createTaskTable = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,"
                + CREATE_TIME + " INTEGER NOT NULL,"
                + PERSON + " TEXT NOT NULL,"
                + NUMBER + " REAL NOT NULL,"
                + CONTENT + " TEXT)";
        db.execSQL(createTaskTable);
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=1;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV ){
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS data");
        // Create tables again
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldV, int newV ){
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS data");
        // Create tables again
        onCreate(db);
    }

    public void addAccount(Account account){
        SQLiteDatabase db = this.getWritableDatabase();
        // Insert into the database by each row of the table
        ContentValues values = new ContentValues();
        values.put(CREATE_TIME, account.getCreateTime());
        values.put(NUMBER, account.getNumber());
        values.put(PERSON, account.getPerson());
        values.put(CONTENT, account.getContent());
        long id = db.insert(TABLE_NAME, null, values);
        Log.e(TAG, "addAccount: " + id);
        db.close();
    }

    public void deleteAccount(int accountID){
        SQLiteDatabase db = this.getWritableDatabase();
        // Insert into the database by each row of the table
        db.delete(TABLE_NAME, ID + "=?", new String[]{String.valueOf(accountID)});
        Log.e(TAG, "deleteAccount: " + accountID);
        db.close();
    }

    public List<Account> queryAllAccount(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, COLUMNS, null, null, null, null, null);

        if (cursor.getCount() > 0) {
            List<Account> accounts = new ArrayList<Account>(cursor.getCount());
            while (cursor.moveToNext()) {
                Account account = parseAccount(cursor);
                accounts.add(account);
            }
            return accounts;
        }

        return null;
    }

    private Account parseAccount(Cursor cursor){
        Account account = new Account();
        account.setId(Integer.valueOf(cursor.getString(cursor.getColumnIndex(ID))));
        account.setCreateTime(cursor.getString(cursor.getColumnIndex(CREATE_TIME)));
        account.setPerson(cursor.getString(cursor.getColumnIndex(PERSON)));
        account.setNumber(Double.valueOf(cursor.getString(cursor.getColumnIndex(NUMBER))));
        account.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
        return account;
    }
}