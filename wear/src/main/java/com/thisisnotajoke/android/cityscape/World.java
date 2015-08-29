package com.thisisnotajoke.android.cityscape;

import android.content.res.Resources;
import android.util.Log;

import com.thisisnotajoke.android.cityscape.layers.Atlanta;
import com.thisisnotajoke.android.cityscape.layers.Nashville;
import com.thisisnotajoke.android.cityscape.layers.Rural;

import ch.hsr.geohash.WGS84Point;

public class World {
    private static final String TAG = "World";

    public static FaceLayer getCurrentCityFace(Resources resources, WGS84Point location) {
        if(location == null) {
            return new Rural();
        }
        if(Atlanta.contains(location)) {
            Log.d(TAG, "Detected Atlanta");
            return new Atlanta(resources);
        } else if(Nashville.contains(location)) {
            Log.d(TAG, "Detected Nashville");
            return new Nashville(resources);
        }
        Log.w(TAG, "We don't know that part of the world yet, defaulting to rural");
        return new Rural();
    }
}
