# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile ecardecard
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.widget.SearchView { *; }
-keepattributes *Annotation*

-dontwarn com.fasterxml.jackson.databind.**
-keep public class com.fasterxml.jackson.databind.**{ *; }

-dontwarn com.fasterxml.jackson.core.**
-keep public class com.fasterxml.jackson.core.**{ *; }

-dontwarn com.fasterxml.jackson.annotations.**
-keep public class com.fasterxml.jackson.annotations.**{ *; }

-dontwarn cn.edots.nest.**
-keep public class cn.edots.nest.**{ *; }

-dontwarn cn.edots.slug.**
-keep public class cn.edots.slug.**{ *; }

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-dontwarn com.thoughtworks.xstream.**
-keep class com.thoughtworks.xstream.**{*;}

-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.**{*;}

-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

-dontwarn okio.**
-keep class okio.**{*;}

-dontwarn rx.**
-keep class rx.**{*;}

-dontwarn android.content..**
-keep class android.content.**{*;}

-dontwarn org.xmlpull.v1.**
-keep class org.xmlpull.v1.**{*;}

-dontwarn com.wilddog.wilddogauth.**
-keep class com.wilddog.wilddogauth.**{*;}

-dontwarn com.wilddog.wilddogcore.**
-keep class com.wilddog.wilddogcore.**{*;}

-dontwarn com.wilddog.client.**
-keep class com.wilddog.client.**{*;}

-dontwarn com.wetoop.ecard.tools.**
-keep class com.wetoop.ecard.tools.**{*;}

-dontwarn com.wetoop.ecard.api.**
-keep class com.wetoop.ecard.api.**{*;}

-dontwarn com.wetoop.ecard.bean.**
-keep class com.wetoop.ecard.bean.**{*;}

-dontwarn com.wetoop.ecard.listener.**
-keep class com.wetoop.ecard.listener.**{*;}

-dontwarn com.wetoop.ecard.tools.**
-keep class com.wetoop.ecard.tools.**{*;}

-keepnames class com.fasterxml.jackson.** {*;}
-keepnames interface com.fasterxml.jackson.** {*;}