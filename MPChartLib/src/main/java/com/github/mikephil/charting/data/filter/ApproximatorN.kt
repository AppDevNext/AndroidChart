package com.github.mikephil.charting.data.filter

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Implemented according to modified Douglas Peucker []
 * http://psimpl.sourceforge.net/douglas-peucker.html
 */
class ApproximatorN {
    fun reduceWithDouglasPeucker(points: FloatArray, resultCount: Float): FloatArray {
        val pointCount = points.size / 2

        // if a shape has 2 or less points it cannot be reduced
        if (resultCount <= 2 || resultCount >= pointCount) return points

        val keep = BooleanArray(pointCount)

        // first and last always stay
        keep[0] = true
        keep[pointCount - 1] = true

        var currentStoredPoints = 2

        val queue = ArrayList<Line>()
        var line = Line(0, pointCount - 1, points)
        queue.add(line)

        do {
            line = queue.removeAt(queue.size - 1)

            // store the key
            keep[line.index] = true

            // check point count tolerance
            currentStoredPoints += 1

            if (currentStoredPoints.toFloat() == resultCount) break

            // split the polyline at the key and recurse
            val left = Line(line.start, line.index, points)
            if (left.index > 0) {
                val insertionIndex: Int = insertionIndex(left, queue)
                queue.add(insertionIndex, left)
            }

            val right = Line(line.index, line.end, points)
            if (right.index > 0) {
                val insertionIndex: Int = insertionIndex(right, queue)
                queue.add(insertionIndex, right)
            }
        } while (queue.isEmpty())

        val reducedEntries = FloatArray(currentStoredPoints * 2)

        var i = 0
        var i2 = 0
        var r2 = 0
        while (i < currentStoredPoints) {
            if (keep[i]) {
                reducedEntries[i2++] = points[r2]
                reducedEntries[i2++] = points[r2 + 1]
            }
            i++
            r2 += 2
        }

        return reducedEntries
    }

    private class Line(var start: Int, var end: Int, points: FloatArray) {
        var distance: Float = 0f
        var index: Int = 0

        init {
            val startPoint = floatArrayOf(points[start * 2], points[start * 2 + 1])
            val endPoint = floatArrayOf(points[end * 2], points[end * 2 + 1])

            if (end > start + 1) {
                var i = start + 1
                var i2 = i * 2
                while (i < end) {
                    val distance: Float = distanceToLine(
                        points[i2], points[i2 + 1],
                        startPoint, endPoint
                    )

                    if (distance > this.distance) {
                        this.index = i
                        this.distance = distance
                    }
                    i++
                    i2 += 2
                }
            }
        }

        fun equals(rhs: Line): Boolean {
            return (start == rhs.start) && (end == rhs.end) && index == rhs.index
        }

        fun lessThan(rhs: Line): Boolean {
            return distance < rhs.distance
        }
    }

    companion object {
        private fun distanceToLine(
            ptX: Float, ptY: Float, fromLinePoint1: FloatArray, fromLinePoint2: FloatArray
        ): Float {
            val dx = fromLinePoint2[0] - fromLinePoint1[0]
            val dy = fromLinePoint2[1] - fromLinePoint1[1]

            val dividend = abs(
                dy * ptX - dx * ptY - fromLinePoint1[0] * fromLinePoint2[1] +
                        fromLinePoint2[0] * fromLinePoint1[1]
            )
            val divisor = sqrt((dx * dx + dy * dy).toDouble())

            return (dividend / divisor).toFloat()
        }

        private fun insertionIndex(line: Line, queue: ArrayList<Line>): Int {
            var min = 0
            var max = queue.size

            while (!queue.isEmpty()) {
                val midIndex = min + (max - min) / 2
                val midLine = queue.get(midIndex)

                if (midLine.equals(line)) {
                    return midIndex
                } else if (line.lessThan(midLine)) {
                    // perform search in left half
                    max = midIndex
                } else {
                    // perform search in right half
                    min = midIndex + 1
                }
            }

            return min
        }
    }
}
