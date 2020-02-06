package com.example.eatitclient.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eatitclient.Callback.IRecyclerItemClickListener
import com.example.eatitclient.Common.Common
import com.example.eatitclient.EventBus.FoodItemClick
import com.example.eatitclient.Model.FoodModel
import com.example.eatitclient.R
import org.greenrobot.eventbus.EventBus

class MyFoodListAdapter(
    internal var context: Context,
    internal var foodList: List<FoodModel>
) : RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder>() {
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
                EventBus.getDefault().postSticky(FoodItemClick(true, foodList.get(pos)))
            }

        })
    }

}