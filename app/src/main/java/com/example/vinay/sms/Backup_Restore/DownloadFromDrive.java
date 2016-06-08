package com.example.vinay.sms.Backup_Restore;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.vinay.sms.Messaging.Display.SmsDisplayFragment;
import com.example.vinay.sms.Utilities.DatabaseHandler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadFromDrive {

    private final String TAG = this.getClass().getSimpleName();

    GoogleApiClient mGoogleApiClient;

    Context context;

    SharedPreferences pref;

    SharedPreferences.Editor editor;

    private static final String DRIVE_ID = "driveId";

    private ProgressDialog mProgress;

    private DatabaseHandler db;

    public DownloadFromDrive(Context context) {
        this.context = context;
        initializeSharedPreference();
        mProgress = new ProgressDialog(context);
        mProgress.setMessage("Restoring Backup");
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        db = new DatabaseHandler(context);
    }

    public void initializeSharedPreference() {
        pref = context.getSharedPreferences("DrivePref", Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void open() {
        // Reset progress dialog back to zero as we're
        // initiating an opening request.

        mProgress.show();

        DriveFile.DownloadProgressListener listener = new DriveFile.DownloadProgressListener() {
            @Override
            public void onProgress(long bytesDownloaded, long bytesExpected) {
                //Using Spinner so no need to show progress
            }
        };
        String driveIdStr = pref.getString(DRIVE_ID, null);
        assert driveIdStr != null;
        /*
      File that is selected with the open file activity.
     */
        Log.d(TAG, "open: " + driveIdStr);
        DriveId mSelectedFileDriveId = DriveId.decodeFromString(driveIdStr);
        DriveFile driveFile = mSelectedFileDriveId.asDriveFile();

//        DriveFile appFolderFile = Drive.DriveApi.getFile(mGoogleApiClient, driveId);

        driveFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, listener)
                .setResultCallback(driveContentsCallback);
    }

    private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while opening the file contents");
                        return;
                    }
                    InputStream inputStream = result.getDriveContents().getInputStream();
                    try {
                        OutputStream os = new FileOutputStream("/storage/emulated/0/MyBackUp.csv");
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        //read from is to buffer
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        inputStream.close();
                        //flush OutputStream to write any buffered data to file
                        os.flush();
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    LocalImport localImport = new LocalImport();
                    localImport.importFromCSV(db, context);
                    SmsDisplayFragment smsDisplayFragment = new SmsDisplayFragment();
                    smsDisplayFragment.updateList();
                    mProgress.hide();
                    showMessage("Backup Restored Successfully");

                    result.getDriveContents().discard(mGoogleApiClient);
                }
            };

    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

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
}
