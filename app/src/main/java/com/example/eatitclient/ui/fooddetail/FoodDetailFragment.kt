package com.example.eatitclient.ui.fooddetail

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.example.eatitclient.Common.Common
import com.example.eatitclient.Database.CartDataSource
import com.example.eatitclient.Database.CartDatabase
import com.example.eatitclient.Database.CartItem
import com.example.eatitclient.Database.LocalCartDataSource
import com.example.eatitclient.EventBus.CountCartEvent
import com.example.eatitclient.EventBus.MenuItemback
import com.example.eatitclient.Model.CommentModel
import com.example.eatitclient.Model.FoodModel
import com.example.eatitclient.R
import com.example.eatitclient.ui.comment.CommentFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class FoodDetailFragment : Fragment(), TextWatcher {


    private lateinit var foodDetailViewModel: FoodDetailViewModel
    private lateinit var addonBottomSheetDialog: BottomSheetDialog


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
    private var img_add_on: ImageView? = null
    private var chip_group_selected_addon: ChipGroup? = null
    private var waitingDialog: android.app.AlertDialog? = null

    private var chip_group_addon: ChipGroup? = null
    private var edt_search_addon: EditText? = null

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var cartDataSource: CartDataSource


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

                        val updateData = HashMap<String, Any>()
                        updateData["ratingValue"] = sumRating
                        updateData["ratingCount"] = ratingCount

                        foodModel.ratingCount = ratingCount
                        foodModel.ratingValue = sumRating

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
        ratingBar!!.rating = it!!.ratingValue.toFloat() / it!!.ratingCount

        for (sizeModel in it!!.size) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compoundButton, b ->
                if (b)
                    Common.foodSelected!!.userSelectedSize = sizeModel
                calculateTotalPrice()
            }
            var params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price

            rdi_group_size!!.addView(radioButton)
        }

        if (rdi_group_size!!.childCount > 0) {
            val radioButton = rdi_group_size!!.getChildAt(0) as RadioButton
            radioButton.isChecked = true
        }
    }

    private fun calculateTotalPrice() {
        var totalPrice = Common.foodSelected!!.price.toDouble()
        var displayPrice = 0.0

        //addon
        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {
            for (addonModel in Common.foodSelected!!.userSelectedAddon!!) {
                totalPrice += addonModel!!.price.toDouble()
            }
        }

        //size
        totalPrice += Common.foodSelected!!.userSelectedSize!!.price!!.toDouble()
        displayPrice = totalPrice + number_button!!.number.toInt()
        displayPrice = Math.round(displayPrice * 100.0) / 100.0

        food_price!!.text =
            java.lang.StringBuilder().append(Common.formatPrice(displayPrice)).toString()
    }

    private fun initView(root: View?) {
        (activity as AppCompatActivity).supportActionBar!!.setTitle(Common.foodSelected!!.name)

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context!!).cartDAO())
        addonBottomSheetDialog = BottomSheetDialog(context!!, R.style.DialogStyle)
        val layout_user_selected_addon = layoutInflater.inflate(R.layout.layout_addon_display, null)
        chip_group_addon =
            layout_user_selected_addon.findViewById(R.id.chip_group_addon) as ChipGroup
        edt_search_addon = layout_user_selected_addon.findViewById(R.id.edt_search) as EditText
        addonBottomSheetDialog.setContentView(layout_user_selected_addon)
        addonBottomSheetDialog.setOnDismissListener {
            displayUserSelectedAddon()
            calculateTotalPrice()
        }


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
        img_add_on = root!!.findViewById(R.id.img_add_on) as ImageView
        chip_group_selected_addon =
            root!!.findViewById(R.id.chip_group_user_selected_addon) as ChipGroup

        img_add_on!!.setOnClickListener {
            if (Common.foodSelected!!.addon != null) {
                displayAllAddon()
                addonBottomSheetDialog.show()
            }
        }

        btnRating!!.setOnClickListener {
            showDialogRating()
        }

        btnShowComment!!.setOnClickListener {
            val commentFragment = CommentFragment.getInstance()
            commentFragment.show(activity!!.supportFragmentManager, "CommentFragment")
        }

        btnCart!!.setOnClickListener {
            val cartItem = CartItem()
            cartItem.uid = Common.currentUser!!.uid!!
            cartItem.userPhone = Common.currentUser!!.phone!!

            cartItem.foodId = Common.foodSelected!!.id!!
            cartItem.foodName = Common.foodSelected!!.name!!
            cartItem.foodImage = Common.foodSelected!!.image!!
            cartItem.foodPrice = Common.foodSelected!!.price!!.toDouble()
            cartItem.foodQuantity = number_button!!.number.toInt()
            cartItem.foodExtraPrice = Common.calculateExtraPrice(
                Common.foodSelected!!.userSelectedSize,
                Common.foodSelected!!.userSelectedAddon
            )
            if (Common.foodSelected!!.userSelectedAddon != null) {
                cartItem.foodAddon = Gson().toJson(Common.foodSelected!!.userSelectedAddon)
            } else
                cartItem.foodAddon = "Default"
            if (Common.foodSelected!!.userSelectedSize != null) {
                cartItem.foodSize = Gson().toJson(Common.foodSelected!!.userSelectedSize)
            } else
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

    private fun displayAllAddon() {
        if (Common.foodSelected!!.addon!!.size > 0) {
            chip_group_addon!!.clearCheck()
            chip_group_addon!!.removeAllViews()
            edt_search_addon!!.addTextChangedListener(this)

            for (addonModel in Common.foodSelected!!.addon!!) {
                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text = java.lang.StringBuilder(addonModel!!.name!!).append("(+$")
                    .append(addonModel.price).append(")").toString()
                chip.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        if (Common.foodSelected!!.userSelectedAddon == null)
                            Common.foodSelected!!.userSelectedAddon = ArrayList()
                        Common.foodSelected!!.userSelectedAddon!!.add(addonModel)
                    }
                }
                chip_group_addon!!.addView(chip)

            }
        }
    }

    private fun displayUserSelectedAddon() {
        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {
            chip_group_selected_addon!!.removeAllViews()
            for (addonModel in Common.foodSelected!!.userSelectedAddon!!) {
                val chip =
                    layoutInflater.inflate(R.layout.layout_chip_with_delete, null, false) as Chip
                chip.text = java.lang.StringBuilder(addonModel!!.name!!).append("(+$")
                    .append(addonModel.price).append(")").toString()
                chip.isCheckable = false
                chip.setOnCloseIconClickListener() {
                    chip_group_selected_addon!!.removeView(view)
                    Common.foodSelected!!.userSelectedAddon!!.remove(addonModel)
                    displayUserSelectedAddon()
                    calculateTotalPrice()
                }
                chip_group_selected_addon!!.addView(chip)
            }
        } else {
            chip_group_selected_addon!!.removeAllViews()
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

    override fun afterTextChanged(s: Editable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        chip_group_addon!!.clearCheck()
        chip_group_addon!!.removeAllViews()
        for (addonModel in Common.foodSelected!!.addon!!) {
            if (addonModel.name!!.toLowerCase().contains(s.toString().toLowerCase())) {
                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text = java.lang.StringBuilder(addonModel!!.name!!).append("(+$")
                    .append(addonModel.price).append(")").toString()
                chip.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        if (Common.foodSelected!!.userSelectedAddon == null)
                            Common.foodSelected!!.userSelectedAddon = ArrayList()
                        Common.foodSelected!!.userSelectedAddon!!.add(addonModel)
                    }
                }
                chip_group_addon!!.addView(chip)
            }
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemback())
        super.onDestroy()
    }
}