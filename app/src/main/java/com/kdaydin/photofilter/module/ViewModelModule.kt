package com.kdaydin.photofilter.module

import com.kdaydin.photofilter.ui.base.BaseViewModel
import com.kdaydin.photofilter.ui.filter.FilterViewModel
import com.kdaydin.photofilter.ui.splash.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { BaseViewModel() }
    viewModel { FilterViewModel() }
    viewModel { SplashViewModel() }
}