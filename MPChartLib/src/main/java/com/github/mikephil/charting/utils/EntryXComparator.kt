package com.github.mikephil.charting.utils

import com.github.mikephil.charting.data.Entry

/**
 * Comparator for comparing Entry-objects by their x-value.
 * Created by philipp on 17/06/15.
 */
class EntryXComparator : Comparator<Entry> {
    override fun compare(entry1: Entry, entry2: Entry): Int {
        val diff = entry1.x - entry2.x

        return if (diff == 0f) 0
        else {
            if (diff > 0f) 1
            else -1
        }
    }
}
