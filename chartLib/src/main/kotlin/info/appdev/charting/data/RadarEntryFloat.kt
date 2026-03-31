package info.appdev.charting.data

import android.annotation.SuppressLint

@SuppressLint("ParcelCreator")
class RadarEntryFloat : EntryFloat {
    constructor(value: Float) : super(0f, value)

    constructor(value: Float, data: Any?) : super(0f, value, data)

    /**
     * This is the same as getY(). Returns the value of the RadarEntry.
     */
    val value: Float
        get() = y

    override fun copy(): RadarEntryFloat {
        return RadarEntryFloat(y, data)
    }

    @get:Deprecated("")
    @set:Deprecated("")
    override var x: Float
        get() = super.x
        set(x) {
            super.x = x
        }
}
