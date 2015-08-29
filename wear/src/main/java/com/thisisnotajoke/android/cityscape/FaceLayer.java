package com.thisisnotajoke.android.cityscape;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.joda.time.DateTime;

import java.util.GregorianCalendar;

import ch.hsr.geohash.WGS84Point;

public abstract class FaceLayer {
    private static final String TAG = "FaceLayer";
    protected Sun mSun = Sun.DAY;
    protected boolean mAmbient = false;

    protected enum Sun {
        DAY,
        NIGHT,
        SUNSET,
        SUNRISE
    }

    public abstract void draw(Canvas canvas, Rect bounds);

    public void onSunUpdated(Sun sun) {
        mSun = sun;
    }

    public void onAmbientModeChanged(boolean inAmbientMode) {
        mAmbient = inAmbientMode;
    }

    public static Sun calculateSun(WGS84Point point, DateTime time) {
        if(point == null)
            return Sun.NIGHT;
        Log.d(TAG, "Calculating solar schedule on " + time.toString());
        GregorianCalendar calendar = time.toGregorianCalendar();
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(new Location(point.getLatitude(), point.getLongitude()), time.getZone().getID());
        DateTime sunsetStart = new DateTime(calculator.getCivilSunsetCalendarForDate(calendar));
        DateTime sunsetEnd = new DateTime(calculator.getAstronomicalSunsetCalendarForDate(calendar));
        DateTime sunriseStart = new DateTime(calculator.getCivilSunriseCalendarForDate(calendar));
        DateTime sunriseEnd = new DateTime(calculator.getAstronomicalSunriseCalendarForDate(calendar));
        if(time.isBefore(sunriseStart) || time.isAfter(sunsetEnd)) {
            return Sun.NIGHT;
        } else if(time.isAfter(sunriseEnd) && time.isBefore(sunsetStart)) {
            return Sun.DAY;
        } else if(time.isBefore(sunriseEnd)) {
            return Sun.SUNRISE;
        } else {
            return Sun.SUNSET;
        }
    }

}
