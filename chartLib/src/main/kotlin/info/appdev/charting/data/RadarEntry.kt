package info.appdev.charting.data

@Deprecated(
    message = "The replacement is RadarEntryFloat, or use RadarEntryDouble for higher precision. RadarEntry is retained for backward compatibility but will be removed in a future version.",
    replaceWith = ReplaceWith("RadarEntryFloat", "info.appdev.charting.data.RadarEntryFloat")
)
class RadarEntry : RadarEntryFloat {
    constructor(value: Float) : super(value)

    constructor(value: Float, data: Any?) : super(value, data)

}
