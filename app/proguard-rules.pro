# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\renato\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class com.facebook.FacebookSdk { *; }

-keep class com.google.android.gms.ads.AdListener { *; }
-keep class com.google.android.gms.ads.AdRequest { *; }
-keep class com.google.android.gms.ads.AdView { *; }
-keep class android.support.v7.widget.SearchView { *; }
-keep class android.support.v7.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.squareup.picasso.** { *; }

-keep class com.sow.gpstrackerpro.classes.** { *; }
-keep class com.sow.gpstrackerpro.application.** { *; }
-keep class com.sow.gpstrackerpro.events.** { *; }
-keep class com.sow.gpstrackerpro.receivers.** { *; }
-keep class com.sow.gpstrackerpro.services.** { *; }