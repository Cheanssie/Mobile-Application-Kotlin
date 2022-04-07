package com.lamont.assignment.diffUtil

import androidx.recyclerview.widget.DiffUtil
import com.lamont.assignment.model.Post

class PostDiffUtil (
    private val newPostList : List<Post>,
    private val oldPostList : List<Post>
    ): DiffUtil.Callback(){
        override fun getOldListSize(): Int {
            return oldPostList.size
        }

        override fun getNewListSize(): Int {
            return newPostList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldPostList[oldItemPosition] == newPostList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return when {
                oldPostList[oldItemPosition].postId != newPostList[newItemPosition].postId ->
                    false

                else -> true

            }
        }
}