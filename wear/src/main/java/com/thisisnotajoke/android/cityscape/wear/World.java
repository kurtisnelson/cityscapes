package com.thisisnotajoke.android.cityscape.wear;

import android.content.res.Resources;
import android.util.Log;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import com.thisisnotajoke.android.cityscape.wear.layer.Atlanta;
import com.thisisnotajoke.android.cityscape.wear.layer.Nashville;
import com.thisisnotajoke.android.cityscape.wear.layer.Rural;

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
        DateTime dawn = new DateTime(calculator.getCivilSunriseCalendarForDate(calendar));
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

    public static FaceLayer getCityFace(Resources resources, String city) {
        if(city == null) {
            return new Rural();
        }else if(city.equalsIgnoreCase("atlanta")) {
            return new Atlanta(resources);
        }else if(city.equalsIgnoreCase("nashville")) {
            return new Nashville(resources);
        } else {
            throw new RuntimeException("Invalid city: " + city);
        }
    }
}
