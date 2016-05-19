package com.thisisnotajoke.android.cityscape;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;

public abstract class FaceLayer {
    private static final String TAG = "FaceLayer";
    protected Sun mSun = Sun.DAY;
    protected boolean mAmbient = false;

    public FaceLayer(Resources resources) {

    }

    protected FaceLayer() {
    }

    public abstract void draw(Canvas canvas, Rect bounds);

    public void onSunUpdated(Sun sun) {
        mSun = sun;
    }

    public void onAmbientModeChanged(boolean inAmbientMode) {
        mAmbient = inAmbientMode;
    }
}
