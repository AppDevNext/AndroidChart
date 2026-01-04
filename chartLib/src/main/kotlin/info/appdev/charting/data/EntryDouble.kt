package info.appdev.charting.data

import android.graphics.drawable.Drawable
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
open class EntryDouble : BaseEntry<Double>, Parcelable, Serializable {

    constructor() : super()

    constructor(y: Double) : super(y)

    constructor(y: Double, data: Any?) : super(y, data)

    constructor(y: Double, icon: Drawable?) : super(y, icon)

    constructor(y: Double, icon: Drawable?, data: Any?) : super(y, icon, data)

    constructor(x: Double, y: Double) : super(x, y)

    constructor(x: Double, y: Double, data: Any?) : super(x, y, data)

    constructor(x: Double, y: Double, icon: Drawable?) : super(x, y, icon)

    constructor(x: Double, y: Double, icon: Drawable?, data: Any?) : super(x, y, icon, data)

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
        return "Entry x=$x y=$y"
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
                throw ParcelFormatException("Cannot parcel an Entry with non-parcelable data")
            }
        } else {
            dest.writeInt(0)
        }
    }

    protected constructor(`in`: Parcel) {
        this.xBase = `in`.readDouble()
        this.yBase = `in`.readDouble()
        if (`in`.readInt() == 1) {
            this.data = `in`.readParcelable(Any::class.java.classLoader)
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
