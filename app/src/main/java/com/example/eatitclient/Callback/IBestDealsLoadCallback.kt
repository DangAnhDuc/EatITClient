package com.example.eatitclient.Callback

import com.example.eatitclient.Model.BestDealModel

interface IBestDealsLoadCallback {
    fun onBestDealsLoadSuccess(bestDealsModelList: List<BestDealModel>)
    fun onBestDealsLoadFailed(message: String)
}