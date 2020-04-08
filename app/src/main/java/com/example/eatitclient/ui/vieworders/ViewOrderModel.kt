package com.example.eatitclient.ui.vieworders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eatitclient.Model.Order

class ViewOrderModel : ViewModel() {

    val mutableLiveDataOrderList: MutableLiveData<List<Order>>

    init {
        mutableLiveDataOrderList = MutableLiveData()
    }

    fun setMutableLiveDataOrderList(orderList: List<Order>) {
        mutableLiveDataOrderList.value = orderList
    }
}