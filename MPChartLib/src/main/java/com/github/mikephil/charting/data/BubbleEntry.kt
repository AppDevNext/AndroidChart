package com.github.mikephil.charting.data

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable

/**
 * Subclass of Entry that holds a value for one entry in a BubbleChart. Bubble
 * chart implementation: Copyright 2015 Pierre-Marc Airoldi Licensed under
 * Apache License 2.0
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ParcelCreator")
class BubbleEntry : Entry {
    /**
     * Returns the size of this entry (the size of the bubble).
     *
     * @return
     */
    /** size value  */
    var size: Float = 0f

    /**
     * Constructor.
     *
     * @param x The value on the x-axis.
     * @param y The value on the y-axis.
     * @param size The size of the bubble.
     */
    constructor(x: Float, y: Float, size: Float) : super(x, y) {
        this.size = size
    }

    /**
     * Constructor.
     *
     * @param x The value on the x-axis.
     * @param y The value on the y-axis.
     * @param size The size of the bubble.
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: Float, y: Float, size: Float, data: Any?) : super(x, y, data) {
        this.size = size
    }

    /**
     * Constructor.
     *
     * @param x The value on the x-axis.
     * @param y The value on the y-axis.
     * @param size The size of the bubble.
     * @param icon Icon image
     */
    constructor(x: Float, y: Float, size: Float, icon: Drawable?) : super(x, y, icon) {
        this.size = size
    }

    /**
     * Constructor.
     *
     * @param x The value on the x-axis.
     * @param y The value on the y-axis.
     * @param size The size of the bubble.
     * @param icon Icon image
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: Float, y: Float, size: Float, icon: Drawable?, data: Any?) : super(x, y, icon, data) {
        this.size = size
    }

    public override fun copy(): BubbleEntry {
        val c = BubbleEntry(x, y, this.size, data)
        return c
    }
}
