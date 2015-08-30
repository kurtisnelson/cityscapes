package com.thisisnotajoke.android.cityscape;

import android.content.res.Resources;
import android.util.Log;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import com.thisisnotajoke.android.cityscape.layers.Atlanta;
import com.thisisnotajoke.android.cityscape.layers.Nashville;
import com.thisisnotajoke.android.cityscape.layers.Rural;

import org.joda.time.DateTime;

import java.util.GregorianCalendar;

import ch.hsr.geohash.WGS84Point;

public class World {
    private static final String TAG = "World";

    public static FaceLayer getCurrentCityFace(Resources resources, WGS84Point location) {
        if(location == null) {
            return new Rural();
        }
        if(Atlanta.contains(location)) {
            Log.d(TAG, "Detected Atlanta");
            return new Atlanta(resources);
        } else if(Nashville.contains(location)) {
            Log.d(TAG, "Detected Nashville");
            return new Nashville(resources);
        }
        Log.w(TAG, "We don't know that part of the world yet, defaulting to rural");
        return new Rural();
    }

    public static Sun calculateSun(WGS84Point point, DateTime time) {
        if(point == null)
            return Sun.NIGHT;
        Log.d(TAG, "Calculating solar schedule");
        GregorianCalendar calendar = time.toGregorianCalendar();
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(new Location(point.getLatitude(), point.getLongitude()), time.getZone().toTimeZone());
        DateTime dusk = new DateTime(calculator.getOfficialSunsetCalendarForDate(calendar));
        DateTime sunset = new DateTime(calculator.getCivilSunsetCalendarForDate(calendar));
        DateTime dawn = new DateTime(calculator.getCivilSunsetCalendarForDate(calendar));
        DateTime sunrise = new DateTime(calculator.getOfficialSunriseCalendarForDate(calendar));
        if(time.isBefore(dawn) || time.isAfter(sunset)) {
            return Sun.NIGHT;
        } else if(time.isAfter(sunrise) && time.isBefore(dusk)) {
            return Sun.DAY;
        } else if(time.isBefore(sunrise)) {
            return Sun.SUNRISE;
        } else {
            return Sun.SUNSET;
        }
    }
}
