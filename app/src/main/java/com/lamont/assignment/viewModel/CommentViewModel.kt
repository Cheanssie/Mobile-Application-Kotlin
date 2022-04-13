package com.lamont.assignment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lamont.assignment.model.Comment
import com.lamont.assignment.repository.CommentRepository

class CommentViewModel {
    companion object {
        private var commentRepo: CommentRepository = CommentRepository()

        fun addComment(comment: Comment) {
            commentRepo.addComment(comment)
        }

    }

    lateinit var commentList : MutableLiveData<MutableList<Comment>>

    fun loadCommentList() : LiveData<MutableList<Comment>> {
        commentList = commentRepo.loadCommentList()
        return commentList
    }
}