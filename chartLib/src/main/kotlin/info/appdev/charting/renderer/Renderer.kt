package info.appdev.charting.renderer

import info.appdev.charting.utils.ViewPortHandler

abstract class Renderer(
    /**
     * the component that handles the drawing area of the chart and it's offsets
     */
    @JvmField
    protected var viewPortHandler: ViewPortHandler
)
