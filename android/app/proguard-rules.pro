# Release minification is disabled (minifyEnabled false). If you enable it, keep:
# Moshi reads the DTOs by reflection (ClassJsonAdapter): keep their fields and names.
-keep class id.laukpauk.data.model.** { *; }
-keepclassmembers class * { @com.squareup.moshi.Json <fields>; }
-keep class com.xendit.** { *; }
-keep class com.cardinalcommerce.** { *; }
