package com.github.mikephil.charting.components

import android.graphics.DashPathEffect
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.utils.ColorTemplate


class LegendEntry {
    constructor()

    /**
     *
     * @param label The legend entry text. A `null` label will start a group.
     * @param form The form to draw for this entry.
     * @param formSize Set to NaN to use the legend's default.
     * @param formLineWidth Set to NaN to use the legend's default.
     * @param formLineDashEffect Set to nil to use the legend's default.
     * @param formColor The color for drawing the form.
     */
    constructor(
        label: String?,
        form: LegendForm?,
        formSize: Float,
        formLineWidth: Float,
        formLineDashEffect: DashPathEffect?,
        formColor: Int
    ) {
        this.label = label
        this.form = form
        this.formSize = formSize
        this.formLineWidth = formLineWidth
        this.formLineDashEffect = formLineDashEffect
        this.formColor = formColor
    }

    /**
     * The legend entry text.
     * A `null` label will start a group.
     */
    var label: String? = null

    /**
     * The form to draw for this entry.
     *
     * `NONE` will avoid drawing a form, and any related space.
     * `EMPTY` will avoid drawing a form, but keep its space.
     * `DEFAULT` will use the Legend's default.
     */
    var form: LegendForm? = LegendForm.DEFAULT

    /**
     * Form size will be considered except for when .None is used
     *
     * Set as NaN to use the legend's default
     */
    var formSize: Float = Float.Companion.NaN

    /**
     * Line width used for shapes that consist of lines.
     *
     * Set as NaN to use the legend's default
     */
    var formLineWidth: Float = Float.Companion.NaN

    /**
     * Line dash path effect used for shapes that consist of lines.
     *
     * Set to null to use the legend's default
     */
    var formLineDashEffect: DashPathEffect? = null

    /**
     * The color for drawing the form
     */
    var formColor: Int = ColorTemplate.COLOR_NONE
}