package com.thisisnotajoke.android.cityscape.layers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.thisisnotajoke.android.cityscape.FaceLayer;
import com.thisisnotajoke.android.cityscape.Sun;
import com.thisisnotajoke.android.cityscape.SunColors;

public class Sky extends FaceLayer {
    Paint mPaint;

    public Sky() {
        mPaint = new Paint();
        updatePaint();
    }

    @Override
    public void draw(Canvas canvas, Rect bounds) {
        canvas.drawRect(0, 0, bounds.width(), bounds.height(), mPaint);
    }

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);
        updatePaint();
    }

    @Override
    public void onSunUpdated(Sun sun) {
        super.onSunUpdated(sun);
        updatePaint();
    }

    private void updatePaint() {
        if(mAmbient) {
            mPaint.setColor(SunColors.NIGHT);
            return;
        }
        if(mSun == Sun.DAY) {
            mPaint.setColor(SunColors.DAY);
        } else if (mSun == Sun.SUNRISE) {
            mPaint.setColor(SunColors.SUNRISE);
        } else if (mSun == Sun.SUNSET) {
            mPaint.setColor(SunColors.SUNSET);
        } else {
            mPaint.setColor(SunColors.NIGHT);
        }
    }
}
