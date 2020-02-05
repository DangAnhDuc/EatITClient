package com.example.eatitclient.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eatitclient.Callback.IBestDealsLoadCallback
import com.example.eatitclient.Callback.IPopularLoadCallback
import com.example.eatitclient.Common.Common
import com.example.eatitclient.Model.BestDealModel
import com.example.eatitclient.Model.PopularCategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel(), IPopularLoadCallback, IBestDealsLoadCallback {
    private var popularListMutableLiveData: MutableLiveData<List<PopularCategoryModel>>? = null
    private var bestDealsListMutableLiveData: MutableLiveData<List<BestDealModel>>? = null

    private lateinit var messageError:MutableLiveData<String>
    private lateinit var popularLoadCallback: IPopularLoadCallback

    private lateinit var bestDealsLoadCallback: IBestDealsLoadCallback

    val bestDealList: LiveData<List<BestDealModel>>
        get() {
            if (bestDealsListMutableLiveData == null) {
                bestDealsListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadBestDealsList()
            }
            return bestDealsListMutableLiveData!!
        }

    private fun loadBestDealsList() {
        val tempList = ArrayList<BestDealModel>()
        val bestDealsRef = FirebaseDatabase.getInstance().getReference(Common.BESTDEALS_REF)
        bestDealsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                bestDealsLoadCallback.onBestDealsLoadFailed(p0.message!!)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapShot in p0!!.children) {
                    var model = itemSnapShot.getValue<BestDealModel>(BestDealModel::class.java)
                    tempList.add(model!!)
                }
                bestDealsLoadCallback.onBestDealsLoadSuccess(tempList)
            }

        })
    }

    val popularList:LiveData<List<PopularCategoryModel>>
    get(){
        if(popularListMutableLiveData==null){
            popularListMutableLiveData= MutableLiveData()
            messageError= MutableLiveData()
            loadPopularList()
        }
        return popularListMutableLiveData!!
    }

    private fun loadPopularList() {
        val tempList= ArrayList<PopularCategoryModel>()
        val popularRef= FirebaseDatabase.getInstance().getReference(Common.POPULAR_REF)
        popularRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                popularLoadCallback.onPopularLoadFailed(p0.message!!)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for(itemSnapShot in p0!!.children){
                    var model= itemSnapShot.getValue<PopularCategoryModel>(PopularCategoryModel::class.java)
                    tempList.add(model!!)
                }
                popularLoadCallback.onPopularLoadSuccess(tempList)
            }

        })
    }

    init {
         popularLoadCallback= this
        bestDealsLoadCallback = this
    }

    override fun onPopularLoadSuccess(popularModelList: List<PopularCategoryModel>) {
        popularListMutableLiveData!!.value = popularModelList
    }

    override fun onPopularLoadFailed(message: String) {
        messageError.value=message
    }

    override fun onBestDealsLoadSuccess(bestDealsModelList: List<BestDealModel>) {
        bestDealsListMutableLiveData!!.value = bestDealsModelList
    }

    override fun onBestDealsLoadFailed(message: String) {
        messageError.value = message
    }
}