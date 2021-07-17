package com.kdaydin.photofilter.ui.listener

import com.kdaydin.photofilter.data.entities.Overlay


interface OverlaySelectionListener {
    fun onOverlaySelected(overlay: Overlay)
}