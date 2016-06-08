package com.example.vinay.sms.Backup_Restore;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.vinay.sms.Constants.Network_Constants;

public class NetworkStatus {

    public int chkStatus(Context context) {

        final ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isWiFi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        boolean isCellular = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;

        if (isWiFi) {
            return Network_Constants.CONNECTED_VIA_WIFI;
        } else if (isCellular) {
            return Network_Constants.CONNECTED_VIA_CELLULAR;
        } else {
            return Network_Constants.NOT_CONNECTED;
        }
    }
}
