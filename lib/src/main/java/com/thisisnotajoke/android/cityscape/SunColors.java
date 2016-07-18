package com.thisisnotajoke.android.cityscape;

import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;

public class SunColors {

  public static final ColorFilter CHANGE_FILTER = new ColorMatrixColorFilter(new float[]{  //4700K
      1, 0, 0, 0, 0,
      0, 223.0f / 255.0f, 0, 0, 0,
      0, 0, 194.0f / 255.0f, 0, 0,
      0, 0, 0, 1, 0});
  public static final ColorFilter NIGHT_FILTER = new ColorMatrixColorFilter(new float[]{  //8100K
      225 / 255.0f, 0, 0, 0, 0,
      0, 231.0f / 255.0f, 0, 0, 0,
      0, 0, 1, 0, 0,
      0, 0, 0, 1, 0});
  public static final ColorFilter AMBIENT_FILTER = new ColorMatrixColorFilter(new float[]{
      0.3f, 0.59f, 0.11f, 0, 0,
      0.3f, 0.59f, 0.11f, 0, 0,
      0.3f, 0.59f, 0.11f, 0, 0,
      0, 0, 0, 1, 0,});
  public static int SUNRISE;
  public static int SUNSET;
  public static int NIGHT;
  public static int DAY;

  public static void initialize(Resources resources) {
    SUNSET = resources.getColor(R.color.sky_sunset);
    SUNRISE = resources.getColor(R.color.sky_sunrise);
    NIGHT = resources.getColor(R.color.sky_night);
    DAY = resources.getColor(R.color.sky_day);
  }
}
