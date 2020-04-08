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
import com.example.eatitclient.EventBus.CategoryClick
import com.example.eatitclient.Model.CategoryModel
import com.example.eatitclient.R
import org.greenrobot.eventbus.EventBus

class MyCategoriesAdapter(
    internal var context: Context,
    internal var categoriesList: List<CategoryModel>
) : RecyclerView.Adapter<MyCategoriesAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var category_name: TextView? = null
        var category_image: ImageView? = null

        internal var listener: IRecyclerItemClickListener? = null
        fun setListener(listener: IRecyclerItemClickListener) {
            this.listener = listener
        }
        init {
            category_image = itemView.findViewById(R.id.img_category) as ImageView
            category_name = itemView.findViewById(R.id.tv_category_name) as TextView
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener!!.onItemClick(v!!, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.layout_category_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(categoriesList.get(position).image).into(holder.category_image!!)
        holder.category_name!!.setText(categoriesList.get(position).name)

        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                Common.categorySelected = categoriesList[pos]

                EventBus.getDefault().postSticky(CategoryClick(true, categoriesList.get(position)))
            }

        })
    }

    override fun getItemViewType(position: Int): Int {
        return if (categoriesList.size == 1) {
            Common.DEFAULT_COLUMN_COUNT
        } else {
            if (categoriesList.size % 2 == 0)
                Common.DEFAULT_COLUMN_COUNT
            else
                if (position > 1 && position == categoriesList.size - 1)
                    Common.FULL_WITDTH_COLUMN
                else
                    Common.DEFAULT_COLUMN_COUNT
        }
    }

    fun getCategoryList(): List<CategoryModel> {
        return categoriesList
    }
}