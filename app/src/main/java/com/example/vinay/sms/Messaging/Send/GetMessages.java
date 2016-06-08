package com.example.vinay.sms.Messaging.Send;

import android.content.Context;
import android.database.Cursor;

import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.Utilities.ContactUtil;
import com.example.vinay.sms.Utilities.DatabaseHandler;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class GetMessages {

    private ContactUtil contactUtil;
    private DatabaseHandler db;
    public GetMessages(Context context) {
        contactUtil = new ContactUtil(context);
        db = new DatabaseHandler(context);
    }

    public void getMessgaes(Cursor cursor1, LinkedHashSet<SMS> uniquelinkedHashSet,
                            final LinkedHashSet<SMS> linkedHashSet,
                            HashMap<String, Integer> countOfMessages,
                            String sentStatus, final String tableName) {

        LinkedHashSet<String> senderHashSet = new LinkedHashSet<>();
        int count = 0;
        String[] columns = new String[]{"address", "person", "date", "body", "type", "read"};
        assert cursor1 != null;
        if (cursor1.getCount() > 0) {
            while (cursor1.moveToNext()) {
                String sender = cursor1.getString(cursor1.getColumnIndex(columns[0]));
//                String name = cursor1.getString(cursor1.getColumnIndex(columns[1]));
                String date = cursor1.getString(cursor1.getColumnIndex(columns[2]));
                String msg = cursor1.getString(cursor1.getColumnIndex(columns[3]));
                String type = cursor1.getString(cursor1.getColumnIndex(columns[4]));
                String read = cursor1.getString(cursor1.getColumnIndex(columns[5]));
                String senderAddress = contactUtil.getSenderName(sender);
                SMS m = new SMS(senderAddress, date, msg, type, sender, read, sentStatus);

                if (!senderHashSet.contains(sender)) {
                    uniquelinkedHashSet.add(m);
                    senderHashSet.add(sender);
                }
                if (Integer.parseInt(read) == 0) {
                    count = 0;
                    if (countOfMessages.containsKey(senderAddress)) {
                        count = countOfMessages.get(senderAddress);
                    }
                    countOfMessages.put(senderAddress, ++count);
                }
                linkedHashSet.add(m);
            }
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            for (SMS eachMessage : linkedHashSet) {
                                String senderAddress = eachMessage.getSenderAddress();
                                String date = eachMessage.getDate();
                                String msg = eachMessage.getMessage();
                                String senderNumber = eachMessage.getSenderNumber();
                                String type = eachMessage.getType();
                                String readStatus = eachMessage.getReadStatus();
                                String sentStatus = eachMessage.getSentStatus();
                                db.addMessage(senderAddress, date, msg, type, senderNumber, readStatus, sentStatus, tableName);
                            }
                        }
                    }
                    // Starts the thread by calling the run() method in its Runnable
            ).start();
        }
    }

    public void updateList(HashMap<String, Integer> countOfMessages,
                           LinkedHashSet<SMS> uniquelinkedHashSet, String tableName) {

        countOfMessages.clear();
        int count = 0;
        LinkedHashSet<String> senderHashSet = new LinkedHashSet<>();
        List<SMS> list = db.getMessagesFromTable(tableName);
        for (SMS message : list) {
            String sender = message.getSenderAddress();
            String read = message.getReadStatus();
            if (!senderHashSet.contains(sender)) {
                uniquelinkedHashSet.add(message);
                senderHashSet.add(sender);
            }
            if (Integer.parseInt(read) == 0) {
                if (countOfMessages.containsKey(sender)) {
                    count = countOfMessages.get(sender);
                }
                countOfMessages.put(sender, ++count);
            }
        }
    }
}
