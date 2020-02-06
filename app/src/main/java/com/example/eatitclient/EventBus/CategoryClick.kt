package com.example.eatitclient.EventBus

import com.example.eatitclient.Model.CategoryModel

class CategoryClick(
    var isSuccess: Boolean,
    var category: CategoryModel
) {
}