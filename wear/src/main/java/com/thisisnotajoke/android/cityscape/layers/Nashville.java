package com.thisisnotajoke.android.cityscape.layers;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.thisisnotajoke.android.cityscape.FaceLayer;
import com.thisisnotajoke.android.cityscape.R;
import com.thisisnotajoke.android.cityscape.Sun;
import com.thisisnotajoke.android.cityscape.SunColors;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.WGS84Point;

public class Nashville extends FaceLayer {
    private static final BoundingBox BOUNDING_BOX = new BoundingBox(36.262914, 35.888864, -86.975785, -86.405870);
    private static final String TAG = "Nashville";
    private final Drawable mBuilding;

    public Nashville(Resources resources) {
        mBuilding = resources.getDrawable(R.drawable.nashville, null);
    }

    @Override
    public void draw(Canvas canvas, Rect bounds) {
        mBuilding.setBounds(bounds.width() / 2, bounds.height() / 2, bounds.right, bounds.bottom);
        mBuilding.draw(canvas);
    }

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);
        mBuilding.clearColorFilter();
        if(mAmbient) {
            mBuilding.setColorFilter(SunColors.AMBIENT_FILTER);
        } else {
            onSunUpdated(mSun);
        }
    }

    @Override
    public void onSunUpdated(Sun sun) {
        super.onSunUpdated(sun);
        mBuilding.clearColorFilter();
        switch (sun) {
            case SUNSET:
            case SUNRISE:
                mBuilding.setColorFilter(SunColors.CHANGE_FILTER);
                break;
            case NIGHT:
                mBuilding.setColorFilter(SunColors.NIGHT_FILTER);
        }
    }

    public static boolean contains(WGS84Point point) {
        return BOUNDING_BOX.contains(point);
    }
}
