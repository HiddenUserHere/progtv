# Keep kotlinx.serialization generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class dev.jvfl.progtv.** {
    *** Companion;
}
-keepclasseswithmembers class dev.jvfl.progtv.** {
    kotlinx.serialization.KSerializer serializer(...);
}
