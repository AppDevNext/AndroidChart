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

    private var _x: Double = 0.0
    open var x: Double
        get() = _x
        set(value) {
            _x = value
        }

    constructor()

    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     */
    constructor(x: Double, y: Double) : super(y) {
        this._x = x
    }

    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x    the x value
     * @param y    the y value (the actual value of the entry)
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: Double, y: Double, data: Any?) : super(y, data) {
        this._x = x
    }

    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     */
    constructor(x: Double, y: Double, icon: Drawable?) : super(y, icon) {
        this._x = x
    }

    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: Double, y: Double, icon: Drawable?, data: Any?) : super(y, icon, data) {
        this._x = x
    }

    /**
     * returns an exact copy of the entry
     */
    open fun copy(): EntryDouble {
        val e = EntryDouble(x, y, data)
        return e
    }

    /**
     * Compares value, xIndex and data of the entries. Returns true if entries
     * are equal in those points, false if not. Does not check by hash-code like
     * it's done by the "equals" method.
     */
    fun equalTo(e: EntryDouble?): Boolean {
        if (e == null)
            return false

        if (e.data !== this.data)
            return false

        if (abs((e.x - this.x)) > Utils.DOUBLE_EPSILON)
            return false

        if (abs((e.y - this.y)) > Utils.DOUBLE_EPSILON)
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
        this._x = `in`.readDouble()
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
