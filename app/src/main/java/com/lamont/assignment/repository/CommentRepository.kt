package com.lamont.assignment.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.model.Comment

class CommentRepository {
    var db : FirebaseFirestore = FirebaseFirestore.getInstance()
    var _commentList : MutableLiveData<MutableList<Comment>> = MutableLiveData(mutableListOf())

    fun loadCommentList(): MutableLiveData<MutableList<Comment>> {
        readCommentList()
        return _commentList
    }

    fun addComment(comment: Comment) {
        db.collection("comment")
            .add(comment)
    }

    private fun readCommentList() {
        db.collection("comment")
            .orderBy("createdDate")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                return@addSnapshotListener
            }
            querySnapshot?.let {
                var commentData : MutableList<Comment> = mutableListOf()
                for (document in it) {
                    val commentDesc = document.get("commentDesc").toString()
                    val ownerName = document.get("ownerName").toString()
                    val postId = document.get("postId").toString()
                    val createdDate = document.get("createdDate").toString()
                    val comment = Comment(postId, ownerName, commentDesc, createdDate)
                    commentData.add(comment)
                }
                _commentList.value = commentData
            }
        }
    }
}

internal var _commentList : MutableLiveData<MutableList<Comment>>
    get() {return _commentList}
    set(value) { _commentList = value}