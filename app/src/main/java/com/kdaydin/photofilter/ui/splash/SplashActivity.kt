package com.kdaydin.photofilter.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kdaydin.photofilter.ui.filter.FilterActivity

class SplashActivity : AppCompatActivity() {
    val viewModel by lazy<SplashViewModel> {
        SplashViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, FilterActivity::class.java))
        finish()
    }

}