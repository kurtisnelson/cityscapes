package com.thisisnotajoke.android.cityscape.layers;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.thisisnotajoke.android.cityscape.FaceLayer;
import com.thisisnotajoke.android.cityscape.R;
import com.thisisnotajoke.android.cityscape.Sun;

public class Sky extends FaceLayer {
    private int DAY_BG_COLOR;
    private int NIGHT_BG_COLOR;
    private int SUNRISE_BG_COLOR;
    private int SUNSET_BG_COLOR;

    Paint mPaint;

    public Sky(Resources resources) {
        DAY_BG_COLOR = resources.getColor(R.color.sky_day);
        NIGHT_BG_COLOR = resources.getColor(R.color.sky_night);
        SUNRISE_BG_COLOR = resources.getColor(R.color.sky_sunrise);
        SUNSET_BG_COLOR = resources.getColor(R.color.sky_sunset);

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
            mPaint.setColor(NIGHT_BG_COLOR);
            return;
        }
        if(mSun == Sun.DAY) {
            mPaint.setColor(DAY_BG_COLOR);
        } else if (mSun == Sun.SUNRISE) {
            mPaint.setColor(SUNRISE_BG_COLOR);
        } else if (mSun == Sun.SUNSET) {
            mPaint.setColor(SUNSET_BG_COLOR);
        } else {
            mPaint.setColor(NIGHT_BG_COLOR);
        }
    }
}
