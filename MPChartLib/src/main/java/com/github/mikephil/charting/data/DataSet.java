
package com.github.mikephil.charting.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The DataSet class represents one group or type of entries (Entry) in the
 * Chart that belong together. It is designed to logically separate different
 * groups of values inside the Chart (e.g. the values for a specific line in the
 * LineChart, or the values of a specific group of bars in the BarChart).
 *
 * @author Philipp Jahoda
 */
public abstract class DataSet<T extends Entry> extends BaseDataSet<T> implements Serializable {

    /**
     * the entries that this DataSet represents / holds together
     */
    protected List<T> mEntries;

    /**
     * maximum y-value in the value array
     */
    protected float mYMax = -Float.MAX_VALUE;

    /**
     * minimum y-value in the value array
     */
    protected float mYMin = Float.MAX_VALUE;

    /**
     * maximum x-value in the value array
     */
    protected float mXMax = -Float.MAX_VALUE;

    /**
     * minimum x-value in the value array
     */
    protected float mXMin = Float.MAX_VALUE;


    /**
     * Creates a new DataSet object with the given values (entries) it represents. Also, a
     * label that describes the DataSet can be specified. The label can also be
     * used to retrieve the DataSet from a ChartData object.
     */
    public DataSet(List<T> entries, String label) {
        super(label);
        this.mEntries = entries;

        if (mEntries == null)
            mEntries = new ArrayList<>();

        calcMinMax();
    }

    @Override
    public void calcMinMax() {

        mYMax = -Float.MAX_VALUE;
        mYMin = Float.MAX_VALUE;
        mXMax = -Float.MAX_VALUE;
        mXMin = Float.MAX_VALUE;

        if (mEntries == null || mEntries.isEmpty())
            return;

        for (T e : mEntries) {
            calcMinMax(e);
        }
    }

    @Override
    public void calcMinMaxY(float fromX, float toX) {
        mYMax = -Float.MAX_VALUE;
        mYMin = Float.MAX_VALUE;

        if (mEntries == null || mEntries.isEmpty())
            return;

        int indexFrom = getEntryIndex(fromX, Float.NaN, Rounding.DOWN);
        int indexTo = getEntryIndex(toX, Float.NaN, Rounding.UP);

        if (indexTo < indexFrom) return;

        for (int i = indexFrom; i <= indexTo; i++) {

            // only recalculate y
            calcMinMaxY(mEntries.get(i));
        }
    }

    /**
     * Updates the min and max x and y value of this DataSet based on the given Entry.
     */
    protected void calcMinMax(T entry) {

        if (entry == null)
            return;

        calcMinMaxX(entry);
        calcMinMaxY(entry);
    }

    protected void calcMinMaxX(T entry) {

        if (entry.getX() < mXMin)
            mXMin = entry.getX();

        if (entry.getX() > mXMax)
            mXMax = entry.getX();
    }

    protected void calcMinMaxY(T entry) {

        if (entry.getY() < mYMin)
            mYMin = entry.getY();

        if (entry.getY() > mYMax)
            mYMax = entry.getY();
    }

    @Override
    public int getEntryCount() {
        return mEntries.size();
    }

    /**
     * This method is deprecated.
     * Use getEntries() instead.
     */
    @Deprecated
    public List<T> getValues() {
        return mEntries;
    }

    /**
     * Returns the array of entries that this DataSet represents.
     */
    public List<T> getEntries() {
        return mEntries;
    }

    /**
     * This method is deprecated.
     * Use setEntries(...) instead.
     */
    @Deprecated
    public void setValues(List<T> values) {
        setEntries(values);
    }

    /**
     * Sets the array of entries that this DataSet represents, and calls notifyDataSetChanged()
     */
    public void setEntries(List<T> entries) {
        mEntries = entries;
        notifyDataSetChanged();
    }

    /**
     * Provides an exact copy of the DataSet this method is used on.
     */
    public abstract DataSet<T> copy();

    protected void copy(DataSet dataSet) {
        super.copy(dataSet);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(toSimpleString());
        for (int i = 0; i < mEntries.size(); i++) {
            buffer.append(mEntries.get(i).toString()).append(" ");
        }
        return buffer.toString();
    }

