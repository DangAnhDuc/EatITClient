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
import com.example.eatitclient.Common.Common
import com.example.eatitclient.Model.CommentModel
import com.example.eatitclient.Model.FoodModel
import com.example.eatitclient.R
import com.example.eatitclient.ui.comment.CommentFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog

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
    private var rdi_group_size: RadioGroup? = null

    private var waitingDialog: android.app.AlertDialog? = null

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
        foodDetailViewModel.getMutableLiveDataComment().observe(this, Observer {
            submitRatingToFirebase(it)
        })
        return root
    }

    private fun submitRatingToFirebase(it: CommentModel?) {
        waitingDialog!!.show()

        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id!!)
            .push()
            .setValue(it)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addRatingToFood(it!!.ratingValue.toDouble())
                }
                waitingDialog!!.dismiss()
            }
    }

    private fun addRatingToFood(ratingValue: Double) {
        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF)
            .child(Common.categorySelected!!.menu_id!!)
            .child("foods")
            .child(Common.foodSelected!!.key!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    waitingDialog!!.dismiss()
                    Toast.makeText(context!!, "" + p0.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val foodModel = p0.getValue(FoodModel::class.java)
                        foodModel!!.key = Common.foodSelected!!.key
                        val sumRating = foodModel.ratingValue!!.toDouble() + ratingValue
                        val ratingCount = foodModel.ratingCount + 1
                        val result = sumRating / ratingCount

                        val updateData = HashMap<String, Any>()
                        updateData["ratingValue"] = result
                        updateData["ratingCount"] = ratingCount

                        foodModel.ratingCount = ratingCount
                        foodModel.ratingValue = result

                        p0.ref
                            .updateChildren(updateData)
                            .addOnCompleteListener { task ->
                                waitingDialog!!.dismiss()
                                if (task.isSuccessful) {
                                    Common.foodSelected = foodModel
                                    foodDetailViewModel!!.setFoodModel(foodModel)
                                    Toast.makeText(context!!, "Thank you ", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                    } else
                        waitingDialog!!.dismiss()
                }

            })
    }

    private fun displayInfo(it: FoodModel?) {
        Glide.with(context!!).load(it!!.image).into(img_food!!)
        food_name!!.text = StringBuilder(it!!.name!!)
        food_price!!.text = StringBuilder(it!!.price!!.toString())
        food_description!!.text = StringBuilder(it!!.description!!)
        ratingBar!!.rating = it!!.ratingValue.toFloat()

        for (sizeModel in it!!.size) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compoundButton, b ->
                if (b)
                    Common.foodSelected.userSelectedSize = sizeModel
                calculateTotalPrice()
            }
            var params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price

            rdi_group_size!!.addView(radioButton)
        }

        if (rdi_group_size!!.clipChildren > 0) {
            val radioButton = rdi_group_size!!.getChildAt(0) as RadioButton
            radioButton.isChecked = true
        }
    }

    private fun calculateTotalPrice() {
        var totalPrice = Common.foodSelected!!.price.toDouble()
        var displayPrice = 0.0

        totalPrice += Common.foodSelected!!.userSelectedSize!!.price!!.toDouble()
        displayPrice = totalPrice + number_button.number.toInt()
        displayPrice = Math.round(displayPrice * 100.0 / 100.0)
    }

    private fun initView(root: View?) {

        waitingDialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()
        btnCart = root!!.findViewById(R.id.btnCart) as CounterFab
        btnRating = root!!.findViewById(R.id.btn_rating) as FloatingActionButton
        img_food = root!!.findViewById(R.id.img_food) as ImageView
        btnShowComment = root!!.findViewById(R.id.btnShowComment) as Button
        ratingBar = root!!.findViewById(R.id.ratingBar) as RatingBar
        number_button = root!!.findViewById(R.id.number_button) as ElegantNumberButton
        food_name = root!!.findViewById(R.id.food_name) as TextView
        food_price = root!!.findViewById(R.id.food_price) as TextView
        food_description = root!!.findViewById(R.id.food_description) as TextView
        rdi_group_size = root!!.findViewById(R.id.rdi_group_size) as RadioGroup

        btnRating!!.setOnClickListener {
            showDialogRating()
        }

        btnShowComment!!.setOnClickListener {
            val commentFragment = CommentFragment.getInstance()
            commentFragment.show(activity!!.supportFragmentManager, "CommentFragment")
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
            commentModel.name = Common.currentUser!!.name
            commentModel.uid = Common.currentUser!!.uid
            commentModel.comment = edt_comment.text.toString()
            commentModel.ratingValue = ratingBar.rating
            val serverTimeStamp = HashMap<String, Any>()
            serverTimeStamp["timeStamp"] = ServerValue.TIMESTAMP
            commentModel.commentTimeStamp = (serverTimeStamp)
            foodDetailViewModel!!.setCommentModel(commentModel)
        }
        val dialog = buider.create()
        dialog.show()
    }
}