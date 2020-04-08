package com.example.eatitclient.Callback

import com.example.eatitclient.Model.Order

interface ILoadOrderCallbackListener {
    fun onLoadOrderSuccess(orderList: List<Order>)
    fun onLoadOerderFailed(message: String)
}