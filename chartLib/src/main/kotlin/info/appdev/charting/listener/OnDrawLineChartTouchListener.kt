package info.appdev.charting.listener

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

class OnDrawLineChartTouchListener : SimpleOnGestureListener(), OnTouchListener {
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }
}
