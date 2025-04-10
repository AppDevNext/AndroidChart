
package com.github.mikephil.charting.buffer;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

public class BarBuffer extends AbstractBuffer<IBarDataSet> {

    protected int mDataSetIndex = 0;
	protected int mDataSetCount;
    protected boolean mContainsStacks;
    protected boolean mInverted = false;

    /** width of the bar on the x-axis, in values (not pixels) */
    protected float mBarWidth = 1f;

    public BarBuffer(int dataSetCount, boolean containsStacks) {
        super();
		this.mDataSetCount = dataSetCount;
        this.mContainsStacks = containsStacks;
    }

    public void setBarWidth(float barWidth) {
        this.mBarWidth = barWidth;
    }

    public void setDataSet(int index) {
        this.mDataSetIndex = index;
    }

    public void setInverted(boolean inverted) {
        this.mInverted = inverted;
    }

    protected void addBar(float left, float top, float right, float bottom) {
        buffer.add(left);
        buffer.add(top);
        buffer.add(right);
        buffer.add(bottom);
    }

    @Override
    public void feed(IBarDataSet data) {

        float size = data.getEntryCount() * phaseX;
        float barWidthHalf = mBarWidth / 2f;

        for (int i = 0; i < size; i++) {

            BarEntry e = data.getEntryForIndex(i);

            if(e == null)
                continue;

            float x = e.getX();
            float y = e.getY();
            float[] vals = e.getYVals();

            if (!mContainsStacks || vals == null) {

                float left = x - barWidthHalf;
                float right = x + barWidthHalf;
                float bottom, top;

                if (mInverted) {
                    bottom = y >= 0 ? y : 0;
                    top = y <= 0 ? y : 0;
                } else {
                    top = y >= 0 ? y : 0;
                    bottom = y <= 0 ? y : 0;
                }

                // multiply the height of the rect with the phase
                if (top > 0)
                    top *= phaseY;
                else
                    bottom *= phaseY;

                addBar(left, top, right, bottom);

            } else {

                float posY = 0f;
                float negY = -e.getNegativeSum();
                float yStart;

                // fill the stack
				for (float value : vals) {

					if (value == 0.0f && (posY == 0.0f || negY == 0.0f)) {
						// Take care of the situation of a 0.0 value, which overlaps a non-zero bar
						y = value;
						yStart = y;
					} else if (value >= 0.0f) {
						y = posY;
						yStart = posY + value;
						posY = yStart;
					} else {
						y = negY;
						yStart = negY + Math.abs(value);
						negY += Math.abs(value);
					}

					float left = x - barWidthHalf;
					float right = x + barWidthHalf;
					float bottom, top;

					if (mInverted) {
						bottom = Math.max(y, yStart);
						top = Math.min(y, yStart);
					} else {
						top = Math.max(y, yStart);
						bottom = Math.min(y, yStart);
					}

					// multiply the height of the rect with the phase
					top *= phaseY;
					bottom *= phaseY;

					addBar(left, top, right, bottom);
				}
            }
        }

        reset();
    }
}
