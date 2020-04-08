package com.example.eatitclient.Callback

import com.example.eatitclient.Model.Order

interface ILoadTimeFromFirebaseCallback {
    fun onLoadTimeSuccess(order: Order, estimatedTimeMs: Long)
    fun onLoadTimeFailed(message: String)
}