# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Keep class names for serialization/deserialization
-keep class com.shoutboxapp.shoutbox.** { *; }

# Keep model classes used by JSON libraries
-keep class com.shoutboxapp.shoutbox.models.** { *; }

# Keep Android classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Prevent obfuscation of Retrofit/Gson classes
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }

# Keep annotated methods for dependency injection (e.g., Hilt or Dagger)
-keep class * {
    @javax.inject.* <methods>;
}
-keepattributes SourceFile,LineNumberTable
-keep class * {
    public protected *;
}

# Keep Instrumentation classes if they are required
-keep class android.test.** { *; }
-keep class junit.framework.** { *; }

# Prevent stripping of test-related classes
-dontwarn android.test.**
-dontwarn junit.framework.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder