package info.appdev.charting.data

import android.graphics.drawable.Drawable

@Deprecated(
    message = "The replacement is PieEntryFloat, or use PieEntryDouble for higher precision. PieEntry is retained for backward compatibility but will be removed in a future version.",
    replaceWith = ReplaceWith("PieEntryFloat", "info.appdev.charting.data.PieEntryFloat")
)
class PieEntry : PieEntryFloat {

    constructor(value: Float) : super(0f)

    constructor(value: Float, data: Any?) : super(value, data)

    constructor(value: Float, icon: Drawable?) : super(value, icon)

    constructor(value: Float, icon: Drawable?, data: Any?) : super(value, icon, data)

    constructor(value: Float, label: String?) : super(value) {
        this.label = label
    }

    constructor(value: Float, label: String?, data: Any?) : super(value, data) {
        this.label = label
    }

    constructor(value: Float, label: String?, icon: Drawable?) : super(value, icon) {
        this.label = label
    }

    constructor(value: Float, label: String?, icon: Drawable?, data: Any?) : super(value, icon, data) {
        this.label = label
    }

}
