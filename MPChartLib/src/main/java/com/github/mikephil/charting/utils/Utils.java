
package com.github.mikephil.charting.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;

import androidx.annotation.NonNull;

/**
 * Utilities class that has some helper methods. Needs to be initialized by
 * calling Utils.init(...) before usage. Inside the Chart.init() method, this is
 * done, if the Utils are used before that, Utils.init(...) needs to be called
 * manually.
 *
 * @author Philipp Jahoda
 */
@SuppressWarnings("JavaDoc")
public abstract class Utils {

	public static int minimumFlingVelocity = 50;
	public static int maximumFlingVelocity = 8000;
	public final static double DEG2RAD = (Math.PI / 180.0);
	public final static float FDEG2RAD = ((float) Math.PI / 180.f);

	@SuppressWarnings("unused")
	public final static double DOUBLE_EPSILON = Double.longBitsToDouble(1);

	@SuppressWarnings("unused")
	public final static float FLOAT_EPSILON = Float.intBitsToFloat(1);

	/**
	 * initialize method, called inside the Chart.init() method.
	 */
	@SuppressWarnings("deprecation")
	public static void init(@NonNull Context context) {
		ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
		minimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
		maximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
	}

	private static final Rect mCalcTextHeightRect = new Rect();

	/**
	 * calculates the approximate height of a text, depending on a demo text
	 * avoid repeated calls (e.g. inside drawing methods)
	 */
	public static int calcTextHeight(Paint paint, String demoText) {

		Rect r = mCalcTextHeightRect;
		r.set(0, 0, 0, 0);
		paint.getTextBounds(demoText, 0, demoText.length(), r);
		return r.height();
	}

	private static final Paint.FontMetrics mFontMetrics = new Paint.FontMetrics();

	public static float getLineHeight(Paint paint) {
		return getLineHeight(paint, mFontMetrics);
	}

	public static float getLineHeight(Paint paint, Paint.FontMetrics fontMetrics) {
		paint.getFontMetrics(fontMetrics);
		return fontMetrics.descent - fontMetrics.ascent;
	}

	public static float getLineSpacing(Paint paint) {
		return getLineSpacing(paint, mFontMetrics);
	}

	public static float getLineSpacing(Paint paint, Paint.FontMetrics fontMetrics) {
		paint.getFontMetrics(fontMetrics);
		return fontMetrics.ascent - fontMetrics.top + fontMetrics.bottom;
	}

	/**
	 * Returns a recyclable FSize instance.
	 * calculates the approximate size of a text, depending on a demo text
	 * avoid repeated calls (e.g. inside drawing methods)
	 *
	 * @param paint
	 * @param demoText
	 * @return A Recyclable FSize instance
	 */
	public static FSize calcTextSize(Paint paint, String demoText) {

		FSize result = FSize.Companion.getInstance(0, 0);
		calcTextSize(paint, demoText, result);
		return result;
	}

	private static final Rect mCalcTextSizeRect = new Rect();

	/**
	 * calculates the approximate size of a text, depending on a demo text
	 * avoid repeated calls (e.g. inside drawing methods)
	 *
	 * @param paint
	 * @param demoText
	 * @param outputFSize An output variable, modified by the function.
	 */
	public static void calcTextSize(Paint paint, String demoText, FSize outputFSize) {

		Rect r = mCalcTextSizeRect;
		r.set(0, 0, 0, 0);
		paint.getTextBounds(demoText, 0, demoText.length(), r);
		outputFSize.setWidth(r.width());
		outputFSize.setHeight(r.height());

	}

	/**
	 * Math.pow(...) is very expensive, so avoid calling it and create it
	 * yourself.
	 */
	static final int[] POW_10 = {
			1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000
	};

	private static final IValueFormatter mDefaultValueFormatter = generateDefaultValueFormatter();

	private static IValueFormatter generateDefaultValueFormatter() {
		return new DefaultValueFormatter(1);
	}

	/// - returns: The default value formatter used for all chart components that needs a default
	public static IValueFormatter getDefaultValueFormatter() {
		return mDefaultValueFormatter;
	}

