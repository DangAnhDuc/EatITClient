package com.example.eatitclient.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.example.eatitclient.Database.CartDataSource
import com.example.eatitclient.Database.CartDatabase
import com.example.eatitclient.Database.CartItem
import com.example.eatitclient.Database.LocalCartDataSource
import com.example.eatitclient.EventBus.UpdateItemInCart
import com.example.eatitclient.R
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus

class MyCartAdapter(internal var context: Context, internal var cartItems: List<CartItem>) :
    RecyclerView.Adapter<MyCartAdapter.MyViewHolder>() {

    internal var compositeDisposable: CompositeDisposable
    internal var cartDataSource: CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var img_car: ImageView
        lateinit var txt_food_name: TextView
        lateinit var txt_food_price: TextView
        lateinit var number_button: ElegantNumberButton

        init {
            img_car = itemView.findViewById(R.id.img_cart) as ImageView
            txt_food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            txt_food_price = itemView.findViewById(R.id.txt_food_price) as TextView
            number_button = itemView.findViewById(R.id.number_count) as ElegantNumberButton

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context!!).inflate(
                R.layout.layout_cart_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(cartItems[position].foodImage).into(holder.img_car)
        holder.txt_food_name.text = StringBuilder(cartItems[position].foodName!!)
        holder.txt_food_price.text =
            StringBuilder("$").append(cartItems[position].foodPrice!! + cartItems[position].foodExtraPrice)
        holder.number_button.number = cartItems[position].foodQuantity.toString()

        holder.number_button.setOnValueChangeListener { view, oldValue, newValue ->
            cartItems[position].foodQuantity = newValue
            EventBus.getDefault().postSticky(UpdateItemInCart(cartItems[position]))
        }

    }

    fun getItemAtPosition(pos: Int): CartItem {
        return cartItems[pos]
    }
}