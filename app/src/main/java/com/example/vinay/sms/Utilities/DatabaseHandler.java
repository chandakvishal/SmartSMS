package com.example.vinay.sms.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.vinay.sms.Messaging.SMS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "bmc_local";
    private static final String KEY_ID = "_id";
    private static final String KEY_ADDRESS = "ADDRESS";
    private static final String KEY_DATE = "DATE";
    private static final String KEY_BODY = "BODY";
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_NUMBER = "NUMBER";
    private static final String KEY_READ_STATUS = "READ";
    private static final String KEY_SENT_STATUS = "SENT";
    private static final String TABLE_INBOX = "_INBOX";
    private static final String TABLE_SENT = "_SENT";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_INBOX_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_INBOX + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_ADDRESS + " TEXT, "
                + KEY_DATE + " TEXT, "
                + KEY_BODY + " TEXT UNIQUE, "
                + KEY_TYPE + " TEXT, "
                + KEY_NUMBER + " TEXT, "
                + KEY_READ_STATUS + " TEXT, "
                + KEY_SENT_STATUS + " TEXT" + ")";

        String CREATE_SENT_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SENT + " ("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_ADDRESS + " TEXT, "
                + KEY_DATE + " TEXT, "
                + KEY_BODY + " TEXT UNIQUE, "
                + KEY_TYPE + " TEXT, "
                + KEY_NUMBER + " TEXT, "
                + KEY_READ_STATUS + " TEXT, "
                + KEY_SENT_STATUS + " TEXT" + ")";

        db.execSQL(CREATE_INBOX_TABLE);
        db.execSQL(CREATE_SENT_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHandler.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INBOX);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENT);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     */
    public void addUser(String address, String date, String body,
                        String type, String number, String readStatus,
                        String sentStatus, String TABLE_NAME) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ADDRESS, address); // SenderAddress
        values.put(KEY_DATE, date); // Date of Message
        values.put(KEY_BODY, body); // Message Body
        values.put(KEY_TYPE, type); // Message Type
        values.put(KEY_NUMBER, number); // SenderNumber
        values.put(KEY_READ_STATUS, readStatus); // Read Status of the Messages
        values.put(KEY_SENT_STATUS, sentStatus); // Sent Status of the Messages

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        Log.i("Added User", "Successfully Added User in" + TABLE_NAME + " Table");
        db.close(); // Closing database connection
    }

    /**
     * Getting messages data from database
     */
    public List<String> searchTable(String queryString, String tableName) {
        String selectQuery = "SELECT _id, ADDRESS FROM " + tableName + " WHERE ( "
                + KEY_ADDRESS + " LIKE '%" + queryString + "%' ) UNION "
                + "SELECT _id, BODY FROM " + tableName + " WHERE ( "
                + KEY_BODY + " LIKE '%" + queryString + "%' ) UNION "
                + "SELECT _id, NUMBER FROM " + tableName + " WHERE ( "
                + KEY_NUMBER + " LIKE '%" + queryString + "%' )";
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("TAG", "searchTable: " + selectQuery);

        List<String> list = new ArrayList<>();

        Cursor cursor = db.rawQuery(selectQuery, null);

        //To add all elements of the cursor in a list (used in search)
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            // The Cursor is now set to the right position
            String address = cursor.getString(1);
            if (!list.contains(address)) {
                list.add(address);
            }
        }
        cursor.close();
        return list;
    }

    /**
     * Getting messages data from database
     */
    public List<SMS> getMessageDetails(String queryString, String tableName) {

        String selectQuery = "SELECT _id, " + KEY_ADDRESS + "," + KEY_DATE
                + "," + KEY_BODY + "," + KEY_TYPE + "," + KEY_NUMBER
                + "," + KEY_READ_STATUS + "," + KEY_SENT_STATUS
                + " FROM " + tableName + " WHERE ( "
                + " ( " + KEY_ADDRESS + " LIKE '%" + queryString + "%' ) OR "
                + " ( " + KEY_BODY + " LIKE '%" + queryString + "%' ) OR "
                + " ( " + KEY_NUMBER + " LIKE '%" + queryString + "%' ))";

        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("TAG", "searchTable: " + selectQuery);

        List<SMS> list = new ArrayList<>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("TAG", "searchTable: " + cursor.getCount());

        //To add all elements of the cursor in a list (used in search)
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            // The Cursor is now set to the right position
            String address = cursor.getString(1);
            String date = cursor.getString(2);
            String body = cursor.getString(3);
            String type = cursor.getString(4);
            String number = cursor.getString(5);
            String read = cursor.getString(6);
            String sent = cursor.getString(7);
            SMS sms = new SMS(address, date, body, type, number, read, sent);
            list.add(sms);
        }
        cursor.close();
        return list;
    }

    /**
     * Getting messages data from database
     */
    public List<SMS> getAllMessages(String senderAddress) {

        String selectQuery = "SELECT * FROM " + TABLE_INBOX
                + " WHERE ADDRESS = \"" + senderAddress
                + "\" UNION SELECT * FROM "
                + TABLE_SENT + " WHERE ADDRESS = \"" + senderAddress + "\"";

        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("TAG", "searchTable: " + selectQuery);

        List<SMS> list = new ArrayList<>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("TAG", "searchTable: " + cursor.getCount());
        Log.d("TAG", "searchTable: " + Arrays.toString(cursor.getColumnNames()));

        //To add all elements of the cursor in a list (used in search)
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            // The Cursor is now set to the right position
            String address = cursor.getString(1);
            String date = cursor.getString(2);
            String body = cursor.getString(3);
            String type = cursor.getString(4);
            String number = cursor.getString(5);
            String read = cursor.getString(6);
            String sent = cursor.getString(7);
            Log.d("TAG", "getAllMessages: " + body);
            SMS sms = new SMS(address, date, body, type, number, read, sent);
            list.add(sms);
        }
        cursor.close();
        return list;
    }

    /**
     * Re create database
     * Delete all tables and create them again
     */
    public void resetUserTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_INBOX, null, null);
        db.delete(TABLE_SENT, null, null);
        Log.d("Database Handler", "resetUserTables: Tables Deleted Successfully");
        db.close();
    }

    public boolean deleteDatabase(Context context) {
        Log.d("Database Handler", "resetUserTables: Databse Deleted Successfully");
        return context.deleteDatabase(DATABASE_NAME);
    }

}
