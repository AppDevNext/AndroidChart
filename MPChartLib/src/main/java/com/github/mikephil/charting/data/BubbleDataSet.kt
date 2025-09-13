package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet

open class BubbleDataSet(yVals: MutableList<BubbleEntry>, label: String) : BarLineScatterCandleBubbleDataSet<BubbleEntry>(yVals, label), IBubbleDataSet {
    protected var mMaxSize: Float = 0f
    protected var mNormalizeSize: Boolean = true

    override var highlightCircleWidth: Float = 2.5f

    override fun calcMinMax(e: BubbleEntry) {
        super.calcMinMax(e)

        val size = e.size

        if (size > mMaxSize) {
            mMaxSize = size
        }
    }

    override fun copy(): DataSet<BubbleEntry> {
        val entries: MutableList<BubbleEntry> = ArrayList()
        for (i in mEntries.indices) {
            entries.add(mEntries[i].copy())
        }
        val copied = BubbleDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(bubbleDataSet: BubbleDataSet) {
        bubbleDataSet.highlightCircleWidth = highlightCircleWidth
        bubbleDataSet.mNormalizeSize = mNormalizeSize
    }

    override val maxSize: Float
        get() = mMaxSize

    override val isNormalizeSizeEnabled: Boolean
        get() = mNormalizeSize

    fun setNormalizeSizeEnabled(normalizeSize: Boolean) {
        mNormalizeSize = normalizeSize
    }
}
