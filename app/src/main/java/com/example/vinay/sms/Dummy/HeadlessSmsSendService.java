package com.example.vinay.sms.Dummy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Dummy service to make sure this app can be default SMS app
 */
public class HeadlessSmsSendService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}