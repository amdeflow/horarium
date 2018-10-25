# Retrofit rules
-keepattributes Signature
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform

-dontwarn okio.**

# Anko rule
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class nl.viasalix.horarium.** { *; }