package com.kdaydin.photofilter.ui.customViews.overlay

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.os.Environment
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.OvershootInterpolator
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*


class OverlayView : View {
    private var gestures: GestureDetector? = null
    private var translate: Matrix? = null
    private var picture: Bitmap? = null
    private var overlay: Bitmap? = null
    private var animateStart: Matrix? = null
    private var animateInterpolator: OvershootInterpolator? = null
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var totalAnimDx = 0f
    private var totalAnimDy = 0f
    private var mScaleFactor = 1f
    private var paint: Paint? = Paint()
    private var testMatrix = Matrix()
    private var noneOverlay = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f))

            invalidate()
            return true
        }
    }

    private val mScaleDetector = ScaleGestureDetector(context, scaleListener)


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        if (isInEditMode.not()) {
            init()
        }
    }


    fun onAnimateMove(dx: Float, dy: Float, duration: Long) {
        animateStart = Matrix(translate)
        animateInterpolator = OvershootInterpolator()
        startTime = System.currentTimeMillis()
        endTime = startTime + duration
        totalAnimDx = dx
        totalAnimDy = dy
        post { onAnimateStep() }
    }

    private fun onAnimateStep() {
        val curTime = System.currentTimeMillis()
        val percentTime = (curTime - startTime).toFloat() / (endTime - startTime).toFloat()
        val percentDistance: Float = animateInterpolator?.getInterpolation(percentTime) ?: 0f
        val curDx = percentDistance * totalAnimDx
        val curDy = percentDistance * totalAnimDy
        translate?.set(animateStart)
        onMove(curDx, curDy)
        if (percentTime < 1.0f) {
            post { onAnimateStep() }
        }
    }

    fun onMove(dx: Float, dy: Float) {
        translate?.postTranslate(dx, dy)
        invalidate()
    }

    fun onResetLocation() {
        translate?.reset()
        invalidate()
    }

    fun onSetLocation(dx: Float, dy: Float) {
        translate?.postTranslate(dx, dy)
    }

    fun init() {
        translate = Matrix(this.matrix)
        gestures = GestureDetector(context, GestureListener(this))
        picture = BitmapFactory.decodeStream(context.assets.open("test.jpeg"))
        overlay = noneOverlay
        val mode: PorterDuff.Mode = PorterDuff.Mode.SCREEN
        val colorFilter = PorterDuffColorFilter(Color.WHITE, mode)
        paint?.colorFilter = colorFilter
        //paint?.xfermode = PorterDuffXfermode(mode)
    }


    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(picture!!, testMatrix, null)
        canvas.apply {
            save()
            scale(mScaleFactor, mScaleFactor)
            canvas.drawBitmap(overlay!!, translate!!, paint)
            restore()
        }
    }

    fun getBitmap(): Bitmap? {
        this.isDrawingCacheEnabled = true
        this.buildDrawingCache()
        val bmp = Bitmap.createBitmap(this.drawingCache)
        this.isDrawingCacheEnabled = false
        return bmp
    }

    fun saveImage() {
        try {
            val toDisk: Bitmap? =
                getBitmap()
            val fileName =
                Environment.getExternalStorageDirectory().toString() + "/${UUID.randomUUID()}.png"
            val file = File(fileName)
            file.parentFile.mkdirs()
            file.createNewFile()
            val stream: OutputStream = FileOutputStream(file)
            toDisk?.compress(
                CompressFormat.PNG,
                80,
                stream
            )
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOverlay(overlay: Bitmap) {
        this.overlay = overlay
        onResetLocation()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestures!!.onTouchEvent(event)
        mScaleDetector!!.onTouchEvent(event)
        return true
    }

    fun clearOverlay() {
        this.overlay = noneOverlay
    }
}