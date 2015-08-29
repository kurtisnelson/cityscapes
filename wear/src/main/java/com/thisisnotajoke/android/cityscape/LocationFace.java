package com.thisisnotajoke.android.cityscape;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.joda.time.DateTime;

import java.util.GregorianCalendar;

import ch.hsr.geohash.WGS84Point;

public abstract class LocationFace {
    private static final String TAG = "LocationFace";
    protected Sky mSky;

    protected enum Sky {
        DAY,
        NIGHT,
        SUNSET,
        SUNRISE
    }

    public abstract void draw(Canvas canvas, Rect bounds);

    public void onSkyUpdated(Sky sky) {
        mSky = sky;
    }

    public static Sky getSky(WGS84Point point, DateTime time) {
        Log.d(TAG, "Calculating solar schedule on " + time.toString());
        GregorianCalendar calendar = time.toGregorianCalendar();
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(new Location(point.getLatitude(), point.getLongitude()), time.getZone().getID());
        DateTime sunsetStart = new DateTime(calculator.getCivilSunsetCalendarForDate(calendar));
        DateTime sunsetEnd = new DateTime(calculator.getAstronomicalSunsetCalendarForDate(calendar));
        DateTime sunriseStart = new DateTime(calculator.getCivilSunriseCalendarForDate(calendar));
        DateTime sunriseEnd = new DateTime(calculator.getAstronomicalSunriseCalendarForDate(calendar));
        if(time.isBefore(sunriseStart) || time.isAfter(sunsetEnd)) {
            return Sky.NIGHT;
        } else if(time.isAfter(sunriseEnd) && time.isBefore(sunsetStart)) {
            return Sky.DAY;
        } else if(time.isBefore(sunriseEnd)) {
            return Sky.SUNRISE;
        } else {
            return Sky.SUNSET;
        }
    }

    public abstract void onAmbientModeChanged(boolean inAmbientMode);
}
