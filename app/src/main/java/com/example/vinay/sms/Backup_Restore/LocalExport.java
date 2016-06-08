package com.example.vinay.sms.Backup_Restore;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class LocalExport {

    private final String TAG = this.getClass().getSimpleName();

    public void exportToCSV(SQLiteDatabase sqldb, Context context) {
        Cursor c = null;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            c = sqldb.rawQuery("SELECT * FROM _INBOX", null);
            Log.d(TAG, "exportToCSV: " + c.getCount());
            int rowCount;
            int colCount;
            File sdCardDir = Environment.getExternalStorageDirectory();
            String filename = "MyBackUp.csv";
            // the name of the file to export with
            File saveFile = new File(sdCardDir, filename);
            FileWriter fw = new FileWriter(saveFile);

            BufferedWriter bw = new BufferedWriter(fw);
            rowCount = c.getCount();
            colCount = c.getColumnCount();
            if (rowCount > 0) {
                c.moveToFirst();

                for (int i = 0; i < rowCount; i++) {
                    c.moveToPosition(i);

                    for (int j = 0; j < colCount; j++) {
                        String data = c.getString(j).replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r");
                        data = data.replaceAll(",", "\\\\`");
                        if (j != colCount - 1)
                            bw.write(data + ",");
                        else
                            bw.write(data);
                        Log.d(TAG, "exportToCSV: " + data);
                    }
                    bw.newLine();
                }
                bw.flush();
                Toast.makeText(context, "Exported Successfully."
                        , Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            if (sqldb.isOpen()) {
                sqldb.close();
                Toast.makeText(context, ex.getMessage()
                        , Toast.LENGTH_SHORT).show();
            }
        } finally {
            assert c != null;
            c.close();
        }
    }
}