    /**
     * Returns a simple string representation of the DataSet with the type and
     * the number of Entries.
     */
    public String toSimpleString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("DataSet, label: " + (getLabel() == null ? "" : getLabel()) + ", entries: " + mEntries.size() +
                "\n");
        return buffer.toString();
    }

    @Override
    public float getYMin() {
        return mYMin;
    }

    @Override
    public float getYMax() {
        return mYMax;
    }

    @Override
    public float getXMin() {
        return mXMin;
    }

    @Override
    public float getXMax() {
        return mXMax;
    }

    @Override
    public void addEntryOrdered(T entry) {

        if (entry == null)
            return;

        if (mEntries == null) {
            mEntries = new ArrayList<>();
        }

        calcMinMax(entry);

        if (!mEntries.isEmpty() && mEntries.get(mEntries.size() - 1).getX() > entry.getX()) {
            int closestIndex = getEntryIndex(entry.getX(), entry.getY(), Rounding.UP);
            mEntries.add(closestIndex, entry);
        } else {
            mEntries.add(entry);
        }
    }

    @Override
    public void clear() {
        mEntries.clear();
        notifyDataSetChanged();
    }

    @Override
    public boolean addEntry(T entry) {

        if (entry == null)
            return false;

        List<T> values = getEntries();
        if (values == null) {
            values = new ArrayList<>();
        }

        calcMinMax(entry);

        // add the entry
        return values.add(entry);
    }

    @Override
    public boolean removeEntry(T entry) {

        if (entry == null)
            return false;

        if (mEntries == null)
            return false;

        // remove the entry
        boolean removed = mEntries.remove(entry);

        if (removed) {
            calcMinMax();
        }

        return removed;
    }

    @Override
    public int getEntryIndex(Entry entry) {
        return mEntries.indexOf(entry);
    }

    @Override
    public T getEntryForXValue(float xValue, float closestToY, Rounding rounding) {

        int index = getEntryIndex(xValue, closestToY, rounding);
        if (index > -1)
            return mEntries.get(index);
        return null;
    }

    @Override
    public T getEntryForXValue(float xValue, float closestToY) {
        return getEntryForXValue(xValue, closestToY, Rounding.CLOSEST);
    }

    @Override
    public T getEntryForIndex(int index) {
        if (index < 0)
            return null;
        if (index >= mEntries.size())
            return null;
        return mEntries.get(index);
    }

    @Override
    public int getEntryIndex(float xValue, float closestToY, Rounding rounding) {

        if (mEntries == null || mEntries.isEmpty())
            return -1;

        int low = 0;
        int high = mEntries.size() - 1;
        int closest = high;

        while (low < high) {
            int m = low + (high - low) / 2;

            Entry currentEntry = mEntries.get(m);

            if (currentEntry != null) {
                Entry nextEntry = mEntries.get(m + 1);

                if (nextEntry != null) {
                    final float d1 = currentEntry.getX() - xValue,
                                d2 = nextEntry.getX() - xValue,
                                ad1 = Math.abs(d1),
                                ad2 = Math.abs(d2);

                    if (ad2 < ad1) {
                        // [m + 1] is closer to xValue
                        // Search in an higher place
                        low = m + 1;
                    } else if (ad1 < ad2) {
                        // [m] is closer to xValue
                        // Search in a lower place
                        high = m;
                    } else {
                        // We have multiple sequential x-value with same distance

                        if (d1 >= 0.0) {
                            // Search in a lower place
                            high = m;
                        } else if (d1 < 0.0) {
                            // Search in an higher place
                            low = m + 1;
                        }
                    }

                    closest = high;
                }
            }
        }

        if (closest != -1) {
            Entry closestEntry = mEntries.get(closest);
            if (closestEntry != null) {
                float closestXValue = closestEntry.getX();
                if (rounding == Rounding.UP) {
                    // If rounding up, and found x-value is lower than specified x, and we can go upper...
                    if (closestXValue < xValue && closest < mEntries.size() - 1) {
                        ++closest;
                    }
                } else if (rounding == Rounding.DOWN) {
                    // If rounding down, and found x-value is upper than specified x, and we can go lower...
                    if (closestXValue > xValue && closest > 0) {
                        --closest;
                    }
                }

                // Search by closest to y-value
                if (!Float.isNaN(closestToY)) {
                    while (closest > 0 && mEntries.get(closest - 1).getX() == closestXValue)
                        closest -= 1;

                    float closestYValue = closestEntry.getY();
                    int closestYIndex = closest;

                    while (true) {
                        closest += 1;
                        if (closest >= mEntries.size())
                            break;

                        final Entry value = mEntries.get(closest);

                        if (value == null) {
                            continue;
                        }

                        if (value.getX() != closestXValue)
                            break;

                        if (Math.abs(value.getY() - closestToY) <= Math.abs(closestYValue - closestToY)) {
                            closestYValue = closestToY;
                            closestYIndex = closest;
                        }
                    }

                    closest = closestYIndex;
                }
            }
        }
        return closest;
    }

    @Override
    public List<T> getEntriesForXValue(float xValue) {

        List<T> entries = new ArrayList<>();

        int low = 0;
        int high = mEntries.size() - 1;

        while (low <= high) {
            int m = (high + low) / 2;
            T entry = mEntries.get(m);

            // if we have a match
            if (xValue == entry.getX()) {
                while (m > 0 && mEntries.get(m - 1).getX() == xValue)
                    m--;

                high = mEntries.size();

                // loop over all "equal" entries
                for (; m < high; m++) {
                    entry = mEntries.get(m);
                    if (entry.getX() == xValue) {
                        entries.add(entry);
                    } else {
                        break;
                    }
                }

                break;
            } else {
                if (xValue > entry.getX())
                    low = m + 1;
                else
                    high = m - 1;
            }
        }

        return entries;
    }

    /**
     * Determines how to round DataSet index values for
     * {@link DataSet#getEntryIndex(float, float, Rounding)} DataSet.getEntryIndex()}
     * when an exact x-index is not found.
     */
    public enum Rounding {
        UP,
        DOWN,
        CLOSEST,
    }
}