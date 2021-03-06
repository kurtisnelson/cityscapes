package com.thisisnotajoke.android.cityscape;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public class DataSyncUtil {

  public static final String KEY_CITY = "CITY";
  public static final String KEY_MODE = "MODE";

  public static final int MODE_GPS = 0;
  public static final int MODE_MANUAL = 1;
  public static final int MODE_RANDOM = 2;

  public static final String PATH_WITH_FEATURE = "/watch_face_config";
  private static final String TAG = "DataSyncUtil";

  public interface FetchConfigDataMapCallback {

    void onConfigDataMapFetched(DataMap config);
  }

  public static void fetchConfigDataMap(final GoogleApiClient client,
      final FetchConfigDataMapCallback callback) {
    Wearable.NodeApi.getLocalNode(client).setResultCallback(
        new ResultCallback<NodeApi.GetLocalNodeResult>() {
          @Override
          public void onResult(@NonNull NodeApi.GetLocalNodeResult getLocalNodeResult) {
            String localNode = getLocalNodeResult.getNode().getId();
            Uri uri = new Uri.Builder()
                .scheme("wear")
                .path(DataSyncUtil.PATH_WITH_FEATURE)
                .authority(localNode)
                .build();
            Wearable.DataApi.getDataItem(client, uri)
                .setResultCallback(new DataItemResultCallback(callback));
          }
        }
    );
  }

  public static void overwriteKeysInConfigDataMap(final GoogleApiClient googleApiClient,
      final DataMap configKeysToOverwrite) {

    DataSyncUtil.fetchConfigDataMap(googleApiClient,
        new FetchConfigDataMapCallback() {
          @Override
          public void onConfigDataMapFetched(DataMap currentConfig) {
            DataMap overwrittenConfig = new DataMap();
            overwrittenConfig.putAll(currentConfig);
            overwrittenConfig.putAll(configKeysToOverwrite);
            DataSyncUtil.putConfigDataItem(googleApiClient, overwrittenConfig);
          }
        }
    );
  }

  private static void putConfigDataItem(GoogleApiClient googleApiClient, DataMap newConfig) {
    PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE);
    putDataMapRequest.setUrgent();
    DataMap configToPut = putDataMapRequest.getDataMap();
    configToPut.putAll(newConfig);
    Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
        .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
          @Override
          public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
              Log.d(TAG, "putDataItem result status: " + dataItemResult.getStatus());
            }
          }
        });
  }

  private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {

    private final FetchConfigDataMapCallback mCallback;

    DataItemResultCallback(FetchConfigDataMapCallback callback) {
      mCallback = callback;
    }

    @Override
    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
      if (dataItemResult.getStatus().isSuccess()) {
        if (dataItemResult.getDataItem() != null) {
          DataItem configDataItem = dataItemResult.getDataItem();
          DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
          DataMap config = dataMapItem.getDataMap();
          mCallback.onConfigDataMapFetched(config);
        } else {
          mCallback.onConfigDataMapFetched(new DataMap());
        }
      }
    }
  }
}
