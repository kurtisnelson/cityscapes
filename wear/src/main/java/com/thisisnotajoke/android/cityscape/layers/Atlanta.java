package com.thisisnotajoke.android.cityscape.layers;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.thisisnotajoke.android.cityscape.FaceLayer;
import com.thisisnotajoke.android.cityscape.R;
import com.thisisnotajoke.android.cityscape.Sun;
import com.thisisnotajoke.android.cityscape.SunColors;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.WGS84Point;

public class Atlanta extends FaceLayer {
    private static final BoundingBox BOUNDING_BOX = new BoundingBox(34.080341, 33.411764, -84.699899, -83.997117);
    private final Drawable mBuilding;

    public Atlanta(Resources resources) {
        mBuilding = resources.getDrawable(R.drawable.atlanta, null);
    }

    @Override
    public void draw(Canvas canvas, Rect bounds) {
        mBuilding.setBounds(bounds.right - mBuilding.getIntrinsicWidth(), bounds.bottom - mBuilding.getIntrinsicHeight(), bounds.right, bounds.bottom);
        mBuilding.draw(canvas);
    }

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);
        if(mAmbient) {
            mBuilding.setColorFilter(SunColors.AMBIENT_FILTER);
        } else {
            onSunUpdated(mSun);
        }
    }

    @Override
    public void onSunUpdated(Sun sun) {
        super.onSunUpdated(sun);
        switch (sun) {
            case SUNSET:
                mBuilding.setColorFilter(SunColors.CHANGE_FILTER);
                break;
            case SUNRISE:
                mBuilding.setColorFilter(SunColors.CHANGE_FILTER);
                break;
            case NIGHT:
                mBuilding.setColorFilter(SunColors.NIGHT_FILTER);
                break;
            case DAY:
                mBuilding.clearColorFilter();
        }
    }

    public static boolean contains(WGS84Point point) {
        return BOUNDING_BOX.contains(point);
    }
}
