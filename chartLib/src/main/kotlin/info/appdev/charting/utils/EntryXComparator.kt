package info.appdev.charting.utils

import info.appdev.charting.data.Entry
import kotlin.Comparator
import kotlin.Int

/**
 * Comparator for comparing Entry-objects by their x-value.
 */
class EntryXComparator : Comparator<Entry> {
    override fun compare(entry1: Entry, entry2: Entry): Int {
        val diff = entry1.x - entry2.x

        return diff.compareTo(0f)
    }
}
