package info.appdev.charting.data

import info.appdev.charting.utils.ChartIcon

open class PieEntryFloat : EntryFloat {
    var label: String? = null

    constructor(value: Float) : super(0f, value)

    constructor(value: Float, data: Any?) : super(0f, value, data)

    constructor(value: Float, icon: ChartIcon?) : super(0f, value, icon)

    constructor(value: Float, icon: ChartIcon?, data: Any?) : super(0f, value, icon, data)

    constructor(value: Float, label: String?) : super(0f, value) {
        this.label = label
    }

    constructor(value: Float, label: String?, data: Any?) : super(0f, value, data) {
        this.label = label
    }

    constructor(value: Float, label: String?, icon: ChartIcon?) : super(0f, value, icon) {
        this.label = label
    }

    constructor(value: Float, label: String?, icon: ChartIcon?, data: Any?) : super(0f, value, icon, data) {
        this.label = label
    }

    val value: Float
        /**
         * This is the same as getY(). Returns the value of the PieEntry.
         */
        get() = y

    @get:Deprecated("")
    @set:Deprecated("")
    override var x: Float
        get() = super.x
        set(x) {
            super.x = x
        }

    override fun copy(): PieEntryFloat {
        return PieEntryFloat(y, label, data)
    }
}
