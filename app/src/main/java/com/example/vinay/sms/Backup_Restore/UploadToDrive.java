package com.example.vinay.sms.Backup_Restore;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadToDrive {

    private final String DRIVE_ID = "driveId";

    @SuppressWarnings("FieldCanBeLocal")
    private final String FILE_SIZE = "fileSize";

    @SuppressWarnings("FieldCanBeLocal")
    private final String BACKUP_DATE = "date";

    private final String TAG = this.getClass().getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private Context context;

    public SharedPreferences pref;

    public SharedPreferences.Editor editor;

    public UploadToDrive(Context context) {
        this.context = context;
        initializeSharedPreference();
    }

    public void initializeSharedPreference() {
        pref = context.getSharedPreferences("DrivePref", Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    final public ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                    Log.d(TAG, "onResult: " + result.getStatus());
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    final File file = new File("/storage/emulated/0/MyBackUp.csv");

                    final byte[] bFile = new byte[(int) file.length()];

                    editor.putString(FILE_SIZE, getFileSize(bFile));
                    editor.putString(BACKUP_DATE, getDate());

                    // Perform I/O off the UI thread.
                    new Thread() {
                        @Override
                        public void run() {
                            // write content to DriveContents
                            OutputStream outputStream = driveContents.getOutputStream();

                            ByteArrayOutputStream csvStream = new ByteArrayOutputStream();

                            try {
                                FileInputStream fileInputStream = new FileInputStream(file);
                                fileInputStream.read(bFile);
                                fileInputStream.close();

                                csvStream.write(bFile);

                                outputStream.write(csvStream.toByteArray());
                                outputStream.close();

                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                        .setTitle("MyBackUp.csv")
                                        .setMimeType("text/csv")
                                        .setStarred(true).build();

                                // create a file on root folder
                                Drive.DriveApi.getRootFolder(getGoogleApiClient())
                                        .createFile(getGoogleApiClient(), changeSet, driveContents)
                                        .setResultCallback(fileCallback);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            };

    final public ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(@NonNull DriveFolder.DriveFileResult result) {

                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the file");
                        return;
                    }
                    showMessage("Created a file with content: " + result.getDriveFile().getDriveId());

                    if (pref.getString(DRIVE_ID, null) != null) {

                        new ResetAsyncTask(result).execute();

                    } else {
                        editor.putString(DRIVE_ID, String.valueOf(result.getDriveFile().getDriveId())).apply();
                    }
                }
            };

    /**
     * Getter for the {@code GoogleApiClient}.
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Setter for the {@code GoogleApiClient}.
     */
    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * AsyncTask to delete app data file.
     */
    private class ResetAsyncTask extends AsyncTask<Void, Void, Void> {

        DriveFolder.DriveFileResult result;

        public ResetAsyncTask(DriveFolder.DriveFileResult result) {
            this.result = result;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final String driveIdStr = pref.getString(DRIVE_ID, null);
            if (driveIdStr != null) {
                DriveId fileId = DriveId.decodeFromString(driveIdStr);
                DriveFile sumFile = fileId.asDriveFile();
                // Call to delete app data file. Consider using DriveResource.trash()
                // for user visible files.
                com.google.android.gms.common.api.Status deleteStatus =
                        sumFile.delete(mGoogleApiClient).await();
                if (!deleteStatus.isSuccess()) {
                    Log.e(TAG, "Unable to delete app data.");
                    return null;
                }
                // Remove stored DriveId.
                pref.edit().remove(DRIVE_ID).apply();
                Log.d(TAG, "Past files deleted.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            editor.putString(DRIVE_ID, String.valueOf(result.getDriveFile().getDriveId())).apply();
        }
    }

    public String getDate() {
        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz", Locale.ENGLISH);
        return ft.format(dNow);
    }

    public String getFileSize(byte[] bFile) {
        return String.valueOf(((double) bFile.length / 1000)).substring(0, 3) + " KB";
    }
}
