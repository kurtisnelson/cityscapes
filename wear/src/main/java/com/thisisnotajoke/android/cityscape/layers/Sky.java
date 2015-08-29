package com.thisisnotajoke.android.cityscape.layers;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.thisisnotajoke.android.cityscape.FaceLayer;
import com.thisisnotajoke.android.cityscape.R;

public class Sky extends FaceLayer {
    private int DAY_BG_COLOR;
    private int NIGHT_BG_COLOR;

    Paint mPaint;

    public Sky(Resources resources) {
        DAY_BG_COLOR = resources.getColor(R.color.day_background);
        NIGHT_BG_COLOR = resources.getColor(R.color.night_background);

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
        if(mSun == FaceLayer.Sun.DAY && !mAmbient) {
            mPaint.setColor(DAY_BG_COLOR);
        }else {
            mPaint.setColor(NIGHT_BG_COLOR);
        }
    }
}
