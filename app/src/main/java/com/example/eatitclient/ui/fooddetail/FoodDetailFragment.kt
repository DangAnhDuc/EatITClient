package com.example.eatitclient.ui.fooddetail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.example.eatitclient.Model.CommentModel
import com.example.eatitclient.Model.FoodModel
import com.example.eatitclient.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FoodDetailFragment : Fragment() {

    private lateinit var foodDetailViewModel: FoodDetailViewModel

    private var img_food: ImageView? = null
    private var btnCart: CounterFab? = null
    private var btnRating: FloatingActionButton? = null
    private var food_name: TextView? = null
    private var food_description: TextView? = null
    private var food_price: TextView? = null
    private var number_button: ElegantNumberButton? = null
    private var ratingBar: RatingBar? = null
    private var btnShowComment: Button? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodDetailViewModel =
            ViewModelProviders.of(this).get(FoodDetailViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_details, container, false)
        initView(root)
        foodDetailViewModel.getMutableLiveDataFood().observe(this, Observer {
            displayInfo(it)
        })
        return root
    }

    private fun displayInfo(it: FoodModel?) {
        Glide.with(context!!).load(it!!.image).into(img_food!!)
        food_name!!.text = StringBuilder(it!!.name!!)
        food_price!!.text = StringBuilder(it!!.price!!.toString())
        food_description!!.text = StringBuilder(it!!.description!!)

    }

    private fun initView(root: View?) {
        btnCart = root!!.findViewById(R.id.btnCart) as CounterFab
        btnRating = root!!.findViewById(R.id.btn_rating) as FloatingActionButton
        img_food = root!!.findViewById(R.id.img_food) as ImageView
        btnShowComment = root!!.findViewById(R.id.btnShowComment) as Button
        ratingBar = root!!.findViewById(R.id.ratingBar) as RatingBar
        number_button = root!!.findViewById(R.id.number_button) as ElegantNumberButton
        food_name = root!!.findViewById(R.id.food_name) as TextView
        food_price = root!!.findViewById(R.id.food_price) as TextView
        food_description = root!!.findViewById(R.id.food_description) as TextView


        btnRating!!.setOnClickListener {
            showDialogRating()
        }
    }

    private fun showDialogRating() {
        var buider = AlertDialog.Builder(context!!)
        buider.setTitle("Rating Food")
        buider.setMessage("Please fill information")

        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_rating_comment, null)

        val ratingBar = itemView.findViewById<RatingBar>(R.id.rating_bar)
        val edt_comment = itemView.findViewById<EditText>(R.id.edt_comment)

        buider.setView(itemView)
        buider.setNegativeButton("CANCEL") { dialogInterface, i -> dialogInterface.dismiss() }

        buider.setPositiveButton("OK") { dialogInterface, i ->
            val commentModel = CommentModel()
        }
        val dialog = buider.create()
        dialog.show()
    }
}