-keep public class * extends com.thisisnotajoke.android.cityscape.lib.FaceLayer
-keepclassmembers public class * extends com.thisisnotajoke.android.cityscape.lib.FaceLayer { public <init>(android.content.res.Resources); }
# joda
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }