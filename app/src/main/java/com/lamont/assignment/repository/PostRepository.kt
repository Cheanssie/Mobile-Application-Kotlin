package com.lamont.assignment.repository

import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lamont.assignment.model.Post

class PostRepository {
    var db : FirebaseFirestore = FirebaseFirestore.getInstance()
    var _postList : MutableLiveData<MutableList<Post>> = MutableLiveData(mutableListOf())

    fun loadPostList(): MutableLiveData<MutableList<Post>> {
        readPostList()
        return _postList
    }

    fun removeLike(likeId: String) {
        db.collection("like")
            .document(likeId)
            .delete()
    }

    fun addLike(like: MutableMap<String, Any>) {
        db.collection("like")
            .add(like)
    }

    fun removeComment(commentId: String) {
        db.collection("comment")
            .document(commentId)
            .delete()
    }

    fun addComment(comment: MutableMap<String, Any>) {
        db.collection("comment")
            .add(comment)
    }

    fun deletePost(postId: String) {
        db.collection("post")
            .document(postId)
            .delete()
        db.collection("like")
            .whereEqualTo("postId", postId)
            .get()
            .addOnSuccessListener {
                for (like in it) {
                    removeLike(like.id)
                }
            }
        db.collection("comment")
            .whereEqualTo("postId", postId)
            .get()
            .addOnSuccessListener {
                for (comment in it) {
                    removeComment(comment.id)
                }
            }
    }

    private fun readPostList() {
        db.collection("post").orderBy("createdDate", Query.Direction.DESCENDING).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                return@addSnapshotListener
            }
            querySnapshot?.let {
                var postData : MutableList<Post> = mutableListOf()
                for (document in it) {
                    val forumDesc = document.get("forumDesc").toString()
                    val postOwner = document.get("postOwner").toString()
                    val imgUri = document.get("imgUri").toString().toUri()
                    val videoUri = document.get("videoUri").toString().toUri()
                    val createdDate = document.get("createdDate").toString()
                    val ownerId = document.get("ownerId").toString()
                    val ivProfile = document.get("ivProfile").toString().toUri()
                    val postId = document.get("postId").toString()
                    val post = Post(postId, ivProfile, postOwner, forumDesc, imgUri, videoUri, createdDate, ownerId)
                    postData.add(post)
                }
                _postList.value = postData
            }
        }
    }
}

internal var _postList : MutableLiveData<MutableList<Post>>
    get() {return _postList}
    set(value) { _postList = value}