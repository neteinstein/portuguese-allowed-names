# R8 rules for the release build (minification, obfuscation and resource shrinking are all
# enabled in app/build.gradle.kts).
#
# Deliberately short: every third-party library this app uses ships its own consumer rules
# inside its AAR/JAR (Room keeps `* extends RoomDatabase`, pdfbox-android keeps the
# reflectively-instantiated SecurityHandlers, OkHttp, DataStore, Compose, Navigation and
# Lifecycle all declare theirs), and R8 applies those automatically. What's left below is only
# what no library can know about: our own code that is addressed by *name* at runtime, plus the
# attributes needed to make obfuscated crash reports readable again.

# --- Crash-report readability ----------------------------------------------------------------
# Without these, obfuscated stack traces lose their file/line information entirely and can't be
# recovered even with the mapping file. `-renamesourcefileattribute` replaces the real source
# file name with a constant, so keeping SourceFile leaks nothing while retracing still works.
# The mapping needed to decode them is written to app/build/outputs/mapping/release/mapping.txt
# (AGP also embeds it inside the AAB automatically, so Play Console deobfuscates uploads).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Enums persisted by name ------------------------------------------------------------------
# SettingsRepositoryImpl writes these two to DataStore as `value.name` and reads them back by
# matching that string against `entries`, so their constant names are part of the on-disk
# settings format, not just an internal detail. Pinning them keeps R8's enum optimizations
# (which are free to unbox an enum into an int once it looks like nothing depends on its
# identity) from silently resetting a user's saved refresh period or search engine on upgrade.
# Gender is deliberately absent: it round-trips through explicit "F"/"M" codes (see
# data/mapper), never through `name`, and SyncOrigin only builds a nav route within a single
# process, so neither is name-sensitive.
-keepclassmembers enum org.neteinstein.pickaname.domain.model.RefreshPeriod {
    <fields>;
}
-keepclassmembers enum org.neteinstein.pickaname.domain.model.SearchEngine {
    <fields>;
}

# --- pdfbox-android ----------------------------------------------------------------------------
# pdfbox-android is a port of desktop PDFBox and still references the desktop-only APIs its
# Android code paths never reach (AWT imaging via javax.imageio, the optional BouncyCastle
# crypto provider used for encrypted PDFs, OSGi service wiring). Those classes don't exist on
# Android, so R8's full mode reports them as missing and fails the build unless they're
# explicitly declared unreachable here.
-dontwarn org.bouncycastle.**
-dontwarn javax.imageio.**
-dontwarn org.osgi.**
# Same story for JP2Decoder: pdfbox-android's JPXFilter calls into the separate, optional
# `com.gemalto.jp2:jp2-android` artifact to decode JPEG 2000 images. This app doesn't depend on
# it (the names list is a text-only PDF), so the reference is already dangling in today's
# unminified build too - declaring it here changes nothing at runtime, it only stops R8 from
# treating a dependency we intentionally don't ship as a build error.
-dontwarn com.gemalto.jp2.**
