package com.thisisnotajoke.android.cityscape.contexts;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.thisisnotajoke.android.cityscape.LocationFace;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.WGS84Point;

public class Nashville extends LocationFace {
    private static final BoundingBox BOUNDING_BOX = new BoundingBox(36.262914, 35.888864, -86.975785, -86.405870);

    @Override
    public void draw(Canvas canvas, Rect bounds) {

    }

    public static boolean contains(WGS84Point point) {
        return BOUNDING_BOX.contains(point);
    }
}