	/**
	 * Returns a recyclable MPPointF instance.
	 * Calculates the position around a center point, depending on the distance
	 * from the center, and the angle of the position around the center.
	 *
	 * @param center
	 * @param dist
	 * @param angle  in degrees, converted to radians internally
	 * @return
	 */
	public static MPPointF getPosition(MPPointF center, float dist, float angle) {
		MPPointF p = MPPointF.Companion.getInstance(0, 0);
		getPosition(center, dist, angle, p);
		return p;
	}

	public static void getPosition(MPPointF center, float dist, float angle, MPPointF outputPoint) {
		outputPoint.setX((float) (center.getX() + dist * Math.cos(Math.toRadians(angle))));
		outputPoint.setY((float) (center.getY() + dist * Math.sin(Math.toRadians(angle))));
	}

	public static void velocityTrackerPointerUpCleanUpIfNecessary(MotionEvent ev, VelocityTracker tracker) {

		// Check the dot product of current velocities.
		// If the pointer that left was opposing another velocity vector, clear.
		tracker.computeCurrentVelocity(1000, maximumFlingVelocity);
		final int upIndex = ev.getActionIndex();
		final int id1 = ev.getPointerId(upIndex);
		final float x1 = tracker.getXVelocity(id1);
		final float y1 = tracker.getYVelocity(id1);
		for (int i = 0, count = ev.getPointerCount(); i < count; i++) {
			if (i == upIndex) {
				continue;
			}

			final int id2 = ev.getPointerId(i);
			final float x = x1 * tracker.getXVelocity(id2);
			final float y = y1 * tracker.getYVelocity(id2);

			final float dot = x + y;
			if (dot < 0) {
				tracker.clear();
				break;
			}
		}
	}

	/**
	 * returns an angle between 0.f < 360.f (not less than zero, less than 360)
	 */
	public static float getNormalizedAngle(float angle) {
		while (angle < 0.f) {
			angle += 360.f;
		}

		return angle % 360.f;
	}

	private static final Rect mDrawableBoundsCache = new Rect();

	public static void drawImage(Canvas canvas, Drawable drawable, int x, int y) {

		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();

		MPPointF drawOffset = MPPointF.Companion.getInstance();
		drawOffset.setX(x - (width / 2));
		drawOffset.setY(y - (height / 2));

		drawable.copyBounds(mDrawableBoundsCache);
		drawable.setBounds(
				mDrawableBoundsCache.left,
				mDrawableBoundsCache.top,
				mDrawableBoundsCache.left + width,
				mDrawableBoundsCache.top + width);

		int saveId = canvas.save();
		// translate to the correct position and draw
		canvas.translate(drawOffset.getX(), drawOffset.getY());
		drawable.draw(canvas);
		canvas.restoreToCount(saveId);
	}

	/**
	 * Returns a recyclable FSize instance.
	 * Represents size of a rotated rectangle by degrees.
	 *
	 * @param rectangleWidth
	 * @param rectangleHeight
	 * @param degrees
	 * @return A Recyclable FSize instance
	 */
	public static FSize getSizeOfRotatedRectangleByDegrees(float rectangleWidth, float rectangleHeight, float degrees) {
		final float radians = degrees * FDEG2RAD;
		return getSizeOfRotatedRectangleByRadians(rectangleWidth, rectangleHeight, radians);
	}

	/**
	 * Returns a recyclable FSize instance.
	 * Represents size of a rotated rectangle by radians.
	 *
	 * @param rectangleWidth
	 * @param rectangleHeight
	 * @param radians
	 * @return A Recyclable FSize instance
	 */
	public static FSize getSizeOfRotatedRectangleByRadians(float rectangleWidth, float rectangleHeight, float radians) {
		return FSize.Companion.getInstance(
				Math.abs(rectangleWidth * (float) Math.cos(radians)) + Math.abs(rectangleHeight * (float) Math.sin(radians)),
				Math.abs(rectangleWidth * (float) Math.sin(radians)) + Math.abs(rectangleHeight * (float) Math.cos(radians))
		);
	}

}
