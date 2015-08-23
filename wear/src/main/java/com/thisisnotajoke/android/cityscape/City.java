package com.thisisnotajoke.android.cityscape;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.format.Time;

public abstract class City {
    abstract void draw(Canvas canvas, Rect bounds);
    abstract void predraw(Time time);

    public static boolean isDay(Time time) {
        return time.hour > 7 && time.hour < 19;
    }

    public abstract void onAmbientModeChanged(boolean inAmbientMode);
}
