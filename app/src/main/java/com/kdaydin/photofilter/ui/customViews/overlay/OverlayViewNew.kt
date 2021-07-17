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
import androidx.core.content.res.ResourcesCompat
import com.kdaydin.photofilter.R
import java.io.*
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*


class OverlayViewNew : View {

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.light_blue, null)


    private var gestures: GestureDetector? = null
    private var translate: Matrix? = null
    private var picture: Bitmap
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

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(picture.width, picture.height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
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

    init {
        translate = Matrix(this.matrix)
        gestures = GestureDetector(context, GestureListenerNew(this))
        picture = BitmapFactory.decodeStream(context.assets.open("test.jpeg"))
        overlay = noneOverlay
        /*val mode: PorterDuff.Mode = PorterDuff.Mode.SCREEN
        val colorFilter = PorterDuffColorFilter(Color.WHITE, mode)
        paint?.colorFilter = colorFilter
        //paint?.xfermode = PorterDuffXfermode(mode)*/
    }

    fun onSetLocation(dx: Float, dy: Float) {
        translate?.postTranslate(dx, dy)
    }


    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(extraBitmap, testMatrix, null)
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
/*
    *//**
     * Converts a immutable bitmap to a mutable bitmap. This operation doesn't allocates
     * more memory that there is already allocated.
     *
     * @param imgIn - Source image. It will be released, and should not be used more
     * @return a copy of imgIn, but muttable.
     *//*
    fun convertToMutable(imgIn: Bitmap): Bitmap {
        var imgIn = imgIn
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            val file = File(
                Environment.getExternalStorageDirectory().toString() + File.separator + "temp.tmp"
            )

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            val randomAccessFile = RandomAccessFile(file, "rw")

            // get the width and height of the source bitmap.
            val width = imgIn.width
            val height = imgIn.height
            val type: Bitmap.Config = imgIn.config

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            val channel: FileChannel = randomAccessFile.getChannel()
            val map: MappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, (imgIn.rowBytes * height).toLong())
            imgIn.copyPixelsToBuffer(map)
            //recycle the source bitmap, this will be no longer used.
            imgIn.recycle()
            System.gc() // try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            imgIn = Bitmap.createBitmap(width, height, type)
            map.position(0)
            //load it back from temporary
            imgIn.copyPixelsFromBuffer(map)
            //close the temporary file and channel , then delete that also
            channel.close()
            randomAccessFile.close()

            // delete the temp file
            file.delete()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return imgIn
    }*/
}