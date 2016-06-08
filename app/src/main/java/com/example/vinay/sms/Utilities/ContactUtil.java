package com.example.vinay.sms.Utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactUtil {

    private final Context context;

    public ContactUtil(Context context) {
        this.context = context;
    }

    public String getSenderName(String senderAddress) {
        if (senderAddress.charAt(0) < 60) {
            senderAddress = senderAddress.replaceAll("-", "");
            senderAddress = senderAddress.replaceAll(" +", "");
        }
        if (senderAddress.matches("^([+,.\\s0-9]*)([0-9]+)")) {
            senderAddress = getContactName(senderAddress);
        }
        return senderAddress;
    }

    public String getContactName(String number) {

        String name = null;

        // define the columns I want the query to return
        String[] projection = new String[]{
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID};

        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        // query time
        Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                //Contact Found
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            } else {
                //Contact not found
                name = number;
            }
            cursor.close();
        }
        return name;
    }
}
