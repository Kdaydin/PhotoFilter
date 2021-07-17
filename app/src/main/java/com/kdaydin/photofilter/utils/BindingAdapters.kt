package com.kdaydin.photofilter.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.kdaydin.photofilter.R


@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    Glide.with(view.context).load(url).error(R.drawable.bg_splash).into(view)
}