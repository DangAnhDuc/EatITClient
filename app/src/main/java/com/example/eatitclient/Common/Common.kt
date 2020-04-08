package com.example.eatitclient.Common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.eatitclient.Model.*
import com.example.eatitclient.R
import com.google.firebase.database.FirebaseDatabase
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

object Common {
    fun formatPrice(price: Double): String {
        if (price != 0.toDouble()) {
            val df = DecimalFormat("#,##0.00")
            df.roundingMode = RoundingMode.HALF_UP
            val finalPrice = StringBuilder(df.format(price)).toString()
            return finalPrice.replace(".", ",")
        } else {
            return "0,00"
        }
    }

    fun calculateExtraPrice(
        userSelectedSize: SizeModel?,
        userSelectedAddon: MutableList<AddonModel>?
    ): Double {
        var result: Double = 0.0
        if (userSelectedSize == null && userSelectedAddon == null) {
            return 0.0
        } else if (userSelectedSize == null) {
            for (addonModel in userSelectedAddon!!) {
                result += addonModel.price!!.toDouble()
            }
            return result

        } else if (userSelectedAddon == null) {
            result = userSelectedSize!!.price.toDouble()
            return result
        } else {
            result = userSelectedSize!!.price.toDouble()
            for (addonModel in userSelectedAddon!!) {
                result += addonModel.price!!.toDouble()
            }
            return result
        }
    }

    fun setSpanString(welcome: String, name: String?, txtUser: TextView?) {
        val buider = SpannableStringBuilder()
        buider.append(welcome)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 0, name!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        buider.append(txtSpannable)
        txtUser!!.setText(buider, TextView.BufferType.SPANNABLE)
    }

    fun createOrderNumber(): String {
        return java.lang.StringBuilder()
            .append(System.currentTimeMillis())
            .append(Math.abs(Random().nextInt()))
            .toString()
    }

    fun getDateOfWeek(i: Int): String {
        when (i) {
            i -> return "Monday"
            2 -> return "Tuesday"
            3 -> return "Wednesday"
            4 -> return "Thursday"
            5 -> return "Friday"
            6 -> return "Saturday"
            7 -> return "Sunday"
            else -> return "Unknown"
        }
    }

    fun convertStatusToText(orderStatus: Int): String {
        when (orderStatus) {
            0 -> return "Placed"
            1 -> return "Shipping"
            2 -> return "Shipped"
            -1 -> return "Cancelled"
            else -> return "Unknown"
        }
    }

    fun updateToken() {
        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REF)
            .child(currentUser!!.uid!!)
            .setValue(TokenModel(currentUser!!.uid!!, Common.SESSION_TOKEN))
            .addOnFailureListener { e ->
                Log.d("Token Error", e.message)
            }
    }

    fun showNotification(
        context: Context,
        id: Int,
        title: String?,
        content: String?,
        intent: Intent?
    ) {
        var pendingIntent: PendingIntent? = null
        if (intent != null)
            pendingIntent =
                PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val NOTIFICATION_CHANEL_ID = "com.example.eatitclient"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANEL_ID,
                "Eat IT v2", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = "Eat It v2"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.lightColor = (Color.RED)
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)

            notificationManager.createNotificationChannel(notificationChannel)
        }
        val buider = NotificationCompat.Builder(context, NOTIFICATION_CHANEL_ID)

        buider.setContentTitle(title!!).setContentText(content!!).setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_restaurant_menu_black_24dp
                )
            )

        if (pendingIntent != null)
            buider.setContentIntent(pendingIntent)
        val notification = buider.build()

        notificationManager.notify(id, notification)
    }

    fun getNewOrderTopic(): String {
        return java.lang.StringBuilder("/topics/new_order").toString()
    }


    val NOTI_TITLE = "title"
    val NOTI_CONTENT = "content"
    val TOKEN_REF = "Tokens"
    var currentToken: String = ""
    val ORDER_REF: String = "Order"
    var COMMENT_REF = "Comments"
    var foodSelected: FoodModel? = null
    var categorySelected: CategoryModel? = null
    val CATEGORY_REF = "Category"
    val FULL_WITDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    val BESTDEALS_REF = "BestDeals"
    val POPULAR_REF= "MostPopular"
    val USER_REFERENCE= "Users"
    var currentUser:UserModel?=null
    var SESSION_TOKEN: String = ""
}