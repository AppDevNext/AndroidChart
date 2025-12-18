package com.github.mikephil.charting.interfaces.datasets;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer;

public interface IScatterDataSet extends ILineScatterCandleRadarDataSet<Entry> {

    /**
     * the currently set scatter shape size
     */
    float getScatterShapeSize();

    /**
     * radius of the hole in the shape
     */
    float getScatterShapeHoleRadius();

    /**
     * the color for the hole in the shape
     */
    int getScatterShapeHoleColor();

    /**
     * the IShapeRenderer responsible for rendering this DataSet.
     */
    IShapeRenderer getShapeRenderer();
}
