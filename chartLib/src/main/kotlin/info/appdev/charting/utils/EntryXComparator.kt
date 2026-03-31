package info.appdev.charting.utils

import info.appdev.charting.data.EntryFloat

/**
 * Comparator for comparing Entry-objects by their x-value.
 */
class EntryXComparator : Comparator<EntryFloat> {
    override fun compare(entryFloat1: EntryFloat, entryFloat2: EntryFloat): Int {
        val diff = entryFloat1.x - entryFloat2.x

        return diff.compareTo(0f)
    }
}
