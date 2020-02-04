package com.example.eatitclient.Model

class PopularCategoryModel {
    var food_id:String?=null
    var meny_id:String?=null
    var name:String?=null
    var image:String?= null

    constructor()
    constructor(food_id: String?, meny_id: String?, name: String?, image: String?) {
        this.food_id = food_id
        this.meny_id = meny_id
        this.name = name
        this.image = image
    }

}