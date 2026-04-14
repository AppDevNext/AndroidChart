package info.appdev.charting.data

@Deprecated("The replacement is RadarEntryFloat, or use RadarEntryDouble for higher precision. RadarEntry is retained for backward compatibility but will be removed in a future version.")
class RadarEntry : RadarEntryFloat {
    constructor(value: Float) : super(value)

    constructor(value: Float, data: Any?) : super(value, data)

}
