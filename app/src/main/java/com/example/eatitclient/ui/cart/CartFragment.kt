package com.example.eatitclient.ui.cart

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.text.TextUtils
import android.view.*
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.example.eatitclient.Adapter.MyCartAdapter
import com.example.eatitclient.Callback.ILoadTimeFromFirebaseCallback
import com.example.eatitclient.Callback.IMyButtonCallback
import com.example.eatitclient.Common.Common
import com.example.eatitclient.Common.MySwipeHelper
import com.example.eatitclient.Database.CartDataSource
import com.example.eatitclient.Database.CartDatabase
import com.example.eatitclient.Database.CartItem
import com.example.eatitclient.Database.LocalCartDataSource
import com.example.eatitclient.EventBus.CountCartEvent
import com.example.eatitclient.EventBus.HideFABCart
import com.example.eatitclient.EventBus.MenuItemback
import com.example.eatitclient.EventBus.UpdateItemInCart
import com.example.eatitclient.Model.FCMResponse
import com.example.eatitclient.Model.FCMSendData
import com.example.eatitclient.Model.Order
import com.example.eatitclient.R
import com.example.eatitclient.Remote.ICloudFunctions
import com.example.eatitclient.Remote.IFCMServices
import com.example.eatitclient.Remote.RetrofitCloudClient
import com.example.eatitclient.Remote.RetrofitFCMClient
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CartFragment : Fragment(), ILoadTimeFromFirebaseCallback {

    private val REQUEST_BRAINTREE_CODE: Int = 8888
    private lateinit var cartViewModel: CartViewModel
    private var cartDataSource: CartDataSource? = null
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var recyclerViewState: Parcelable? = null
    var txt_empty_cart: TextView? = null
    var txt_total_price: TextView? = null
    var group_place_holder: CardView? = null
    var recycler_cart: RecyclerView? = null
    var adapter: MyCartAdapter? = null
    private lateinit var btn_place_order: MaterialButton
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    lateinit var ifcmServices: IFCMServices
    internal var address: String = ""
    internal var comment: String = ""

    lateinit var cloudFunctions: ICloudFunctions
    lateinit var listener: ILoadTimeFromFirebaseCallback

    override fun onResume() {
        super.onResume()
        calculateTotalPrice()
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        EventBus.getDefault().postSticky((HideFABCart(true)))

        cartViewModel =
            ViewModelProviders.of(this).get(CartViewModel::class.java)
        cartViewModel.initCartdataSource(context!!)
        val root = inflater.inflate(R.layout.fragment_cart, container, false)
        initViews(root)
        initLocation()
        cartViewModel.getMutableLiveDataCartItem().observe(this, Observer {
            if (it == null || it.isEmpty()) {
                recycler_cart!!.visibility = View.GONE
                group_place_holder!!.visibility = View.GONE
                txt_empty_cart!!.visibility = View.VISIBLE
            } else {
                recycler_cart!!.visibility = View.VISIBLE
                group_place_holder!!.visibility = View.VISIBLE
                txt_empty_cart!!.visibility = View.GONE
                adapter = MyCartAdapter(context!!, it)
                recycler_cart!!.adapter = adapter
            }
        })
        return root
    }

    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallback()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                currentLocation = p0!!.lastLocation
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(5000)
        locationRequest.setFastestInterval(3000)
        locationRequest.setSmallestDisplacement(10f)
    }

    private fun initViews(root: View?) {

        setHasOptionsMenu(true)

        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        ifcmServices = RetrofitFCMClient.getInstance().create(IFCMServices::class.java)
        listener = this
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context!!).cartDAO())
        txt_empty_cart = root!!.findViewById(R.id.txt_empty_cart) as TextView
        txt_total_price = root!!.findViewById(R.id.txt_total_price) as TextView
        group_place_holder = root!!.findViewById(R.id.group_place_holder) as CardView
        recycler_cart = root!!.findViewById(R.id.recycler_cart) as RecyclerView
        recycler_cart!!.setHasFixedSize(true)
        var layoutManager = LinearLayoutManager(context)
        recycler_cart!!.layoutManager = layoutManager
        recycler_cart!!.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

        var swipe = object : MySwipeHelper(context!!, recycler_cart!!, 200) {
            override fun instaniateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(
                    MyButton(
                        context!!,
                        "Delete",
                        30,
                        0,
                        Color.parseColor("#FF3C30"),
                        object : IMyButtonCallback {
                            override fun onClick(pos: Int) {
                                val deleteItem = adapter!!.getItemAtPosition(pos)
                                cartDataSource!!.deleteCart(deleteItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : SingleObserver<Int> {
                                        override fun onSuccess(t: Int) {
                                            adapter!!.notifyItemRemoved(pos)
                                            sumCart()
                                            EventBus.getDefault().postSticky(CountCartEvent(true))
                                            Toast.makeText(
                                                context,
                                                "Delete item success",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }

                                        override fun onSubscribe(d: Disposable) {

                                        }

                                        override fun onError(e: Throwable) {
                                            Toast.makeText(
                                                context,
                                                "" + e.message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    })
                            }

                        })
                )
            }

        }
        btn_place_order = root!!.findViewById(R.id.btn_place_order) as MaterialButton
        btn_place_order.setOnClickListener {
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle("One more step")

            val view = LayoutInflater.from(context).inflate(R.layout.layout_place_order, null)
            val edt_address = view.findViewById<View>(R.id.edt_address) as EditText
            val edt_comment = view.findViewById<View>(R.id.edt_comment) as EditText
            val txt_address = view.findViewById<View>(R.id.txt_address_detail) as TextView
            val rdi_home = view.findViewById<RadioButton>(R.id.rdi_home_address) as RadioButton
            val rdi_other = view.findViewById<RadioButton>(R.id.rdi_other_address) as RadioButton
            val rdi_ship_to_this_address =
                view.findViewById<RadioButton>(R.id.rdi_ship_this_address) as RadioButton

            val rdi_cod = view.findViewById<RadioButton>(R.id.rdi_cod) as RadioButton
            val rdi_braintress = view.findViewById<RadioButton>(R.id.rdi_braintree) as RadioButton

            edt_address.setText(Common.currentUser!!.address!!)
            rdi_home.setOnCheckedChangeListener { compounButton, b ->
                if (b) {
                    edt_address.setText(Common.currentUser!!.address!!)
                    txt_address.visibility = View.GONE

                }
            }
            rdi_other.setOnCheckedChangeListener { compounButton, b ->
                if (b) {
                    edt_address.setText("")
                    txt_address.visibility = View.GONE

                }
            }
            rdi_ship_to_this_address.setOnCheckedChangeListener { compounButton, b ->
                if (b) {
                    fusedLocationProviderClient!!.lastLocation.addOnFailureListener { e ->
                        Toast.makeText(
                            context!!,
                            "" + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                        .addOnCompleteListener { task ->
                            val coordinates = java.lang.StringBuilder()
                                .append(task.result!!.latitude)
                                .append("/")
                                .append(task.result!!.longitude)
                                .toString()


                            val singleAddress = Single.just(
                                getAddressFromLatlng(
                                    task.result!!.latitude!!,
                                    task.result!!.longitude
                                )
                            )

                            val disposable = singleAddress.subscribeWith(object :
                                DisposableSingleObserver<String>() {
                                override fun onSuccess(t: String) {
                                    edt_address.setText(coordinates)
                                    txt_address.visibility = View.VISIBLE
                                    txt_address.setText(t)
                                }

                                override fun onError(e: Throwable) {
                                    edt_address.setText(coordinates)
                                    txt_address.visibility = View.VISIBLE
                                    txt_address.setText(e.message!!)
                                }

                            })


                        }
                }
            }

            builder.setView(view)
            builder.setNegativeButton("NO") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            builder.setPositiveButton("YES") { dialogInterface, _ ->
                if (rdi_cod.isChecked)
                    paymentCOD(edt_address.text.toString(), edt_comment.text.toString())
                else if (rdi_braintress.isChecked) {
                    address = edt_address.text.toString()
                    comment = edt_comment.text.toString()
                    if (!TextUtils.isEmpty(Common.currentToken)) {
                        val dropInRequest = DropInRequest().clientToken(Common.currentToken)
                        startActivityForResult(
                            dropInRequest.getIntent(context!!),
                            REQUEST_BRAINTREE_CODE
                        )
                    }
                }
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun paymentCOD(address: String, comment: String) {
        compositeDisposable.add(
            cartDataSource!!.getAllCart(Common.currentUser!!.uid!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ cartItemList ->
                    cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<Double> {
                            override fun onSuccess(totalPrice: Double) {
                                val finalPrice = totalPrice
                                val order = Order()
                                order.userId = Common.currentUser!!.uid!!
                                order.userName = Common.currentUser!!.name!!
                                order.userPhone = Common.currentUser!!.phone!!
                                order.shippingAddress = address
                                order.comment = comment

                                if (currentLocation != null) {
                                    order.lat = currentLocation!!.latitude
                                    order.lng = currentLocation!!.longitude
                                }

                                order.cartItemList = cartItemList
                                order.totalPayment = totalPrice
                                order.finalPayment = finalPrice
                                order.discount = 0
                                order.isCod = true
                                order.transactionId = " Cash on Delivery"

                                syncLocalTimeWithSever(order)
                            }

                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onError(e: Throwable) {
                                if (!e.message!!.contains("Query returned empty"))
                                    Toast.makeText(
                                        context,
                                        "[SUM CART]" + e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                        })

                }, { throwable ->
                    Toast.makeText(context, "" + throwable.message, Toast.LENGTH_SHORT).show()

                })
        )


    }

    private fun writeOrderToFireBase(order: Order) {
        FirebaseDatabase.getInstance()
            .getReference(Common.ORDER_REF)
            .child(Common.createOrderNumber())
            .setValue(order)
            .addOnFailureListener()
            { e ->
                Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()

            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cartDataSource!!.cleanCart(Common.currentUser!!.uid!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<Int> {
                            override fun onSuccess(t: Int) {
                                var dataSend = HashMap<String, String>()
                                dataSend.put(Common.NOTI_TITLE, "New Order")
                                dataSend.put(
                                    Common.NOTI_CONTENT,
                                    "You have new order" + Common.currentUser!!.phone
                                )
                                val sendData = FCMSendData(Common.getNewOrderTopic(), dataSend)
                                compositeDisposable.add(
                                    ifcmServices.sendNotification(sendData)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({ fcmResponse: FCMResponse ->
                                            if (fcmResponse!!.succes != 0) {
                                                Toast.makeText(
                                                    context,
                                                    "Order placed successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }, { throwable ->
                                            Toast.makeText(
                                                context,
                                                "Order was sent but notification failed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        })


                                )
                                EventBus.getDefault().postSticky(CountCartEvent(true))
                            }

                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onError(e: Throwable) {
                                Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                            }

                        })
                }
            }

    }

    private fun getAddressFromLatlng(latitude: Double, longitude: Double): String {
        val geoCoder = Geocoder(context!!, Locale.getDefault())
        var result: String? = null
        try {
            val addressList = geoCoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null && addressList.size > 0) {
                val address = addressList[0]
                val sb = java.lang.StringBuilder(address.getAddressLine(0))
                result = sb.toString()
            } else
                return "Address not found"
            return result
        } catch (e: IOException) {
            return e.message!!
        }
    }

    private fun sumCart() {
        cartDataSource!!.sumPrice(com.example.eatitclient.Common.Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSuccess(t: Double) {
                    txt_total_price!!.text = StringBuilder("Total: $")
                        .append(t)
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(context, "[SUM CART]" + e.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        cartViewModel!!.onStop()
        compositeDisposable.clear()
        EventBus.getDefault().postSticky(HideFABCart(false))
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateItemInCart(event: UpdateItemInCart) {
        if (event.cartItem != null) {
            recyclerViewState = recycler_cart!!.layoutManager!!.onSaveInstanceState()
            cartDataSource!!.updateCart(event.cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((object : SingleObserver<Int> {
                    override fun onSuccess(t: Int) {
                        calculateTotalPrice()
                        recycler_cart!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "[UPDATE CART]" + e.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                }))
        }
    }

    private fun calculateTotalPrice() {
        cartDataSource!!.sumPrice(com.example.eatitclient.Common.Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSuccess(price: Double) {
                    txt_total_price!!.text = StringBuilder("Total: $").append(
                        com.example.eatitclient.Common.Common.formatPrice(price)
                    )
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(context, "[SUM CART]" + e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater!!.inflate(R.menu.cart_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item!!.itemId == R.id.action_clear_cart) {
            cartDataSource!!.cleanCart(com.example.eatitclient.Common.Common.currentUser!!.uid!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSuccess(t: Int) {
                        Toast.makeText(context, "Clear Cart Success", Toast.LENGTH_SHORT).show()
                        EventBus.getDefault().postSticky(CountCartEvent(true))
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                    }

                })
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu!!.findItem(R.id.action_settings).setVisible(false)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BRAINTREE_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result =
                    data!!.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                val nonce = result!!.paymentMethodNonce

                cartDataSource!!.sumPrice(com.example.eatitclient.Common.Common.currentUser!!.uid!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<Double> {
                        override fun onSuccess(totalPrice: Double) {
                            cartDataSource!!.getAllCart(com.example.eatitclient.Common.Common.currentUser!!.uid!!)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ cartItems: List<CartItem>? ->
                                    compositeDisposable.add(
                                        (cloudFunctions.submitPayment(
                                            totalPrice,
                                            nonce!!.nonce
                                        ))
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({ braintreeTransaction ->
                                                if (braintreeTransaction.success) {
                                                    val finalPrice = totalPrice
                                                    val order = Order()
                                                    order.userId = Common.currentUser!!.uid!!
                                                    order.userName = Common.currentUser!!.name!!
                                                    order.userPhone = Common.currentUser!!.phone!!
                                                    order.shippingAddress = address
                                                    order.comment = comment

                                                    if (currentLocation != null) {
                                                        order.lat = currentLocation!!.latitude
                                                        order.lng = currentLocation!!.longitude
                                                    }

                                                    order.cartItemList = cartItems
                                                    order.totalPayment = totalPrice
                                                    order.finalPayment = finalPrice
                                                    order.discount = 0
                                                    order.isCod = false
                                                    order.transactionId =
                                                        braintreeTransaction.transaction!!.id

                                                    syncLocalTimeWithSever(order)
                                                }
                                            },
                                                { throwable ->
                                                    if (!throwable.message!!.contains("Query returned empty"))
                                                        Toast.makeText(
                                                            context,
                                                            "[SUM CART]" + throwable.message,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                })
                                    )
                                },
                                    { throwable ->
                                        Toast.makeText(
                                            context,
                                            "" + throwable.message,
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    })

                        }

                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onError(e: Throwable) {
                            Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                        }

                    })
            }
        }
    }

    private fun syncLocalTimeWithSever(order: Order) {
        val offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")
        offsetRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                listener.onLoadTimeFailed(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val offset = p0.getValue(Long::class.java)
                val estimatedServerTimeMs = System.currentTimeMillis() + offset!!
                val sdf = SimpleDateFormat("MM dd yyyy, HH:mm")
                val date = Date(estimatedServerTimeMs)

                listener.onLoadTimeSuccess(order, estimatedServerTimeMs)
            }

        }
        )
    }

    override fun onLoadTimeSuccess(order: Order, estimatedTimeMs: Long) {
        order.createDate = estimatedTimeMs
        order.orderStatus = 0
        writeOrderToFireBase(order)
    }

    override fun onLoadTimeFailed(message: String) {
        Toast.makeText(context!!, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemback())
        super.onDestroy()
    }
}