
package com.github.mikephil.charting.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.highlight.CombinedHighlighter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.CombinedDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.renderer.CombinedChartRenderer;

/**
 * This chart class allows the combination of lines, bars, scatter and candle
 * data all displayed in one chart area.
 *
 * @author Philipp Jahoda
 */
@SuppressWarnings("unused")
public class CombinedChart extends BarLineChartBase<CombinedData> implements CombinedDataProvider {

	/**
	 * if set to true, all values are drawn above their bars, instead of below
	 * their top
	 */
	private boolean mDrawValueAboveBar = true;


	/**
	 * flag that indicates whether the highlight should be full-bar oriented, or single-value?
	 */
	protected boolean mHighlightFullBarEnabled = false;

	/**
	 * if set to true, a grey area is drawn behind each bar that indicates the
	 * maximum value
	 */
	private boolean mDrawBarShadow = false;

	protected DrawOrder[] mDrawOrder;

	/**
	 * enum that allows to specify the order in which the different data objects
	 * for the combined-chart are drawn
	 */
	public enum DrawOrder {
		BAR, BUBBLE, LINE, CANDLE, SCATTER
	}

	public CombinedChart(Context context) {
		super(context);
	}

	public CombinedChart(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CombinedChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init() {
		super.init();

		// Default values are not ready here yet
		mDrawOrder = new DrawOrder[]{
				DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.LINE, DrawOrder.CANDLE, DrawOrder.SCATTER
		};

		setHighlighter(new CombinedHighlighter(this, this));

		// Old default behaviour
		setHighlightFullBarEnabled(true);

		mRenderer = new CombinedChartRenderer(this, mAnimator, mViewPortHandler);
	}

	@Override
	public CombinedData getCombinedData() {
		return mData;
	}

	@Override
	public void setData(CombinedData data) {
		super.setData(data);
		setHighlighter(new CombinedHighlighter(this, this));
		((CombinedChartRenderer) mRenderer).createRenderers();
		mRenderer.initBuffers();
	}

	/**
	 * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch
	 * point
	 * inside the CombinedChart.
	 */
	@Override
	public Highlight getHighlightByTouchPoint(float x, float y) {

		if (mData == null) {
			Log.e(LOG_TAG, "Can't select by touch. No data set.");
			return null;
		} else {
			Highlight h = getHighlighter().getHighlight(x, y);
			if (h == null || !isHighlightFullBarEnabled()) {
				return h;
			}

			// For isHighlightFullBarEnabled, remove stackIndex
			return new Highlight(h.getX(), h.getY(),
					h.getXPx(), h.getYPx(),
					h.getDataSetIndex(), -1, h.getAxis());
		}
	}

	@Override
	public LineData getLineData() {
		if (mData == null) {
			return null;
		}
		return mData.getLineData();
	}

	@Override
	public BarData getBarData() {
		if (mData == null) {
			return null;
		}
		return mData.getBarData();
	}

	@Override
	public ScatterData getScatterData() {
		if (mData == null) {
			return null;
		}
		return mData.getScatterData();
	}

	@Override
	public CandleData getCandleData() {
		if (mData == null) {
			return null;
		}
		return mData.getCandleData();
	}

	@Override
	public BubbleData getBubbleData() {
		if (mData == null) {
			return null;
		}
		return mData.getBubbleData();
	}

	@Override
	public boolean isDrawBarShadowEnabled() {
		return mDrawBarShadow;
	}

	@Override
	public boolean isDrawValueAboveBarEnabled() {
		return mDrawValueAboveBar;
	}

	/**
	 * If set to true, all values are drawn above their bars, instead of below
	 * their top.
	 */
	public void setDrawValueAboveBar(boolean enabled) {
		mDrawValueAboveBar = enabled;
	}


	/**
	 * If set to true, a grey area is drawn behind each bar that indicates the
	 * maximum value. Enabling his will reduce performance by about 50%.
	 */
	public void setDrawBarShadow(boolean enabled) {
		mDrawBarShadow = enabled;
	}

	/**
	 * Set this to true to make the highlight operation full-bar oriented,
	 * false to make it highlight single values (relevant only for stacked).
	 */
	public void setHighlightFullBarEnabled(boolean enabled) {
		mHighlightFullBarEnabled = enabled;
	}

	/**
	 * @return true the highlight operation is be full-bar oriented, false if single-value
	 */
	@Override
	public boolean isHighlightFullBarEnabled() {
		return mHighlightFullBarEnabled;
	}

	/**
	 * Returns the currently set draw order.
	 */
	public DrawOrder[] getDrawOrder() {
		return mDrawOrder;
	}

	/**
	 * Sets the order in which the provided data objects should be drawn. The
	 * earlier you place them in the provided array, the further they will be in
	 * the background. e.g. if you provide new DrawOrer[] { DrawOrder.BAR,
	 * DrawOrder.LINE }, the bars will be drawn behind the lines.
	 */
	public void setDrawOrder(DrawOrder[] order) {
		if (order == null || order.length <= 0) {
			return;
		}
		mDrawOrder = order;
	}

	/**
	 * draws all MarkerViews on the highlighted positions
	 */
	protected void drawMarkers(Canvas canvas) {

		// if there is no marker view or drawing marker is disabled
		if (mMarkers == null || !isDrawMarkersEnabled() || !valuesToHighlight()) {
			return;
		}

		for (int i = 0; i < mIndicesToHighlight.length; i++) {

			Highlight highlight = mIndicesToHighlight[i];
			IDataSet set = mData.getDataSetByHighlight(highlight);

			Entry e = mData.getEntryForHighlight(highlight);
			if (e == null || set == null) {
				continue;
			}

			int entryIndex = set.getEntryIndex(e);

			// make sure entry not null
			if (entryIndex > set.getEntryCount() * mAnimator.getPhaseX()) {
				continue;
			}

			float[] pos = getMarkerPosition(highlight);

			// check bounds
			if (!mViewPortHandler.isInBounds(pos[0], pos[1])) {
				continue;
			}

			// callbacks to update the content
			IMarker markerItem =  mMarkers.get(i % mMarkers.size());
            markerItem.refreshContent(e, highlight);

			// draw the marker
			markerItem.draw(canvas, pos[0], pos[1]);
		}
	}

    @Override
    public String getAccessibilityDescription() {
        return "This is a combined chart";
    }
}
