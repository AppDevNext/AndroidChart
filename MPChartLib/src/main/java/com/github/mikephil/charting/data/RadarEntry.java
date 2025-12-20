package com.github.mikephil.charting.data;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

/**
 * Created by philipp on 13/06/16.
 */
@SuppressLint("ParcelCreator")
public class RadarEntry extends Entry {

    public RadarEntry(float value) {
        super(0f, value);
    }

    public RadarEntry(float value, Object data) {
        super(0f, value, data);
    }

    /**
     * This is the same as getY(). Returns the value of the RadarEntry.
     */
    public float getValue() {
        return getY();
    }

    @NonNull
	public RadarEntry copy() {
		return new RadarEntry(getY(), getData());
    }

    @Deprecated
    @Override
    public void setX(float x) {
        super.setX(x);
    }

    @Deprecated
    @Override
    public float getX() {
        return super.getX();
    }
}
