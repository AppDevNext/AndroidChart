package com.github.mikephil.charting.interfaces.dataprovider

import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface BubbleDataProvider : BarLineScatterCandleBubbleDataProvider {
    val bubbleData: BubbleData?
}
