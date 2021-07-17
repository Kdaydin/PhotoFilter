package com.kdaydin.photofilter.ui.filter

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.kdaydin.photofilter.R
import com.kdaydin.photofilter.data.entities.Overlay
import com.kdaydin.photofilter.databinding.ActivityFilterBinding
import com.kdaydin.photofilter.ui.adapter.OverlayListAdapter
import com.kdaydin.photofilter.ui.base.BaseActivity
import com.kdaydin.photofilter.ui.base.VMState
import com.kdaydin.photofilter.ui.listener.OverlaySelectionListener
import org.koin.android.ext.android.get


class FilterActivity : BaseActivity<FilterViewModel, ActivityFilterBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel?.getOverlays()
        viewModel?.overlayData?.observe(this) {
            if (it.isNullOrEmpty().not()) {
                binding?.rvOverlays?.apply {
                    val mAdapter = OverlayListAdapter(it!!, object : OverlaySelectionListener {
                        override fun onOverlaySelected(overlay: Overlay) {
                            Glide.with(context)
                                .asBitmap().load(overlay.overlayUrl).addListener(object :
                                    com.bumptech.glide.request.RequestListener<Bitmap> {
                                    override fun onResourceReady(
                                        overlayBtmp: Bitmap?,
                                        model: Any?,
                                        target: Target<Bitmap>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        this@FilterActivity.runOnUiThread {
                                            overlayBtmp?.let {
                                                this@FilterActivity.binding?.overlayView?.setOverlay(
                                                    overlayBtmp
                                                )
                                                this@FilterActivity.binding?.overlayView?.invalidate()
                                            }
                                        }
                                        return true
                                    }

                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Bitmap>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        this@FilterActivity.runOnUiThread {
                                            this@FilterActivity.binding?.overlayView?.clearOverlay()
                                            this@FilterActivity.binding?.overlayView?.invalidate()

                                        }
                                        return false
                                    }
                                }).submit()
                        }
                    })
                    adapter = mAdapter
                }
            }
        }
    }

    override fun getLayoutRes(): Int = R.layout.activity_filter

    override fun getViewModelType(): FilterViewModel = get()

    override fun onStateChanged(state: VMState?) {
        when (state) {
            is FilterVMState.SavePhoto -> {
                if (verifyStoragePermissions(this) == true)
                    binding?.overlayView?.saveImage()
            }
            is FilterVMState.CloseApp -> {
                finish()
            }
        }
    }

    // Storage Permissions
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    private fun verifyStoragePermissions(activity: Activity?): Boolean? {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        } else {
            return true
        }
        return null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                // Permission Granted
                binding?.overlayView?.saveImage()
            } else {
                // Permission Denied
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}