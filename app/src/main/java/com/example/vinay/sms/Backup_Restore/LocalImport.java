package com.example.vinay.sms.Backup_Restore;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.Utilities.DatabaseHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static com.example.vinay.sms.Constants.DB_Constants.KEY_ADDRESS;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_BODY;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_DATE;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_ID;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_NUMBER;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_READ_STATUS;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_SENT_STATUS;
import static com.example.vinay.sms.Constants.DB_Constants.KEY_TYPE;

public class LocalImport {

    private final String TAG = this.getClass().getSimpleName();

    public void importFromCSV(DatabaseHandler db, Context context) {

        String filepath = "/storage/emulated/0/MyBackUp.csv";
        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
        String tableName = "_INBOX";
        sqLiteDatabase.execSQL("DELETE FROM " + tableName);
        try {
            try {

                FileReader file = new FileReader(filepath);

                BufferedReader buffer = new BufferedReader(file);
                ContentValues contentValues = new ContentValues();
                String line;
                sqLiteDatabase.beginTransaction();

                while ((line = buffer.readLine()) != null) {
                    // defining 3 columns with null or blank field //values acceptance
                    String[] str = line.split(",", 8);
                    String count = str[0];
                    String address = str[1];
                    String date = str[2];
                    String body = str[3];
                    body = body.replaceAll("\\\\r", "\r").replaceAll("\\\\n", "\n");
                    body = body.replaceAll("\\\\`", ",");
                    String type = str[4];
                    String number = str[5];
                    String read = str[6];
                    String sent = str[7];

                    contentValues.put(KEY_ID, count);
                    contentValues.put(KEY_ADDRESS, address);
                    contentValues.put(KEY_DATE, date);
                    contentValues.put(KEY_BODY, body);
                    contentValues.put(KEY_TYPE, type);
                    contentValues.put(KEY_NUMBER, number);
                    contentValues.put(KEY_READ_STATUS, read);
                    contentValues.put(KEY_SENT_STATUS, sent);

                    Log.d(TAG, "importFromCSV: " + count);

                    sqLiteDatabase.insert(tableName, null, contentValues);
                }
                Toast.makeText(context, "Successfully Updated Database."
                        , Toast.LENGTH_SHORT).show();
                sqLiteDatabase.setTransactionSuccessful();
                sqLiteDatabase.endTransaction();
            } catch (IOException e) {
                if (sqLiteDatabase.inTransaction())
                    sqLiteDatabase.endTransaction();
                Dialog d = new Dialog(context);
                d.setTitle(e.getMessage() + "first");
                d.show();
                // db.endTransaction();
            }
        } catch (Exception ex) {
            if (sqLiteDatabase.inTransaction())
                sqLiteDatabase.endTransaction();

            Dialog d = new Dialog(context);
            d.setTitle(ex.getMessage() + "second");
            d.show();
            // db.endTransaction();
        }
        List<SMS> myList = db.getAllMessages(null);
        Log.d(TAG, "importFromCSV: " + myList.size());
    }
}
