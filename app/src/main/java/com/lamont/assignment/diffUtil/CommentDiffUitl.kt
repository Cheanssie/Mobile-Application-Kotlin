package com.lamont.assignment.diffUtil

import androidx.recyclerview.widget.DiffUtil
import com.lamont.assignment.model.Comment

class CommentDiffUtil (
    private val newCommentList : List<Comment>,
    private val oldCommentList : List<Comment>
): DiffUtil.Callback(){
    override fun getOldListSize(): Int {
        return oldCommentList.size
    }

    override fun getNewListSize(): Int {
        return newCommentList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldCommentList[oldItemPosition] == newCommentList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldCommentList[oldItemPosition].postId != newCommentList[newItemPosition].postId ->
                false
            else -> true
        }
    }
}