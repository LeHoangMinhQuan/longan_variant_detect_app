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
-dontwarn org.tensorflow.lite.gpu.GpuDelegateFactory$Options$GpuBackend
-dontwarn org.tensorflow.lite.gpu.GpuDelegateFactory$Options

#############################################
# 🔒 Firebase / Firestore / Realtime Database
#############################################

# Keep model classes (prevent stripping or renaming)
-keep class com.quan.phnloinhn.src.Model.** { *; }

# Keep all constructors and fields (for Firebase reflection)
-keepclassmembers class com.quan.phnloinhn.src.Model.** {
    public <init>();
    <fields>;
    <methods>;
}

# Keep Firebase core classes and annotations
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.internal.** { *; }

# (Optional) Prevent warnings about Firebase reflection
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**