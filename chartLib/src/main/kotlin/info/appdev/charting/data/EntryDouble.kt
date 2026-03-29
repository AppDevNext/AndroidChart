package info.appdev.charting.data

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.ParcelFormatException
import android.os.Parcelable
import info.appdev.charting.utils.Utils
import java.io.Serializable
import kotlin.math.abs

open class EntryDouble : BaseEntry<Double>, Parcelable, Serializable {

    constructor()

    /**
     * An Entry represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     */
    constructor(x: Double, y: Double) : super(x = x, y = y)

    /**
     * An Entry represents one single entry in the chart.
     *
     * @param x    the x value
     * @param y    the y value (the actual value of the entry)
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: Double, y: Double, data: Any?) : super(x = x, y = y, data = data)

    /**
     * An Entry represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     */
    constructor(x: Double, y: Double, icon: Drawable?) : super(x = x, y = y, icon = icon)

    /**
     * An Entry represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: Double, y: Double, icon: Drawable?, data: Any?) : super(x = x, y = y, icon = icon, data = data)

    /**
     * returns an exact copy of the entry
     */
    open fun copy(): EntryDouble {
        val e = EntryDouble(
            x = x,
            y = y,
            data = data
        )
        return e
    }

    /**
     * Compares value, xIndex and data of the entries. Returns true if entries
     * are equal in those points, false if not. Does not check by hash-code like
     * it's done by the "equals" method.
     */
    fun equalTo(entryDouble: EntryDouble?): Boolean {
        if (entryDouble == null)
            return false

        if (entryDouble.data !== this.data)
            return false

        if (abs((entryDouble.x - this.x)) > Utils.DOUBLE_EPSILON)
            return false

        if (abs((entryDouble.y - this.y)) > Utils.DOUBLE_EPSILON)
            return false

        return true
    }

    /**
     * returns a string representation of the entry containing x-index and value
     */
    override fun toString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            "${this.javaClass.typeName.substringAfterLast(".")} x=$x y=$y"
        } else {
            "EntryDouble x=$x y=$y"
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(this.x)
        dest.writeDouble(this.y)
        if (data != null) {
            if (data is Parcelable) {
                dest.writeInt(1)
                dest.writeParcelable(data as Parcelable?, flags)
            } else {
                throw ParcelFormatException("Cannot parcel an EntryDouble with non-parcelable data")
            }
        } else {
            dest.writeInt(0)
        }
    }

    protected constructor(`in`: Parcel) {
        this.x = `in`.readDouble()
        this.yBase = `in`.readDouble()
        if (`in`.readInt() == 1) {
            this.data = if (Build.VERSION.SDK_INT >= 33) {
                `in`.readParcelable(Any::class.java.classLoader, Any::class.java)
            } else {
                @Suppress("DEPRECATION")
                `in`.readParcelable(Any::class.java.classLoader)
            }
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<EntryDouble> = object : Parcelable.Creator<EntryDouble> {
            override fun createFromParcel(source: Parcel): EntryDouble {
                return EntryDouble(source)
            }

            override fun newArray(size: Int): Array<EntryDouble?> {
                return arrayOfNulls(size)
            }
        }
    }
}
