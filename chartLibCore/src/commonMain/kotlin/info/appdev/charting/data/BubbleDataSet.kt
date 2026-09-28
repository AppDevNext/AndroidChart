package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.IBubbleDataSet
import info.appdev.charting.utils.convertDpToPixel

open class BubbleDataSet(yVals: MutableList<BubbleEntryFloat>, label: String) : BarLineScatterCandleBubbleDataSet<BubbleEntryFloat>(yVals, label), IBubbleDataSet {
    protected var mMaxSize: Float = 0f
    protected var mNormalizeSize: Boolean = true

    private var mHighlightCircleWidth = 2.5f

    override fun calcMinMax(entry: BubbleEntryFloat) {
        super.calcMinMax(entry)

        val size = entry.size

        if (size > mMaxSize) {
            mMaxSize = size
        }
    }

    override fun copy(): DataSet<BubbleEntryFloat> {
        val entries: MutableList<BubbleEntryFloat> = ArrayList()
        for (i in entriesInternal.indices) {
            entries.add(entriesInternal[i].copy())
        }
        val copied = BubbleDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(bubbleDataSet: BubbleDataSet) {
        bubbleDataSet.mHighlightCircleWidth = mHighlightCircleWidth
        bubbleDataSet.mNormalizeSize = mNormalizeSize
    }

    override val maxSize: Float
        get() = mMaxSize
    override var isNormalizeSizeEnabled: Boolean
        get() = mNormalizeSize
        set(value) {
            mNormalizeSize = value
        }
    override var highlightCircleWidth: Float
        get() = mHighlightCircleWidth
        set(value) {
            mHighlightCircleWidth = value.convertDpToPixel()
        }
}
