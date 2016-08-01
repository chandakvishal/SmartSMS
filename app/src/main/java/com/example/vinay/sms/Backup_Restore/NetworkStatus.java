package com.example.vinay.sms.Backup_Restore;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.vinay.sms.Constants.Network_Constants;

public class NetworkStatus {

    public int chkStatus(Context context) {

        final ConnectivityManager connMgr;
        NetworkInfo networkInfo = null;

        try {
            connMgr = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            networkInfo = connMgr.getActiveNetworkInfo();

        } catch (Exception e) {
            Log.e("connectivity", e.toString());
        }

        boolean isWiFi = false;
        boolean isCellular = false;
        if (networkInfo != null) {
            isWiFi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            isCellular = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }

        if (isWiFi) {
            return Network_Constants.CONNECTED_VIA_WIFI;
        } else if (isCellular) {
            return Network_Constants.CONNECTED_VIA_CELLULAR;
        } else {
            return Network_Constants.NOT_CONNECTED;
        }
    }
}
