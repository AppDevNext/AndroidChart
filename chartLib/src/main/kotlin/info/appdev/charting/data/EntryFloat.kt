package info.appdev.charting.data

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
open class EntryFloat : BaseEntry<Float>, Parcelable, Serializable {

    private var _x: Float = 0f
    open var x: Float
        get() = _x
        set(value) {
            _x = value
        }

    constructor()

    /**
     * A EntryFloat represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     */
    constructor(x: Float, y: Float) : super(y) {
        this._x = x
    }

    /**
     * A EntryFloat represents one single entry in the chart.
     *
     * @param x    the x value
     * @param y    the y value (the actual value of the entry)
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: Float, y: Float, data: Any?) : super(y, data) {
        this._x = x
    }

    /**
     * A EntryFloat represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     */
    constructor(x: Float, y: Float, icon: Drawable?) : super(y, icon) {
        this._x = x
    }

    /**
     * A EntryFloat represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     * @param data Spot for additional data this EntryFloat represents.
     */
    constructor(x: Float, y: Float, icon: Drawable?, data: Any?) : super(y, icon, data) {
        this._x = x
    }

    /**
     * returns an exact copy of the entry
     */
    open fun copy(): EntryFloat {
        val e = EntryFloat(x, y, data)
        return e
    }

    /**
     * Compares value, xIndex and data of the entries. Returns true if entries
     * are equal in those points, false if not. Does not check by hash-code like
     * it's done by the "equals" method.
     */
    fun equalTo(e: EntryFloat?): Boolean {
        if (e == null)
            return false

        if (e.data !== this.data)
            return false

        if (abs((e.x - this.x).toDouble()) > Utils.FLOAT_EPSILON)
            return false

        if (abs((e.y - this.y).toDouble()) > Utils.FLOAT_EPSILON)
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
            "EntryFloat x=$x y=$y"
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeFloat(this.x)
        dest.writeFloat(this.y)
        if (data != null) {
            if (data is Parcelable) {
                dest.writeInt(1)
                dest.writeParcelable(data as Parcelable?, flags)
            } else {
                throw ParcelFormatException("Cannot parcel an EntryFloat with non-parcelable data")
            }
        } else {
            dest.writeInt(0)
        }
    }

    protected constructor(`in`: Parcel) {
        this._x = `in`.readFloat()
        this.yBase = `in`.readFloat()
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
        val CREATOR: Parcelable.Creator<EntryFloat> = object : Parcelable.Creator<EntryFloat> {
            override fun createFromParcel(source: Parcel): EntryFloat {
                return EntryFloat(source)
            }

            override fun newArray(size: Int): Array<EntryFloat?> {
                return arrayOfNulls(size)
            }
        }
    }
}
