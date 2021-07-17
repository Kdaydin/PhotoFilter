package com.kdaydin.photofilter.data.remote

import com.kdaydin.photofilter.data.entities.Overlay
import kotlinx.coroutines.Deferred
import retrofit2.http.GET

interface LyrebirdApi {
    @GET("candidates/overlay.json")
    fun getOverlays(): Deferred<List<Overlay>>
}