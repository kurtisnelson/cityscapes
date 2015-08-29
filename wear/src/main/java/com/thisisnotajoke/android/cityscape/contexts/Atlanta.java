package com.thisisnotajoke.android.cityscape.contexts;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.thisisnotajoke.android.cityscape.LocationFace;
import com.thisisnotajoke.android.cityscape.R;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.WGS84Point;

public class Atlanta extends LocationFace {
    private static final BoundingBox BOUNDING_BOX = new BoundingBox(34.080341, 33.411764, -84.699899, -83.997117);

    private static int TIP_COLOR_NIGHT;
    private static int TIP_COLOR;
    private static int ROOF_COLOR;
    private static int ROOF_COLOR_NIGHT;

    private Paint mRoofPaint;
    private Paint mTipPaint;
    private Paint mBodyPaint;

    public Atlanta(Resources resources) {
        TIP_COLOR_NIGHT = resources.getColor(R.color.tip_night);
        TIP_COLOR = resources.getColor(R.color.tip);
        ROOF_COLOR_NIGHT = resources.getColor(R.color.roof_night);
        ROOF_COLOR = resources.getColor(R.color.roof);

        mSky = Sky.DAY;
        mRoofPaint = new Paint();
        mTipPaint = new Paint();
        mBodyPaint = new Paint();
        mBodyPaint.setColor(resources.getColor(R.color.body));
        onAmbientModeChanged(false);
        updatePaintColor();
    }

    @Override
    public void draw(Canvas canvas, Rect bounds) {
        int leftBase = 170;
        int rightBase = leftBase + 75;
        int bottomBase = bounds.bottom;
        int topBase = bottomBase - 110;

        int leftMid = leftBase + 10;
        int rightMid = rightBase - 10;
        int bottomMid = topBase;
        int topMid = bottomMid - 40;

        int roofTipX = (leftMid + rightMid) / 2;
        int roofTipY = topMid - 50;

        int leftSpire = roofTipX - 5;
        int rightSpire = roofTipX + 5;
        int bottomSpire = roofTipY + 10;
        int topSpire = bottomSpire - 10;

        int peakY = topSpire - 5;
        int peakX = (leftSpire + rightSpire) / 2;

        Path roofPath = new Path();
        roofPath.setFillType(Path.FillType.EVEN_ODD);
        roofPath.moveTo(leftMid, topMid);
        roofPath.lineTo(roofTipX, roofTipY);
        roofPath.lineTo(rightMid, topMid);
        roofPath.lineTo(leftMid, topMid);
        roofPath.close();

        Path peakPath = new Path();
        peakPath.setFillType(Path.FillType.EVEN_ODD);
        peakPath.moveTo(leftSpire, topSpire);
        peakPath.lineTo(rightSpire, topSpire);
        peakPath.lineTo(peakX, peakY);
        peakPath.lineTo(leftSpire, topSpire);
        peakPath.close();

        canvas.drawRect(leftBase, topBase, rightBase, bottomBase, mBodyPaint);
        canvas.drawRect(leftMid, topMid, rightMid, bottomMid, mBodyPaint);
        canvas.drawPath(roofPath, mRoofPaint);
        canvas.drawRect(leftSpire, topSpire, rightSpire, bottomSpire, mTipPaint);
        canvas.drawPath(peakPath, mTipPaint);
    }

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
        if(inAmbientMode) {
            mRoofPaint.setStyle(Paint.Style.STROKE);
            mTipPaint.setStyle(Paint.Style.STROKE);
            mBodyPaint.setStyle(Paint.Style.STROKE);
        } else {
            mRoofPaint.setStyle(Paint.Style.FILL);
            mTipPaint.setStyle(Paint.Style.FILL);
            mBodyPaint.setStyle(Paint.Style.FILL);
        }
        mRoofPaint.setAntiAlias(!inAmbientMode);
    }

    @Override
    public void onSkyUpdated(Sky sky) {
        updatePaintColor();
    }

    private void updatePaintColor() {
        mRoofPaint.setColor(mSky == Sky.DAY ? ROOF_COLOR : ROOF_COLOR_NIGHT);
        mTipPaint.setColor(mSky == Sky.DAY ? TIP_COLOR : TIP_COLOR_NIGHT);
    }

    public static boolean contains(WGS84Point point) {
        return BOUNDING_BOX.contains(point);
    }
}
