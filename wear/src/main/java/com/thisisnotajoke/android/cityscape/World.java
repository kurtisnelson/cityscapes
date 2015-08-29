package com.thisisnotajoke.android.cityscape;

import android.content.res.Resources;
import android.util.Log;

import com.thisisnotajoke.android.cityscape.contexts.Atlanta;
import com.thisisnotajoke.android.cityscape.contexts.Nashville;
import com.thisisnotajoke.android.cityscape.contexts.Rural;

import ch.hsr.geohash.WGS84Point;

public class World {
    private static final String TAG = "World";

    public static LocationFace getCurrentCityContextFace(Resources resources, WGS84Point location) {
        if(Atlanta.contains(location)) {
            Log.d(TAG, "Detected Atlanta");
            return new Atlanta(resources);
        } else if(Nashville.contains(location)) {
            Log.d(TAG, "Detected Nashville");
            return new Nashville();
        }
        Log.w(TAG, "We don't know the city, defaulting to rural");
        return new Rural();
    }
}
