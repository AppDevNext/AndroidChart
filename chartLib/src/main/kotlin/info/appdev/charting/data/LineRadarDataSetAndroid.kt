package info.appdev.charting.data

import android.graphics.drawable.Drawable
import info.appdev.charting.interfaces.datasets.ILineRadarDataSet
import java.util.WeakHashMap

/**
 * Android-only side-channel storage for the `fillDrawable` property that used to live directly
 * on `ILineRadarDataSet`/`LineRadarDataSet`. `android.graphics.drawable.Drawable` has no
 * portable equivalent, so it can no longer be a stored property on the now-common
 * `LineRadarDataSet` (used by both the Android-only `LineDataSet` and the fully portable
 * `RadarDataSet`). Backed by a [WeakHashMap] keyed by dataset identity so it doesn't leak
 * datasets that are no longer referenced elsewhere.
 *
 * Note: unlike the old stored property, setting [ILineRadarDataSet.fillColor] no longer
 * implicitly clears a previously set `fillDrawable`, and `.copy()` no longer propagates
 * `fillDrawable` to the copy (see `LineRadarDataSet`'s class doc in `chartLibCore`).
 */
private val fillDrawables = WeakHashMap<ILineRadarDataSet<*>, Drawable?>()

/**
 * The drawable used for filling the area below the line (line/radar charts), as an
 * alternative to a solid [ILineRadarDataSet.fillColor]. Android-only, since it operates on
 * `android.graphics.drawable.Drawable`.
 */
var ILineRadarDataSet<*>.fillDrawable: Drawable?
    get() = fillDrawables[this]
    set(value) {
        fillDrawables[this] = value
    }
