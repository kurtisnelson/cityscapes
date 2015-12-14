package com.thisisnotajoke.android.cityscape.lib;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;

public class ModeManager {

    private static void updateMode(GoogleApiClient googleApiClient, final int mode) {
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putInt(DataSyncUtil.KEY_MODE, mode);
        DataSyncUtil.overwriteKeysInConfigDataMap(googleApiClient, configKeysToOverwrite);
    }

    public static void setManual(GoogleApiClient googleApiClient) {
        updateMode(googleApiClient, DataSyncUtil.MODE_MANUAL);
    }

    public static void setRandom(GoogleApiClient googleApiClient) {
        updateMode(googleApiClient, DataSyncUtil.MODE_RANDOM);
    }

    public static void setGPS(GoogleApiClient googleApiClient) {
        updateMode(googleApiClient, DataSyncUtil.MODE_GPS);
    }
}
