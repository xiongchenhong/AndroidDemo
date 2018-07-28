# 这是在AS 3.1创建的demo工程默认的proguard配置，通过gradle中的 proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro' 引入
# 这里讲两份文件合在一起，便于学习

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


###############################################################################################################
# 从android gradle 插件的2.2版本开始，
# proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
# 中使用的 proguard-android.txt 不再是sdk中的文件，而是由插件在编译时动态生成，可在与app平级的build/intermediates/proguard-files中找到
# 为方便学习，将整份文件直接copy过来

# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
#
# Starting with version 2.2 of the Android plugin for Gradle, this file is distributed together with
# the plugin and unpacked at build-time. The files in $ANDROID_HOME are no longer maintained and
# will be ignored by new version of the Android plugin for Gradle.

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize steps (and performs some
# of these optimizations on its own).
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

# 默认会关掉优化，如果想使用优化，可以使用 proguard-android-optimize.txt，
# 如果使用 proguard-defaults.txt，则可以在 gradle 配置里动态的开启或关闭优化
-dontoptimize

-dontusemixedcaseclassnames # 混淆时不使用大小写混写的类名
-dontskipnonpubliclibraryclasses # 不跳过非public类
-verbose # 工作时打印出详细信息

# Preserve some attributes that may be required for reflection.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod # keep指定的属性，用于反射

# keep google相关的库，dontnote不提示相关的信息（因为不是所有的工程都用到了这些库）
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService
-dontnote com.google.vending.licensing.ILicensingService
-dontnote com.google.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
# 使用了native方法必须要加上该配置，android的默认配置仅能保证方法名不混淆，如果需要keep参数和返回值 使用
#-keepclasseswithmembernames,includedescriptorclasses class * {
#    native <methods>;
#}
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep setters in Views so that animations can still work.
# keep 所有View子类的get和set方法
# ‘***’ 通配符可以匹配所有的类型（基本类型，数组等）

# ? 通配符匹配一个字符
# * 通配符匹配若干字符（不包含分隔符）
# ** 通配符匹配若干字符（包含分隔符）
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick.
# keep 所有activity子类中参数为View类的方法
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
# 如果使用了枚举类，需要指定该配置
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable类中的CREATOR字段
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# R文件中记录资源id的字段
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Preserve annotated Javascript interface methods.
# keep 使用@JavascriptInterface注解修饰的方法，4.2版本之前没有使用注解的需要手动keep
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# The support libraries contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.
# 不提示 support包报的引用错误
-dontnote android.support.**
-dontwarn android.support.**

# This class is deprecated, but remains for backward compatibility.
-dontwarn android.util.FloatMath

# Understand the @Keep support annotation.
# keep Keep注解类
-keep class android.support.annotation.Keep

# keep使用 @keep修饰的类和所有类成员
-keep @android.support.annotation.Keep class * {*;}

# 如果有@keep修饰的方法，则keep类和该方法
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}

# 如果有@keep修饰的成员变量，则keep类和该成员变量
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}

# 如果有@keep修饰的构造器，则keep类和该构造器
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}


