package com.example.vinay.sms.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.vinay.sms.Messaging.SMS;

import java.util.ArrayList;
import java.util.List;

import static com.example.vinay.sms.Constants.DB_Constants.KEY_ADDRESS;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_BODY;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_DATE;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_ID;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_NUMBER;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_READ_STATUS;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_SENT_STATUS;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_TYPE;
import static com.example.vinay.sms.Constants.DB_Constants.TABLE_INBOX;
import static com.example.vinay.sms.Constants.DB_Constants.TABLE_SENT;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "bmc_local";

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
    public void addMessage(SMS sms, String TABLE_NAME) {

        SQLiteDatabase db = this.getWritableDatabase();

        String address = sms.getSenderAddress();
        String date = sms.getDate();
        String body = sms.getMessage();
        String type = sms.getType();
        String number = sms.getSenderNumber();
        String readStatus = sms.getReadStatus();
        String sentStatus = sms.getSentStatus();

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
        Log.i("Added User " + number, "Successfully Added User in" + TABLE_NAME + " Table");
        db.close(); // Closing database connection
    }

    /**
     * Storing user details in database
     */
    public void addMessage(String address, String date, String body,
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
        Log.i("Added User " + number, " AS " + address + "Successfully Added User in" + TABLE_NAME + " Table");
        db.close(); // Closing database connection
    }

    public void deleteSingleMessage(List<SMS> smsList, String TABLE_NAME) {

        SQLiteDatabase db = this.getWritableDatabase();

        for (SMS sms : smsList) {
            String address = sms.getSenderAddress();
            String body = sms.getMessage();

            String whereClause = null, whereArgs[] = new String[0];
            if (body != null) {
                whereClause = "address=? and body=?";
                whereArgs = new String[]{address, body};
            }

            // Deleting Row
            db.delete(TABLE_NAME, whereClause, whereArgs);
            Log.i("Deleted User " + address, "Successfully Deleted User from" + TABLE_NAME + " Table");
        }
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

        return queryDatabase(selectQuery);
    }

    /**
     * Getting messages data from database
     */
    public List<SMS> getAllMessages(String senderAddress) {

        String selectQueryForUser = "SELECT * FROM " + TABLE_INBOX
                + " WHERE NUMBER LIKE \"%" + senderAddress
                + "%\" UNION SELECT * FROM "
                + TABLE_SENT + " WHERE NUMBER LIKE \"%" + senderAddress + "%\"";

        String selectQueryForNull = "SELECT * FROM " + TABLE_INBOX
                + " UNION SELECT * FROM "
                + TABLE_SENT;

        String selectQuery = senderAddress != null ? selectQueryForUser : selectQueryForNull;

        return queryDatabase(selectQuery);
    }

    /**
     * Getting messages data from database
     */
    public List<SMS> getMessagesFromTable(String tableName) {

        return queryDatabase("SELECT * FROM " + tableName + " ORDER BY DATE DESC");
    }

    /**
     * Getting messages data from database
     */
    public int updateReadStatus(String tableName, String number) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_READ_STATUS, "1");

        Log.d("Database Handler", "updateReadStatus: Update Read status of: " + number);
        // updating row
        return db.update(tableName, values, KEY_NUMBER + " = ?",
                new String[] { String.valueOf(number) });
    }

    public List<SMS> queryDatabase(String selectQuery) {
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
}
