package com.example.eatitclient.ui.fooddetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eatitclient.Common.Common
import com.example.eatitclient.Model.FoodModel

class FoodDetailViewModel : ViewModel() {

    private var mutableLiveDataFood: MutableLiveData<FoodModel>? = null
    fun getMutableLiveDataFood(): MutableLiveData<FoodModel> {
        if (mutableLiveDataFood == null) {
            mutableLiveDataFood = MutableLiveData()
        }
        mutableLiveDataFood!!.value = Common.foodSelected
        return mutableLiveDataFood!!
    }
}