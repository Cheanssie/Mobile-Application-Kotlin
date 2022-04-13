package com.lamont.assignment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lamont.assignment.R
import com.lamont.assignment.diffUtil.CommentDiffUtil
import com.lamont.assignment.model.Comment

class CommentAdapter(val context: Context): RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private lateinit var itemListener : OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int, view: View)
    }

    private var oldCommentList: MutableList<Comment> = mutableListOf()

    class CommentViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvOwner = view.findViewById<TextView>(R.id.tvOwner)!!
        val tvCommentDesc = view.findViewById<TextView>(R.id.tvCommentDesc)!!
    }

    fun onItemClickListener(listener: OnItemClickListener) {
        itemListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)

        return CommentViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int {
        return oldCommentList.size
    }

    fun setData(newCommentList: List<Comment>) {
        val diffUtil = CommentDiffUtil(newCommentList ,oldCommentList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldCommentList.clear()
        oldCommentList.addAll(newCommentList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = oldCommentList[position]

        holder.tvOwner.text = comment.ownerName
        holder.tvCommentDesc.text = comment.commentDesc
    }

}