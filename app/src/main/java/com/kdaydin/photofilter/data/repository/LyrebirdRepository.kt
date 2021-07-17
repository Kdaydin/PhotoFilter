package com.kdaydin.photofilter.data.repository

import com.kdaydin.photofilter.data.entities.Overlay
import com.kdaydin.photofilter.data.remote.UseCaseResult

interface LyrebirdRepository {
    suspend fun getOverlays(): UseCaseResult<List<Overlay>>

}