package com.thisisnotajoke.android.cityscape.lib.model;

import android.content.res.Resources;
import android.util.Log;

import com.thisisnotajoke.android.cityscape.lib.FaceLayer;
import com.thisisnotajoke.android.cityscape.lib.layer.Rural;

import java.util.UUID;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.WGS84Point;

public class City {
    private static final String TAG = "City";
    private UUID mID;
    private String mName;
    private Class<? extends FaceLayer> mFace;
    private BoundingBox mBoundingBox;

    public City(String id, String name, Class<? extends FaceLayer> faceClass, BoundingBox box) {
        this(id, name, faceClass);
        mBoundingBox = box;
    }
    public City(String id, String name, Class<? extends FaceLayer> faceClass) {
        this(id, name);
        mFace = faceClass;
    }

    public City(String id, String name) {
        if(id != null)
            mID = UUID.fromString(id);
        mName = name;
    }

    public UUID getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return mName;
    }

    public FaceLayer getFace(Resources res) {
        try {
            return mFace.getConstructor(Resources.class).newInstance(res);
        } catch (Exception e) {
            Log.e(TAG, "Could not build watch face", e);
            return new Rural();
        }
    }

    public boolean contains(WGS84Point point) {
        return mBoundingBox.contains(point);
    }
}