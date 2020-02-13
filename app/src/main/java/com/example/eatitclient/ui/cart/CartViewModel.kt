package com.example.eatitclient.ui.cart

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eatitclient.Common.Common
import com.example.eatitclient.Database.CartDataSource
import com.example.eatitclient.Database.CartDatabase
import com.example.eatitclient.Database.CartItem
import com.example.eatitclient.Database.LocalCartDataSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CartViewModel : ViewModel() {

    private val compositeDisposable: CompositeDisposable
    private lateinit var cartDataSource: CartDataSource
    private var mutableLiveDataCartItem: MutableLiveData<List<CartItem>>? = null

    init {
        compositeDisposable = CompositeDisposable()
    }

    fun initCartdataSource(context: Context) {
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }

    fun getMutableLiveDataCartItem(): MutableLiveData<List<CartItem>> {
        if (mutableLiveDataCartItem == null)
            mutableLiveDataCartItem = MutableLiveData()
        getCartItems()
        return mutableLiveDataCartItem!!
    }

    private fun getCartItems() {
        compositeDisposable.addAll(cartDataSource!!.getAllCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ cartItems ->
                mutableLiveDataCartItem!!.value = cartItems
            }, { t: Throwable ->
                mutableLiveDataCartItem!!.value = null
            })
        )
    }

    fun onStop() {
        compositeDisposable.clear()
    }
}