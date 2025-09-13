package com.github.mikephil.charting.exception

object DrawingDataSetNotCreatedException : RuntimeException() {
    private fun readResolve(): Any = DrawingDataSetNotCreatedException

    /**
     *
     */
    private const val serialVersionUID = 1L
}
