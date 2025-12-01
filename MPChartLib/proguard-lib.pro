# Whitelist AndroidChart
# Preserve all public classes and methods

-keep class com.github.mikephil.charting.** { *; }
-keep public class com.github.mikephil.charting.animation.* {
    public protected *;
}
