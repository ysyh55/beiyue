# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/junyuecao/Work/adt-bundle-mac-x86_64-20140702/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class me.zheteng.cbreader.ui.ReadFragment$MyJsInterface {
   public *;
}

#----------友盟
-keep public class me.zheteng.cbreader.R$*{
public static final int *;
}
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#----------友盟
-keep public class org.jsoup.** {
    public *;
}


-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }
-dontwarn com.squareup.okhttp.**