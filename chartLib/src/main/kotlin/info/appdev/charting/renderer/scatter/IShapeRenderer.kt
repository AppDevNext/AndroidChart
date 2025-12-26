package info.appdev.charting.renderer.scatter

import android.graphics.Canvas
import android.graphics.Paint
import info.appdev.charting.interfaces.datasets.IScatterDataSet
import info.appdev.charting.utils.ViewPortHandler

interface IShapeRenderer {
    /**
     * Renders the provided ScatterDataSet with a shape.
     *
     * @param canvas               Canvas object for drawing the shape
     * @param dataSet         The DataSet to be drawn
     * @param viewPortHandler Contains information about the current state of the view
     * @param posX            Position to draw the shape at
     * @param posY            Position to draw the shape at
     * @param renderPaint     Paint object used for styling and drawing
     */
    fun renderShape(
        canvas: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler?,
        posX: Float, posY: Float, renderPaint: Paint
    )
}
