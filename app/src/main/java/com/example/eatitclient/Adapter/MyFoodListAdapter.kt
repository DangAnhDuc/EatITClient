package com.example.eatitclient.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eatitclient.Callback.IRecyclerItemClickListener
import com.example.eatitclient.Common.Common
import com.example.eatitclient.Database.CartDataSource
import com.example.eatitclient.Database.CartDatabase
import com.example.eatitclient.Database.CartItem
import com.example.eatitclient.Database.LocalCartDataSource
import com.example.eatitclient.EventBus.CountCartEvent
import com.example.eatitclient.EventBus.FoodItemClick
import com.example.eatitclient.Model.FoodModel
import com.example.eatitclient.R
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class MyFoodListAdapter(
    internal var context: Context,
    internal var foodList: List<FoodModel>
) : RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder>() {

    private val compositeDisposable: CompositeDisposable
    private val cartDataSource: CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var food_name: TextView? = null
        var food_price: TextView? = null

        var food_image: ImageView? = null
        var food_fav: ImageView? = null
        var food_cart: ImageView? = null


        internal var listener: IRecyclerItemClickListener? = null
        fun setListener(listener: IRecyclerItemClickListener) {
            this.listener = listener
        }

        init {
            food_image = itemView.findViewById(R.id.img_food_image) as ImageView
            food_fav = itemView.findViewById(R.id.img_fav) as ImageView
            food_cart = itemView.findViewById(R.id.img_quick_cart) as ImageView
            food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            food_price = itemView.findViewById(R.id.txt_food_price) as TextView

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener!!.onItemClick(v!!, adapterPosition)
        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.layout_food_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(foodList.get(position).image).into(holder.food_image!!)
        holder.food_name!!.setText(foodList.get(position).name)
        holder.food_price!!.setText(foodList.get(position).price.toString())

        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                Common.foodSelected = foodList.get(pos)
                Common.foodSelected!!.key = pos.toString()
                EventBus.getDefault().postSticky(FoodItemClick(true, foodList.get(pos)))
            }

        })

        holder.food_cart!!.setOnClickListener {
            val cartItem = CartItem()
            cartItem.uid = Common.currentUser!!.uid!!
            cartItem.userPhone = Common.currentUser!!.phone!!

            cartItem.foodId = foodList.get(position).id!!
            cartItem.foodName = foodList.get(position).name!!
            cartItem.foodImage = foodList.get(position).image!!
            cartItem.foodPrice = foodList.get(position).price!!.toDouble()
            cartItem.foodQuantity = 1
            cartItem.foodExtraPrice = 0.0
            cartItem.foodAddon = "Default"
            cartItem.foodSize = "Default"

            cartDataSource.getItemWithAllOptionsInCart(
                Common.currentUser!!.uid!!,
                cartItem.foodId!!, cartItem.foodSize!!, cartItem.foodAddon!!
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItem> {
                    override fun onSuccess(t: CartItem) {
                        if (t.equals(cartItem)) {
                            t.foodExtraPrice = cartItem.foodExtraPrice;
                            t.foodAddon = cartItem.foodAddon
                            t.foodSize = cartItem.foodSize
                            t.foodQuantity = t.foodQuantity + cartItem.foodQuantity
                            cartDataSource.updateCart(t)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<Int> {
                                    override fun onSuccess(t: Int) {
                                        Toast.makeText(
                                            context,
                                            " Update Cart Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }

                                    override fun onSubscribe(d: Disposable) {
                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(
                                            context,
                                            "[Update Cart]" + e.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                        } else {
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem).subscribeOn(
                                Schedulers.io()
                            )
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Toast.makeText(
                                        context,
                                        "Add to cart success",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    EventBus.getDefault().postSticky(CountCartEvent(true))
                                }, { t: Throwable ->
                                    Toast.makeText(
                                        context,
                                        "{INSERT CART}" + t!!.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                            )
                        }
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onError(e: Throwable) {
                        if (e.message!!.contains("empty")) {
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem).subscribeOn(
                                Schedulers.io()
                            )
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Toast.makeText(
                                        context,
                                        "Add to cart success",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    EventBus.getDefault().postSticky(CountCartEvent(true))
                                }, { t: Throwable ->
                                    Toast.makeText(
                                        context,
                                        "{INSERT CART}" + t!!.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                            )
                        } else
                            Toast.makeText(
                                context,
                                "[CART ERROR]" + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                    }

                })
        }
    }

    fun onStop() {
        if (compositeDisposable != null)
            compositeDisposable.clear()
    }


}