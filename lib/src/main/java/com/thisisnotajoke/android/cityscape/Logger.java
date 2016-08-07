package com.thisisnotajoke.android.cityscape;

import android.util.Log;

public class Logger {
  private static final boolean DEBUG = true;

  public static void e(String tag, String msg, Object... args) {
    Log.e(tag, getFormattedMsg(msg, args));
  }

  public static void e(String tag, String msg, Throwable t, Object... args) {
    Log.e(tag, getFormattedMsg(msg, args), t);
  }

  public static void w(String tag, String msg, Object... args) {
    Log.w(tag, getFormattedMsg(msg, args));
  }

  public static void i(String tag, String msg, Object... args) {
    Log.i(tag, getFormattedMsg(msg, args));
  }

  public static void d(String tag, String msg, Object... args) {
    if (DEBUG) {
      Log.d(tag, getFormattedMsg(msg, args));
    }
  }

  public static void v(String tag, String msg, Object... args) {
    if (DEBUG) {
      Log.v(tag, getFormattedMsg(msg, args));
    }
  }

  private static String getFormattedMsg(String msg, Object... args) {
    return (args == null || args.length == 0) ? msg : String.format(msg, args);
  }

  public static boolean isDebug() {
    return DEBUG;
  }
}
