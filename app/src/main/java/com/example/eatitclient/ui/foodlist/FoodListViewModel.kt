package com.example.eatitclient.ui.foodlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eatitclient.Common.Common
import com.example.eatitclient.Model.FoodModel

class FoodListViewModel : ViewModel() {

    private var mutableFoodModelList: MutableLiveData<List<FoodModel>>? = null

    fun getMutableFoodModelList(): MutableLiveData<List<FoodModel>> {
        if (mutableFoodModelList == null)
            mutableFoodModelList = MutableLiveData()
        mutableFoodModelList!!.value = Common.categorySelected!!.foods
        return mutableFoodModelList!!
    }
}