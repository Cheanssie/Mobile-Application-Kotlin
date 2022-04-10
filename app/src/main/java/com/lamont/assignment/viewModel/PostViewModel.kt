package com.lamont.assignment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lamont.assignment.model.Post
import com.lamont.assignment.repository.PostRepository

class PostViewModel {

    companion object {
        private var postRepo: PostRepository = PostRepository()

        fun addLike(like: MutableMap<String, Any>) {
            postRepo.addLike(like)
        }

        fun removeLike(likeId: String) {
            postRepo.removeLike(likeId)
        }

        fun addComment(comment: MutableMap<String, Any>) {
            postRepo.addComment(comment)
        }

        fun removeComment(commentId: String) {
            postRepo.removeComment(commentId)
        }

        fun deletePost(postId: String) {
           postRepo.deletePost(postId)
        }

    }

    lateinit var postList : MutableLiveData<MutableList<Post>>

    fun loadPostList() : LiveData<MutableList<Post>> {
        postList = postRepo.loadPostList()
        return postList
    }
}