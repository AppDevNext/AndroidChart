# Whitelist AndroidChart
# Preserve all public classes and methods

-keep class info.appdev.charting.** { *; }
-keep public class info.appdev.charting.animation.* {
    public protected *;
}
