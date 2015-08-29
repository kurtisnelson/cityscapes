package com.thisisnotajoke.android.cityscape.layers;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.thisisnotajoke.android.cityscape.FaceLayer;
import com.thisisnotajoke.android.cityscape.R;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.WGS84Point;

public class Atlanta extends FaceLayer {
    private static final BoundingBox BOUNDING_BOX = new BoundingBox(34.080341, 33.411764, -84.699899, -83.997117);

    private static int TIP_COLOR_NIGHT;
    private static int TIP_COLOR;
    private static int ROOF_COLOR;
    private static int ROOF_COLOR_NIGHT;

    private final Paint mRoofPaint;
    private final Paint mTipPaint;
    private final Paint mBodyPaint;

    public Atlanta(Resources resources) {
        TIP_COLOR_NIGHT = resources.getColor(R.color.atlanta_tip_night);
        TIP_COLOR = resources.getColor(R.color.atlanta_tip);
        ROOF_COLOR_NIGHT = resources.getColor(R.color.atlanta_roof_night);
        ROOF_COLOR = resources.getColor(R.color.atlanta_roof);

        mRoofPaint = new Paint();
        mTipPaint = new Paint();
        mBodyPaint = new Paint();
        mBodyPaint.setColor(resources.getColor(R.color.atlanta_body));
        updatePaint();
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
        super.onAmbientModeChanged(inAmbientMode);
        updatePaint();
    }

    @Override
    public void onSunUpdated(Sun sun) {
        super.onSunUpdated(sun);
        updatePaint();
    }

    private void updatePaint() {
        mRoofPaint.setColor(mSun == Sun.DAY ? ROOF_COLOR : ROOF_COLOR_NIGHT);
        mTipPaint.setColor(mSun == Sun.DAY ? TIP_COLOR : TIP_COLOR_NIGHT);
        if(mAmbient) {
            mRoofPaint.setStyle(Paint.Style.STROKE);
            mTipPaint.setStyle(Paint.Style.STROKE);
            mBodyPaint.setStyle(Paint.Style.STROKE);
        } else {
            mRoofPaint.setStyle(Paint.Style.FILL);
            mTipPaint.setStyle(Paint.Style.FILL);
            mBodyPaint.setStyle(Paint.Style.FILL);
        }
        mRoofPaint.setAntiAlias(!mAmbient);
    }

    public static boolean contains(WGS84Point point) {
        return BOUNDING_BOX.contains(point);
    }
}
