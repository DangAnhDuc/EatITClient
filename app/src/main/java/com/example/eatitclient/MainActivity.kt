package com.example.eatitclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import dmax.dialog.SpotsDialog
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var dialog: android.app.AlertDialog
    private val compositeDisposable= CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        firebaseAuth= FirebaseAuth.getInstance()
        dialog= SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        listener= FirebaseAuth.AuthStateListener {firebaseAuth ->
            val user= firebaseAuth.currentUser
            if(user!=null){
                Toast.makeText(this@MainActivity, "Already Login",Toast.LENGTH_SHORT).show()
            }
            else{

            }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        if(listener!=null){
            firebaseAuth.removeAuthStateListener(listener)
        }
        compositeDisposable.clear()
        super.onStop()
    }
}
