package com.thisisnotajoke.android.cityscape.layers;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.thisisnotajoke.android.cityscape.FaceLayer;
import com.thisisnotajoke.android.cityscape.R;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.WGS84Point;

public class Nashville extends FaceLayer {
    private static final BoundingBox BOUNDING_BOX = new BoundingBox(36.262914, 35.888864, -86.975785, -86.405870);
    private final Paint mBodyPaint;
    private final Paint mGlassPaint;
    private final Paint mHolePaint;

    private static int HOLE_COLOR;
    private static int HOLE_COLOR_AMBIENT;

    public Nashville(Resources resources) {
        mBodyPaint = new Paint();
        mBodyPaint.setColor(resources.getColor(R.color.nashville_body));
        mGlassPaint = new Paint();
        mGlassPaint.setColor(resources.getColor(R.color.nashville_glass));
        mHolePaint = new Paint();

        HOLE_COLOR = resources.getColor(R.color.black);
        HOLE_COLOR_AMBIENT = resources.getColor(R.color.nashville_hole_ambient);
        updatePaint();
    }

    @Override
    public void draw(Canvas canvas, Rect bounds) {
        int leftBase = 120;
        int rightBase = leftBase + 120;
        int bottomBase = bounds.bottom;
        int topBase = bottomBase - 70;

        int leftGlass = leftBase + 20;
        int rightGlass = rightBase - 20;
        int topGlass = topBase + 30;
        int bottomGlass = bottomBase;

        int leftSpire1 = leftBase;
        int rightSpire1 = leftGlass;
        int leftSpire2 = rightGlass;
        int rightSpire2 = rightBase;
        int bottomSpire = bottomBase;
        int topSpire = topBase - 30;

        canvas.drawRect(leftSpire1, topSpire, rightSpire1, bottomSpire, mBodyPaint);
        canvas.drawRect(leftSpire2, topSpire, rightSpire2, bottomSpire, mBodyPaint);
        canvas.drawRect(rightSpire1, topSpire + 30, leftSpire2, topGlass, mBodyPaint);
        canvas.drawRect(leftGlass, topGlass, rightGlass, bottomGlass, mGlassPaint);
        canvas.drawArc(leftGlass + 15, topGlass - 20, rightGlass - 15, topGlass + 20, 200, 140, !mAmbient, mHolePaint);
        canvas.drawRect(leftGlass + 18, topGlass - 10, rightGlass - 18, topGlass, mHolePaint);
    }

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);
        updatePaint();
    }

    private void updatePaint() {
        if(mAmbient) {
            mBodyPaint.setStyle(Paint.Style.STROKE);
            mGlassPaint.setStyle(Paint.Style.STROKE);
            mHolePaint.setColor(HOLE_COLOR_AMBIENT);
        } else {
            mBodyPaint.setStyle(Paint.Style.FILL);
            mGlassPaint.setStyle(Paint.Style.FILL);
            mHolePaint.setColor(HOLE_COLOR);
        }
        mHolePaint.setAntiAlias(!mAmbient);
    }
    public static boolean contains(WGS84Point point) {
        return BOUNDING_BOX.contains(point);
    }
}
