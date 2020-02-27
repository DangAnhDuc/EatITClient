package com.example.eatitclient.ui.comment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eatitclient.Adapter.MyCommentAdapter
import com.example.eatitclient.Callback.ICommentCallBack
import com.example.eatitclient.Common.Common
import com.example.eatitclient.EventBus.MenuItemback
import com.example.eatitclient.Model.CommentModel
import com.example.eatitclient.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus

class CommentFragment : BottomSheetDialogFragment(), ICommentCallBack {

    private var commentViewModel: CommentViewModel? = null
    private var listener: ICommentCallBack
    private var recyclerView: RecyclerView? = null
    private var dialog: AlertDialog? = null

    init {
        listener = this
    }

    companion object {
        private var instance: CommentFragment? = null

        fun getInstance(): CommentFragment {
            if (instance == null) {
                instance = CommentFragment()
            }
            return instance!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_comment_fragement, container, false)
        initViews(itemView)
        loadCommentFromFirebase()
        commentViewModel!!.mutableLiveDataCommentList.observe(this, Observer { commentList ->
            val adapter = MyCommentAdapter(context!!, commentList)
            recyclerView!!.adapter = adapter
        })
        return itemView
    }

    private fun loadCommentFromFirebase() {
        dialog!!.show()
        val commentModels = ArrayList<CommentModel>()
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id!!)
            .orderByChild("commentTimeStamp")
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    listener.onCommentsLoadFailed(p0.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (commentSnapShot in p0.children) {
                        val commentModel =
                            commentSnapShot.getValue<CommentModel>(CommentModel::class.java)
                        commentModels.add(commentModel!!)
                    }
                    listener.onCommentsLoadSuccess(commentModels)
                }

            })

    }

    private fun initViews(itemView: View?) {
        commentViewModel = ViewModelProviders.of(this).get(CommentViewModel::class.java)
        dialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()
        recyclerView = itemView!!.findViewById(R.id.recycler_comment) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.addItemDecoration(
            DividerItemDecoration(
                context!!,
                layoutManager.orientation
            )
        )
    }

    override fun onCommentsLoadSuccess(commentList: List<CommentModel>) {
        dialog!!.dismiss()
        commentViewModel!!.setCommentList(commentList)
    }

    override fun onCommentsLoadFailed(message: String) {
        Toast.makeText(context!!, "" + message, Toast.LENGTH_SHORT).show()
        dialog!!.dismiss()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemback())
        super.onDestroy()
    }

}