package com.thisisnotajoke.android.cityscape;

import android.content.res.Resources;
import android.util.Log;
import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.WGS84Point;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import com.thisisnotajoke.android.cityscape.layer.Atlanta;
import com.thisisnotajoke.android.cityscape.layer.Empire;
import com.thisisnotajoke.android.cityscape.layer.GoldenGate;
import com.thisisnotajoke.android.cityscape.layer.Nashville;
import com.thisisnotajoke.android.cityscape.layer.Rural;
import com.thisisnotajoke.android.cityscape.layer.Sutro;
import com.thisisnotajoke.android.cityscape.model.City;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.UUID;
import org.joda.time.DateTime;

public class World {

  private static final String TAG = "World";

  public static final City[] CITIES = {
      new City("df4e4cde-aaf1-496e-9386-298dbe2c84f2", "Atlanta", Atlanta.class,
          new BoundingBox(34.080341, 33.411764, -84.699899, -83.997117)),
      new City("1c17bfb7-f9d9-4427-86ae-4a56bd26b902", "Nashville", Nashville.class,
          new BoundingBox(36.262914, 35.888864, -86.975785, -86.405870)),
      new City("08d9498f-b97f-4d62-a37f-561bd3bca3cb", "Empire State Building", Empire.class,
          new BoundingBox(40.894010, 40.555767, -74.054324, -73.671863)),
      new City("28a7243e-a6ae-462f-a780-767415bbbc0c", "Sutro Tower", Sutro.class,
          new BoundingBox(37.774364, 37.726447, -122.470638, -122.398884)),
      new City("af609807-30be-41b5-868a-6d265ae06dfb", "Golden Gate", GoldenGate.class,
          new BoundingBox(38.145365, 37.077101, -123.083734, -121.070489))
  };

  public static Sun calculateSun(WGS84Point point, DateTime time) {
    if (point == null) {
      return Sun.NIGHT;
    }
    Log.d(TAG, "Calculating solar schedule");
    GregorianCalendar calendar = time.toGregorianCalendar();
    SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
        new Location(point.getLatitude(), point.getLongitude()), time.getZone().toTimeZone());
    DateTime dusk = new DateTime(calculator.getOfficialSunsetCalendarForDate(calendar));
    DateTime sunset = new DateTime(calculator.getCivilSunsetCalendarForDate(calendar));
    DateTime dawn = new DateTime(calculator.getCivilSunriseCalendarForDate(calendar));
    DateTime sunrise = new DateTime(calculator.getOfficialSunriseCalendarForDate(calendar));
    if (time.isBefore(dawn) || time.isAfter(sunset)) {
      return Sun.NIGHT;
    } else if (time.isAfter(sunrise) && time.isBefore(dusk)) {
      return Sun.DAY;
    } else if (time.isBefore(sunrise)) {
      return Sun.SUNRISE;
    } else {
      return Sun.SUNSET;
    }
  }

  public static FaceLayer getCityFace(Resources resources, UUID id) {
    if (id == null) {
      return new Rural(resources);
    }
    for (City city : CITIES) {
      if (city.getID().equals(id)) {
        return city.getFace(resources);
      }
    }
    return new Rural(resources);
  }

  public static FaceLayer getCurrentCityFace(Resources resources, WGS84Point location) {
    if (location == null) {
      return new Rural(resources);
    }
    for (City city : CITIES) {
      if (city.contains(location)) {
        return city.getFace(resources);
      }
    }
    return new Rural(resources);
  }

  public static City getCity(String id) {
    if (id == null) {
      return null;
    }
    for (City c : CITIES) {
      if (UUID.fromString(id).equals(c.getID())) {
        return c;
      }
    }
    return null;
  }

  public static FaceLayer getRandomCityFace(Resources resources) {
    int i = new Random().nextInt(CITIES.length);
    return CITIES[i].getFace(resources);
  }
}
