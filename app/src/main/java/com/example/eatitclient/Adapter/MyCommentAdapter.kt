package com.example.eatitclient.Adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eatitclient.Model.CommentModel
import com.example.eatitclient.R

class MyCommentAdapter(
    internal var context: Context,
    internal var commentList: List<CommentModel>
) : RecyclerView.Adapter<MyCommentAdapter.MyViewHolder>() {
    class MyViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var txt_comment_name: TextView? = null
        var txt_comment_date: TextView? = null
        var txt_comment: TextView? = null
        var comment_rating: RatingBar? = null

        init {
            txt_comment_name = itemview.findViewById(R.id.txt_comment_name) as TextView
            txt_comment_date = itemview.findViewById(R.id.txt_comment_date) as TextView
            txt_comment = itemview.findViewById(R.id.txt_comment) as TextView
            comment_rating = itemview.findViewById(R.id.comment_rating) as RatingBar


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context!!).inflate(
                R.layout.layout_comment_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var timeStamp =
            commentList.get(position).commentTimeStamp!!["timeStamp"]!!.toString().toLong()
        holder.txt_comment_date!!.text = DateUtils.getRelativeTimeSpanString(timeStamp)
        holder.txt_comment_name!!.text = commentList.get(position).name
        holder.txt_comment!!.text = commentList.get(position).comment
        holder.comment_rating!!.rating = commentList.get(position).ratingValue
    }

}