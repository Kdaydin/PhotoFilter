package com.kdaydin.photofilter.ui.customViews.overlay

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.createBitmap
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import com.kdaydin.photofilter.utils.CapturePhotoUtils
import java.util.*


class OverlayView : View {
    private lateinit var center: Point
    private var overlayMatrix: Matrix = Matrix()
    private var picture: Bitmap
    private var overlay: Bitmap? = null
    private var paint: Paint? = Paint()
    private var mainMatrix = Matrix()
    private var mOverlayScale = 1f
    private val kMinOverlayScale = 0.1f
    private val kMaxOverlayScale = 5f
    private var noneOverlay = createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = 1 - detector.scaleFactor

            mOverlayScale += scale

            if (mOverlayScale < kMinOverlayScale) mOverlayScale = kMinOverlayScale
            if (mOverlayScale > kMaxOverlayScale) mOverlayScale = kMaxOverlayScale

            overlayMatrix.postScale(
                1f / mOverlayScale,
                1f / mOverlayScale,
                center.x.toFloat(),
                center.y.toFloat()
            )
            mOverlayScale = 1f
            invalidate()
            return true
        }
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            onMove(-distanceX, -distanceY)
            return true
        }
    }

    private val mScaleDetector = ScaleGestureDetector(context, scaleListener)
    private val mGestureDetector = GestureDetector(context, gestureListener)


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val desiredWidth = 100
        val desiredHeight = picture.height * widthSize / picture.width

        //Measure Width
        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                //Must be this size
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                //Can't be bigger than...
                desiredWidth.coerceAtMost(widthSize)
            }
            else -> {
                //Be whatever you want
                desiredWidth
            }
        }

        //Measure Height
        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                //Must be this size
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                //Can't be bigger than...
                desiredHeight.coerceAtMost(heightSize)
            }
            else -> {
                //Be whatever you want
                desiredHeight
            }
        }
        setMeasuredDimension(width, height)
    }


    fun onMove(dx: Float, dy: Float) {
        overlayMatrix.postTranslate(dx, dy)
        invalidate()
    }

    private fun onResetLocation() {
        overlayMatrix.setTranslate(
            (center.x - (overlay?.width ?: 0) / 2).toFloat(),
            (center.y - (overlay?.height ?: 0) / 2).toFloat()
        )
        mOverlayScale = 1f
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val temp = picture
        val bw = temp.width
        val bh = temp.height
        val scale: Float = Math.min(1f * w / bw, 1f * h / bh)
        picture = scaleBitmap(temp, scale)
        // compute init left, top
        center = Point(w / 2, h / 2)
    }

    private fun scaleBitmap(origin: Bitmap?, scale: Float): Bitmap {
        if (origin == null) {
            return createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
        val height = origin.height
        val width = origin.width
        val matrix = Matrix()
        matrix.postScale(scale, scale) // Use after multiplication
        val newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
        if (!origin.isRecycled) {
            origin.recycle()
        }
        return newBM
    }

    init {
        picture = BitmapFactory.decodeStream(context.assets.open("test.jpeg"))
        overlay = noneOverlay
        val mode: PorterDuff.Mode = PorterDuff.Mode.SCREEN
        paint?.xfermode = PorterDuffXfermode(mode)
    }


    override fun onDraw(canvas: Canvas) {
        canvas.apply {
            save()
            canvas.drawBitmap(picture, mainMatrix, null)
            canvas.drawBitmap(overlay!!, overlayMatrix, paint)
            restore()
        }
    }

    private fun getBitmap(): Bitmap? {
        this.isDrawingCacheEnabled = true
        this.buildDrawingCache()
        val bmp = Bitmap.createBitmap(this.drawingCache)
        this.isDrawingCacheEnabled = false
        return bmp
    }

    fun saveImage() {
        try {
            val toDisk: Bitmap? = getBitmap()
            CapturePhotoUtils.insertImage(
                context.contentResolver,
                toDisk,
                UUID.randomUUID().toString(),
                "created by PhotoFilter App"
            )
            Toast.makeText(context, "Photo Saved to Gallery", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOverlay(overlay: Bitmap) {
        this.overlay = overlay
        onResetLocation()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mGestureDetector.onTouchEvent(event)
        mScaleDetector.onTouchEvent(event)
        return true

    }

    fun clearOverlay() {
        this.overlay = noneOverlay
    }
}