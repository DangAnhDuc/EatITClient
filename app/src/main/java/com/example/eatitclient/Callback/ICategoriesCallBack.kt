package com.example.eatitclient.Callback

import com.example.eatitclient.Model.CategoryModel

interface ICategoriesCallBack {
    fun onCategoriesLoadSuccess(categoryModelList: List<CategoryModel>)
    fun onCategoriesLoadFailed(message: String)
}