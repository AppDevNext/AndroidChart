package com.github.mikephil.charting.matrix

import kotlin.math.sqrt

/**
 * Simple 3D vector class. Handles basic vector math for 3D vectors.
 */
class Vector3 {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f

    constructor()

    constructor(array: FloatArray) {
        set(array[0], array[1], array[2])
    }

    constructor(xValue: Float, yValue: Float, zValue: Float) {
        set(xValue, yValue, zValue)
    }

    constructor(other: Vector3) {
        set(other)
    }

    fun add(other: Vector3) {
        x += other.x
        y += other.y
        z += other.z
    }

    fun add(otherX: Float, otherY: Float, otherZ: Float) {
        x += otherX
        y += otherY
        z += otherZ
    }

    fun subtract(other: Vector3) {
        x -= other.x
        y -= other.y
        z -= other.z
    }

    fun subtractMultiple(other: Vector3, multiplicator: Float) {
        x -= other.x * multiplicator
        y -= other.y * multiplicator
        z -= other.z * multiplicator
    }

    fun multiply(magnitude: Float) {
        x *= magnitude
        y *= magnitude
        z *= magnitude
    }

    fun multiply(other: Vector3) {
        x *= other.x
        y *= other.y
        z *= other.z
    }

    fun divide(magnitude: Float) {
        if (magnitude != 0.0f) {
            x /= magnitude
            y /= magnitude
            z /= magnitude
        }
    }

    fun set(other: Vector3) {
        x = other.x
        y = other.y
        z = other.z
    }

    fun set(xValue: Float, yValue: Float, zValue: Float) {
        x = xValue
        y = yValue
        z = zValue
    }

    fun dot(other: Vector3): Float {
        return (x * other.x) + (y * other.y) + (z * other.z)
    }

    fun cross(other: Vector3): Vector3 {
        return Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    fun length(): Float {
        return sqrt(length2().toDouble()).toFloat()
    }

    fun length2(): Float {
        return (x * x) + (y * y) + (z * z)
    }

    fun distance2(other: Vector3): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return (dx * dx) + (dy * dy) + (dz * dz)
    }

    fun normalize(): Float {
        val magnitude = length()

        // TODO: I'm choosing safety over speed here.
        if (magnitude != 0.0f) {
            x /= magnitude
            y /= magnitude
            z /= magnitude
        }

        return magnitude
    }

    fun zero() {
        set(0.0f, 0.0f, 0.0f)
    }

    fun pointsInSameDirection(other: Vector3): Boolean {
        return this.dot(other) > 0
    }

    companion object {
        val ZERO: Vector3 = Vector3(0f, 0f, 0f)
        val UNIT_X: Vector3 = Vector3(1f, 0f, 0f)
        val UNIT_Y: Vector3 = Vector3(0f, 1f, 0f)
        val UNIT_Z: Vector3 = Vector3(0f, 0f, 1f)
    }
}
