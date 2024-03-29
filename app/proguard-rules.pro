
# Keep line numbers for stack trace
-keepattributes SourceFile,LineNumberTable

-keep class * implements java.io.Serializable { *; }

-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Google Play Services

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keep class com.google.android.gms.drive.** {*;}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Apache HTTP
-keep class org.apache.http.** { *; }
-keep class org.apache.commons.codec.** { *; }
-keep class org.apache.commons.logging.** { *; }
-keep class android.net.compatibility.** { *; }
-keep class android.net.http.** { *; }
-dontwarn org.apache.http.**

#Proguard
-keep class android.support.v7.preference.** { *; }
-keep interface android.support.v7.preference.** { *; }

#Acra
-keep class * implements org.acra.sender.ReportSenderFactory
-keep class * implements org.acra.sender.ReportSender

#Picasso
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.internal.platform.*

# This is generated automatically by the Android Gradle plugin
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.ElementKind
-dontwarn javax.lang.model.element.Modifier
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.conscrypt.Conscrypt