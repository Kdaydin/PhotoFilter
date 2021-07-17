package com.kdaydin.photofilter.ui.customViews.overlay

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class GestureListener(val view: OverlayView?) : GestureDetector.OnGestureListener,
    ScaleGestureDetector.SimpleOnScaleGestureListener() {

    private var mScaleFactor = 1f


    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val distanceTimeFactor = 0.4f
        val totalDx = distanceTimeFactor * velocityX / 2
        val totalDy = distanceTimeFactor * velocityY / 2
        view?.onAnimateMove(totalDx, totalDy, (1000 * distanceTimeFactor).toLong())
        return true
    }

    fun onDoubleTap(e: MotionEvent?): Boolean {
        view?.onResetLocation()
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        view?.onMove(-distanceX, -distanceY)
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return false
    }

    fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        mScaleFactor *= detector?.scaleFactor ?: 0f

        // Don't let the object get too small or too large.
        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f))
        view?.invalidate()
        return true

    }
}