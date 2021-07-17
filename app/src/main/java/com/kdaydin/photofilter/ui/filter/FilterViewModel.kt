package com.kdaydin.photofilter.ui.filter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kdaydin.photofilter.data.entities.Overlay
import com.kdaydin.photofilter.data.remote.UseCaseResult
import com.kdaydin.photofilter.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class FilterViewModel : BaseViewModel() {
    val overlayData = MutableLiveData<List<Overlay>?>(null)
    fun getOverlays() {
        viewModelScope.launch {
            when (val result = lyrebirdRepository.getOverlays()) {
                is UseCaseResult.Success -> {
                    val list = result.data.plus(Overlay(0, "None")).sortedBy {
                        it.overlayId
                    }
                    overlayData.postValue(list)
                }
            }
        }
    }
}