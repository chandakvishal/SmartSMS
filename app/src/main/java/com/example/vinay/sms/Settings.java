package com.example.vinay.sms;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.vinay.sms.Backup_Restore.DownloadFromDrive;
import com.example.vinay.sms.Backup_Restore.LocalExport;
import com.example.vinay.sms.Backup_Restore.NetworkStatus;
import com.example.vinay.sms.Backup_Restore.UploadToDrive;
import com.example.vinay.sms.Utilities.DatabaseHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import static com.example.vinay.sms.Constants.Network_Constants.CONNECTED_VIA_WIFI;
import static com.example.vinay.sms.Constants.Network_Constants.CONNECTED_VIA_CELLULAR;

public class Settings extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;

    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 100;

    private final String TAG = this.getClass().getSimpleName();

    DatabaseHandler db;

    String operation = "backup";

    private final String OPERATION_BACKUP = "backup";

    private final String OPERATION_RESTORE = "restore";

    private boolean canConnect = true;

    SharedPreferences pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        pref = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        boolean isDark = pref.getBoolean("isDark", true);
        if (isDark) {
            setTheme(R.style.AppThemeDark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        final SharedPreferences.Editor editor = pref.edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setHomeAsUpIndicator
                (isDark ? R.drawable.ic_arrow_back_white_48dp : R.drawable.ic_arrow_back_black_48dp);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.action_settings);


        Switch themeSwitch = (Switch) findViewById(R.id.lightThemeSwitch);
        assert themeSwitch != null;
        themeSwitch.setChecked(!isDark);
        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setTheme(R.style.AppTheme);
                    editor.putBoolean("isDark", false);
                    editor.apply();
                    restart();
                } else {
                    setTheme(R.style.AppThemeDark);
                    editor.putBoolean("isDark", true);
                    editor.apply();
                    restart();
                }
            }
        });

        Switch notificationSwitch = (Switch) findViewById(R.id.notificationSwitch);
        assert notificationSwitch != null;
        notificationSwitch.setChecked(pref.getBoolean("groupNotification", false));
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("groupNotification", true);
                    editor.apply();
                } else {
                    editor.putBoolean("groupNotification", false);
                    editor.apply();
                }
            }
        });

        Switch networkSwitch = (Switch) findViewById(R.id.backupPreferenceSwitch);
        assert networkSwitch != null;
        networkSwitch.setChecked(pref.getBoolean("syncOverCellular", false));
        networkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("syncOverCellular", true);
                    editor.apply();
                } else {
                    editor.putBoolean("syncOverCellular", false);
                    editor.apply();
                }
            }
        });

        db = new DatabaseHandler(getApplicationContext());

        pref = getSharedPreferences("DrivePref", Context.MODE_PRIVATE);
        String FILE_SIZE = "fileSize";
        String fileSize = pref.getString(FILE_SIZE, "0 KB");

        String BACKUP_DATE = "date";
        String date = pref.getString(BACKUP_DATE, "Last backup Not Found");

        TextView textView = (TextView) findViewById(R.id.recentTextView);

        String textToSet = "Backup Size: " + fileSize + "\nMost Recent Backup was done at: " + date;

        assert textView != null;
        textView.setText(textToSet);

        Button exportLocalButton = (Button) findViewById(R.id.exportLocalButton);
        assert exportLocalButton != null;
        exportLocalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportDB();
            }
        });

        TextView backupButton = (Button) findViewById(R.id.backupButton);
        assert backupButton != null;
        backupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportDB();
                operation = OPERATION_BACKUP;
                Log.d(TAG, "onOptionsItemSelected:BACKUP " + canConnect);
                if (canConnect && !mGoogleApiClient.isConnected() && canConnect()) {
                    mGoogleApiClient.connect();
                } else if (mGoogleApiClient.isConnected()) {
                    executeDriveOperations();
                }
            }
        });

        TextView restoreButton = (Button) findViewById(R.id.restoreButton);
        assert restoreButton != null;
        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operation = OPERATION_RESTORE;
                Log.d(TAG, "onOptionsItemSelected:RESTORE " + canConnect);
                if (canConnect && !mGoogleApiClient.isConnected() && canConnect()) {
                    mGoogleApiClient.connect();
                } else if (mGoogleApiClient.isConnected()) {
                    executeDriveOperations();
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                canConnect = resultCode == RESULT_OK;
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // create new contents resource
        executeDriveOperations();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution is
     * available.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    private void executeDriveOperations() {
        Log.d(TAG, "executeDriveOperations:OPERATION:: " + operation);
        if (operation.equals(OPERATION_BACKUP)) {
            UploadToDrive uploadToDrive;
            uploadToDrive = new UploadToDrive(this);
            uploadToDrive.setGoogleApiClient(getGoogleApiClient());
            Log.d(TAG, "executeDriveOperations: " + "Google Client Set");
            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                    .setResultCallback(uploadToDrive.driveContentsCallback);
        } else if (operation.equals(OPERATION_RESTORE)) {
            DownloadFromDrive downloadFromDrive = new DownloadFromDrive(this);
            downloadFromDrive.setGoogleApiClient(getGoogleApiClient());
            // Open the file and get its contents
            downloadFromDrive.open();
        }
    }

    private void exportDB() {
        SQLiteDatabase sqldb = db.getReadableDatabase(); //My Database class
        LocalExport localExport = new LocalExport();
        localExport.exportToCSV(sqldb, this);
    }

    private boolean canConnect() {
        NetworkStatus networkStatus = new NetworkStatus();
        int status = networkStatus.chkStatus(this);
        return status == CONNECTED_VIA_WIFI || status == CONNECTED_VIA_CELLULAR && pref.getBoolean("syncOverCellular", false);
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void restart() {
        this.finish();
        this.startActivity(new Intent(this, this.getClass()));
    }

}
