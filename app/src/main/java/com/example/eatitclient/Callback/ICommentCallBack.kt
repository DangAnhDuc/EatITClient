package com.example.eatitclient.Callback

import com.example.eatitclient.Model.CommentModel

interface ICommentCallBack {
    fun onCommentsLoadSuccess(commentList: List<CommentModel>)
    fun onCommentsLoadFailed(message: String)
}