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

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Disable R8 optimization that rewrites lambdas using LambdaMetafactory —
# LambdaMetafactory is a hidden Android API blocked on API 31+ (our targetSdk is 36).
# Without this, R8 inlines lambdas in third-party libs (e.g. commons-compress) into
# LambdaMetafactory calls which crash at runtime with "hiddenapi: ... denied".
-optimizations !method/inlining/unique,!method/inlining/short

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ================================================================================================
# Apache POI (Excel library)
# ================================================================================================
# Suppress warnings for desktop/JEE APIs that don't exist on Android
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn javax.xml.namespace.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.**
-dontwarn com.microsoft.schemas.**
-dontwarn schemasMicrosoftComOfficeExcel.**
-dontwarn schemasMicrosoftComOfficeWord.**
-dontwarn schemasMicrosoftComOfficePowerpoint.**
-dontwarn schemaorg_apache_xmlbeans.**

# Keep all Apache POI classes — POI uses reflection internally
-keep class org.apache.poi.** { *; }
-keepclassmembers class org.apache.poi.** { *; }

# Keep HSSF (XLS) and XSSF (XLSX) workbook implementations explicitly.
# WorkbookFactory uses ServiceLoader — we avoid it in code, but keep these in
# case any indirect reflection path still references them.
-keep class org.apache.poi.hssf.usermodel.HSSFWorkbook { *; }
-keep class org.apache.poi.hssf.usermodel.HSSFWorkbookFactory { *; }
-keep class org.apache.poi.xssf.usermodel.XSSFWorkbook { *; }
-keep class org.apache.poi.xssf.usermodel.XSSFWorkbookFactory { *; }
-keep class * implements org.apache.poi.ss.usermodel.IWorkbookFactory { *; }

# XMLBeans (used by POI for XLSX/OOXML parsing) — heavy reflection usage
-keep class org.apache.xmlbeans.** { *; }
-keepclassmembers class org.apache.xmlbeans.** { *; }

# OpenXMLFormats schemas (bundled inside poi-ooxml-schemas / poi-ooxml)
-keep class org.openxmlformats.schemas.** { *; }
-keepclassmembers class org.openxmlformats.schemas.** { *; }

# Microsoft compound document schemas
-keep class com.microsoft.schemas.** { *; }
-keepclassmembers class com.microsoft.schemas.** { *; }

# SchemaOrg XMLBeans generated classes
-keep class schemaorg_apache_xmlbeans.** { *; }
-keepclassmembers class schemaorg_apache_xmlbeans.** { *; }

# ================================================================================================
# Log4j API (newer 2.21.1 — replaces POI's old 2.17.2 to fix Android Class.newInstance issue)
# ================================================================================================
# Missing annotation classes referenced by log4j-api 2.21+ that aren't on Android classpath
-dontwarn aQute.bnd.annotation.spi.ServiceConsumer
-dontwarn aQute.bnd.annotation.spi.ServiceProvider
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
-dontwarn org.osgi.annotation.bundle.**
-dontwarn org.osgi.annotation.versioning.**
-dontwarn org.osgi.framework.**

# Keep log4j-api classes — POI uses them via reflection / ServiceLoader
-keep class org.apache.logging.log4j.** { *; }
-keepclassmembers class org.apache.logging.log4j.** { *; }
# Critical: keep the factory classes that AbstractLogger instantiates via reflection
-keep class org.apache.logging.log4j.message.DefaultFlowMessageFactory { *; }
-keep class org.apache.logging.log4j.message.ParameterizedMessageFactory { *; }
-keep class org.apache.logging.log4j.message.ReusableMessageFactory { *; }

# ================================================================================================
# Apache Commons Compress (used by poi-ooxml for XLSX/ZIP writing)
# ================================================================================================
-dontwarn org.apache.commons.compress.**

# Keep ALL commons-compress classes and constructors.
# ExtraFieldUtils.<clinit> calls register(AsiExtraField.class) and verifies the class
# is concrete via reflection. R8 strips no-arg constructors it deems "unused", causing
# "AsiExtraField is not a concrete class" at runtime.
-keep class org.apache.commons.compress.** { *; }
-keepclassmembers class org.apache.commons.compress.** {
    public <init>(...);
    public <init>();
}

# Prevent R8 from inlining lambdas inside commons-compress into LambdaMetafactory
# calls — LambdaMetafactory is a hidden Android API blocked on API 31+ (targetSdk 36).
-keepclassmembers,allowoptimization class org.apache.commons.compress.** {
    *;
}

# ================================================================================================
# Kotlin Serialization
# ================================================================================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.agcoding.cartrackingapp.**$$serializer { *; }
-keepclassmembers class com.agcoding.cartrackingapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.agcoding.cartrackingapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ================================================================================================
# Room Database
# ================================================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room generated classes
-keep class com.agcoding.cartrackingapp.data.local.** { *; }

# ================================================================================================
# Hilt / Dagger
# ================================================================================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Keep Hilt generated classes
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }
-keep class dagger.hilt.android.internal.lifecycle.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# ================================================================================================
# Kotlin Coroutines
# ================================================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ================================================================================================
# Compose
# ================================================================================================
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep CompositionLocal classes
-keepclassmembers class androidx.compose.** {
    public static ** Companion;
}

# ================================================================================================
# Google Play Services Location
# ================================================================================================
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.**

# ================================================================================================
# WorkManager
# ================================================================================================
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class androidx.work.** { *; }

# ================================================================================================
# DataStore
# ================================================================================================
-keep class androidx.datastore.*.** { *; }

# ================================================================================================
# General Android
# ================================================================================================
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ================================================================================================
# Application-specific rules
# ================================================================================================
# Keep all data models
-keep class com.agcoding.cartrackingapp.data.model.** { *; }
-keep class com.agcoding.cartrackingapp.domain.model.** { *; }

# Keep navigation arguments
-keep class com.agcoding.cartrackingapp.presentation.navigation.** { *; }
