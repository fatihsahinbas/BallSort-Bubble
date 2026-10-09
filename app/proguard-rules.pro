# Project specific ProGuard and R8 rules for release builds

# Keep line numbers and source file names for production crash diagnostics
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Google Play Billing API 7.x
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.**

# Google Mobile Ads (AdMob) & User Messaging Platform (UMP)
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable { public static final *** NULL; }
-keepnames class * implements android.os.Parcelable { public static final ** CREATOR; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.android.ump.**

# Android VibrationManager & System Services backward compatibility
-dontwarn android.os.VibratorManager
-dontwarn android.os.CombinedVibration

# Jetpack DataStore Preferences
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences$Key { *; }

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# App Data Models & JSON Serialization (Leaderboard, Game Models)
-keep class com.example.data.LevelRecord { *; }
-keep class com.example.model.** { *; }
-keepclassmembers enum com.example.model.BallColor { *; }
-keepclassmembers class com.example.model.Tube { *; }
-keepclassmembers class com.example.model.Ball { *; }
-keepclassmembers class com.example.model.MoveHistory { *; }
