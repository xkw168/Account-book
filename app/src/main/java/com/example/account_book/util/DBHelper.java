package com.example.account_book.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.account_book.DailyAccount;
import com.example.account_book.Journey;
import com.example.account_book.JourneyAccount;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper{

    private static final String TAG = DBHelper.class.getSimpleName();

    // Database Version
    private static final int DATABASE_VERSION = 3;
    // Database Name
    private static final String DATABASE_NAME = "ACCOUNT_DATA";

    // field for task table
    private static final String DAILY_ACCOUNT_TABLE = "daily_account_table";
    private static final String JOURNEY_ACCOUNT_TABLE = "journey_account_table";
    private static final String JOURNEY_TABLE = "journey_table";
    private static final String ID = "id";
    private static final String JOURNEY_ID = "journey_id";
    private static final String CREATE_TIME = "create_time";
    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";
    private static final String PERSON = "person";
    private static final String AMOUNT = "amount";
    private static final String TOTAL_AMOUNT = "total_amount";
    private static final String DESTINATION = "destination";
    private static final String CONTENT = "content";
    private static final String CURRENCY_TYPE = "currency_type";

    private static final String[] JOURNEY_ACCOUNT_COLUMNS = {ID, CREATE_TIME, CURRENCY_TYPE, AMOUNT, CONTENT, PERSON, JOURNEY_ID};
    private static final String[] JOURNEY_COLUMNS = {ID, START_DATE, END_DATE, DESTINATION, PERSON, TOTAL_AMOUNT};
    private static final String[] DAILY_ACCOUNT_COLUMNS = {ID, CREATE_TIME, CURRENCY_TYPE, AMOUNT, CONTENT};

    //Constructor
    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String dailyTable = "CREATE TABLE " + DAILY_ACCOUNT_TABLE + "("
                + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,"
                + CREATE_TIME + " TEXT NOT NULL,"
                + CURRENCY_TYPE + " TEXT NOT NULL,"
                + AMOUNT + " REAL NOT NULL,"
                + CONTENT + " TEXT)";
        String journeyTable = "CREATE TABLE " + JOURNEY_TABLE + "("
                + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,"
                + START_DATE + "TEXT NOT NULL,"
                + END_DATE + "TEXT NOT NULL,"
                + DESTINATION + "TEXT NOT NULL,"
                + PERSON + "TEXT NOT NULL,"
                + TOTAL_AMOUNT + "REAL NOT NULL)";
        String journeyAccountTable = "CREATE TABLE " + JOURNEY_ACCOUNT_TABLE + "("
                + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,"
                + CREATE_TIME + " TEXT NOT NULL,"
                + CURRENCY_TYPE + " TEXT NOT NULL,"
                + AMOUNT + " REAL NOT NULL,"
                + CONTENT + " TEXT,"
                + PERSON + "TEXT NOT NULL,"
                + JOURNEY_ID + " INTEGER NOT NULL)";
        db.execSQL(dailyTable);
        db.execSQL(journeyTable);
        db.execSQL(journeyAccountTable);
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
        db.execSQL("DROP TABLE IF EXISTS " + DAILY_ACCOUNT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + JOURNEY_ACCOUNT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + JOURNEY_TABLE);
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS data");
        // Create tables again
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldV, int newV ){
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DAILY_ACCOUNT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + JOURNEY_ACCOUNT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + JOURNEY_TABLE);
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS data");
        // Create tables again
        onCreate(db);
    }

    public void addDailyAccount(DailyAccount dailyAccount){
        SQLiteDatabase db = this.getWritableDatabase();
        // Insert into the database by each row of the table
        ContentValues values = new ContentValues();
        values.put(CREATE_TIME, dailyAccount.getCreateTime());
        values.put(CURRENCY_TYPE, dailyAccount.getCurrencyType());
        values.put(AMOUNT, dailyAccount.getAmount());
        values.put(CONTENT, dailyAccount.getContent());
        long id = db.insert(DAILY_ACCOUNT_TABLE, null, values);
        Log.e(TAG, "addDailyAccount: " + id);
        db.close();
    }

    public void addJourneyAccount(JourneyAccount journeyAccount){
        SQLiteDatabase db = this.getWritableDatabase();
        // Insert into the database by each row of the table
        ContentValues values = new ContentValues();
        values.put(CREATE_TIME, journeyAccount.getCreateTime());
        values.put(CURRENCY_TYPE, journeyAccount.getCurrencyType());
        values.put(AMOUNT, journeyAccount.getAmount());
        values.put(CONTENT, journeyAccount.getContent());

        values.put(JOURNEY_ID, journeyAccount.getJourneyId());
        values.put(PERSON, journeyAccount.getPerson());
        long id = db.insert(JOURNEY_ACCOUNT_TABLE, null, values);
        Log.e(TAG, "addJourneyAccount: " + id);
        db.close();
    }

    public void addJourney(Journey journey){
        SQLiteDatabase db = this.getWritableDatabase();
        // Insert into the database by each row of the table
        ContentValues values = new ContentValues();
        values.put(START_DATE, journey.getStartDate());
        values.put(DESTINATION, journey.getDestination());
        values.put(PERSON, journey.getMember());
        long id = db.insert(JOURNEY_TABLE, null, values);
        Log.e(TAG, "addJourney: " + id);
        db.close();
    }

    public void deleteAccount(int accountID, boolean isDailyAccount){
        SQLiteDatabase db = this.getWritableDatabase();
        // Insert into the database by each row of the table
        db.delete(isDailyAccount ? DAILY_ACCOUNT_TABLE : JOURNEY_ACCOUNT_TABLE, ID + "=?", new String[]{String.valueOf(accountID)});
        Log.e(TAG, isDailyAccount ? "deleteDailyAccount: " : "deleteJourneyAccount: " + accountID);
        db.close();
    }

    public List<DailyAccount> queryDailyAccount(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(DAILY_ACCOUNT_TABLE, DAILY_ACCOUNT_COLUMNS, null, null, null, null, null);

        if (cursor.getCount() > 0) {
            List<DailyAccount> dailyAccounts = new ArrayList<DailyAccount>(cursor.getCount());
            while (cursor.moveToNext()) {
                DailyAccount dailyAccount = parseDailyAccount(cursor);
                dailyAccounts.add(dailyAccount);
            }
            return dailyAccounts;
        }

        return null;
    }

    public List<JourneyAccount> queryJourneyAccount(int journeyID){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(DAILY_ACCOUNT_TABLE, JOURNEY_ACCOUNT_COLUMNS, JOURNEY_ID, new String[journeyID], null, null, null);

        if (cursor.getCount() > 0) {
            List<JourneyAccount> journeyAccounts = new ArrayList<JourneyAccount>(cursor.getCount());
            while (cursor.moveToNext()) {
                JourneyAccount journeyAccount = parseJourneyAccount(cursor);
                journeyAccounts.add(journeyAccount);
            }
            return journeyAccounts;
        }

        return null;
    }

    public List<Journey> queryJourney(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(JOURNEY_TABLE, JOURNEY_COLUMNS, null, null, null, null, null);

        if (cursor.getCount() > 0) {
            List<Journey> journeys = new ArrayList<Journey>(cursor.getCount());
            while (cursor.moveToNext()) {
                Journey journey = parseJourney(cursor);
                journeys.add(journey);
            }
            return journeys;
        }

        return null;
    }

    private DailyAccount parseDailyAccount(Cursor cursor){
        DailyAccount dailyAccount = new DailyAccount();
        dailyAccount.setId(Integer.valueOf(cursor.getString(cursor.getColumnIndex(ID))));
        dailyAccount.setCreateTime(cursor.getString(cursor.getColumnIndex(CREATE_TIME)));
        dailyAccount.setCurrencyType(cursor.getString(cursor.getColumnIndex(CURRENCY_TYPE)));
        dailyAccount.setAmount(Double.valueOf(cursor.getString(cursor.getColumnIndex(AMOUNT))));
        dailyAccount.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
        return dailyAccount;
    }

    private JourneyAccount parseJourneyAccount(Cursor cursor){
        JourneyAccount journeyAccount = new JourneyAccount();
        journeyAccount.setId(Integer.valueOf(cursor.getString(cursor.getColumnIndex(ID))));
        journeyAccount.setJourneyId(Integer.valueOf(cursor.getString(cursor.getColumnIndex(JOURNEY_ID))));
        journeyAccount.setCreateTime(cursor.getString(cursor.getColumnIndex(CREATE_TIME)));
        journeyAccount.setCurrencyType(cursor.getString(cursor.getColumnIndex(CURRENCY_TYPE)));
        journeyAccount.setAmount(Double.valueOf(cursor.getString(cursor.getColumnIndex(AMOUNT))));
        journeyAccount.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
        journeyAccount.setPerson(cursor.getString(cursor.getColumnIndex(PERSON)));
        return journeyAccount;
    }

    private Journey parseJourney(Cursor cursor){
        Journey journey = new Journey();
        journey.setId(Integer.valueOf(cursor.getString(cursor.getColumnIndex(ID))));
        journey.setDestination(cursor.getString(cursor.getColumnIndex(DESTINATION)));
        journey.setMember(cursor.getString(cursor.getColumnIndex(PERSON)));
        journey.setTotalAmount(Double.valueOf(cursor.getString(cursor.getColumnIndex(TOTAL_AMOUNT))));
        journey.setStartDate(cursor.getString(cursor.getColumnIndex(START_DATE)));
        journey.setEndDate(cursor.getString(cursor.getColumnIndex(END_DATE)));
        return journey;
    }
}