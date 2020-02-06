package com.example.eatitclient.Common

import com.example.eatitclient.Model.CategoryModel
import com.example.eatitclient.Model.FoodModel
import com.example.eatitclient.Model.UserModel

object Common {
    var foodSelected: FoodModel? = null
    var categorySelected: CategoryModel? = null
    val CATEGORY_REF = "Category"
    val FULL_WITDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    val BESTDEALS_REF = "BestDeals"
    val POPULAR_REF= "MostPopular"
    val USER_REFERENCE= "Users"
    var currentUser:UserModel?=null
}