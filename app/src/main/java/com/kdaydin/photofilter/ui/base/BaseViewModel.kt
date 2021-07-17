package com.kdaydin.photofilter.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kdaydin.photofilter.data.repository.LyrebirdRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.coroutines.CoroutineContext

open class BaseViewModel : ViewModel(), CoroutineScope, KoinComponent {

    val lyrebirdRepository: LyrebirdRepository = get()
    val state = MutableLiveData<VMState>()
    val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    open fun onStart() {}

    open fun onStop() {}

    open fun onPause() {}

    open fun onCreate() {}

    open fun onResume() {}

    open fun onCreateView() {}

    open fun onViewCreated() {}

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
