package info.appdev.charting.data

import android.R.attr.data
import android.R.attr.x
import android.R.attr.y
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.ParcelFormatException
import android.os.Parcelable
import info.appdev.charting.utils.Utils
import java.io.Serializable
import kotlin.math.abs

/**
 * Class representing one entry in the chart. Might contain multiple values.
 * Might only contain a single value depending on the used constructor.
 */
@Deprecated("The replacement is EntryFloat, or use EntryDouble for higher precision. Entry is retained for backward compatibility but will be removed in a future version.")
class Entry : EntryFloat {

    constructor()

    /**
     * An EntryFloat represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     */
    constructor(x: Float, y: Float) : super(x = x, y = y)

    /**
     * An EntryFloat represents one single entry in the chart.
     *
     * @param x    the x value
     * @param y    the y value (the actual value of the entry)
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: Float, y: Float, data: Any?) : super(x = x, y = y, data = data)

    /**
     * An EntryFloat represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     */
    constructor(x: Float, y: Float, icon: Drawable?) : super(x = x, y = y, icon = icon)

    /**
     * An EntryFloat represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     * @param data Spot for additional data this EntryFloat represents.
     */
    constructor(x: Float, y: Float, icon: Drawable?, data: Any?) : super(x = x, y = y, icon = icon, data = data)

}
