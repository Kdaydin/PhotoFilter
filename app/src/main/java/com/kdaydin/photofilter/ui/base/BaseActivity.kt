package com.kdaydin.photofilter.ui.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.Observer

abstract class BaseActivity<VM : BaseViewModel, DB : ViewDataBinding> : AppCompatActivity() {

    @LayoutRes
    abstract fun getLayoutRes(): Int

    abstract fun getViewModelType(): VM

    protected var viewModel: VM? = null
    protected var binding: DB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayoutRes())
        viewModel = getViewModelType()
        binding?.setVariable(BR.viewModel, viewModel)
        binding?.lifecycleOwner = this
        viewModel.let { vm ->
            if (vm?.state?.hasObservers()?.not() == true) vm.state.observe(this, Observer {
                onStateChanged(vm.state.value)
            })
        }
        viewModel?.onCreate()
    }

    abstract fun onStateChanged(state: VMState?)

    override fun onResume() {
        super.onResume()
        viewModel?.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel?.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewModel?.onStop()
    }

    override fun onStart() {
        super.onStart()
        viewModel?.onStart()
    }

    override fun onDestroy() {
        binding?.unbind()
        binding?.lifecycleOwner = null
        binding = null
        super.onDestroy()
    }
}