package com.lamont.assignment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lamont.assignment.R
import com.lamont.assignment.diffUtil.PostDiffUtil
import com.lamont.assignment.diffUtil.RequestDiffUtil
import com.lamont.assignment.model.Post
import com.lamont.assignment.model.Request
import com.squareup.picasso.Picasso

class PostAdapter(val context: Context): RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private lateinit var itemListener : OnItemClickListener
    private var dbAuth : FirebaseAuth = FirebaseAuth.getInstance()
//    var _postList : MutableLiveData<MutableList<Post>> = MutableLiveData(mutableListOf())

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var oldPostList: MutableList<Post> = mutableListOf()

    class PostViewHolder(view: View, listener: OnItemClickListener): RecyclerView.ViewHolder(view) {
//        val cardRequest = view.findViewById<ConstraintLayout>(R.id.cardRequest)!!
//        val comment = view.findViewById<CardView>(R.id.comment)!!
        val like = view.findViewById<TextView>(R.id.like)!!
        val forumDesc = view.findViewById<TextView>(R.id.forumDesc)!!
        val postOwner = view.findViewById<TextView>(R.id.postOwner)!!
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)!!
        val postImg = view.findViewById<ImageView>(R.id.postImg)!!

        init {
            like.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    fun onItemClickListner(listener: PostAdapter.OnItemClickListener) {
        itemListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.PostViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.forum_item, parent, false)

        return PostAdapter.PostViewHolder(adapterLayout, itemListener)
    }

    override fun getItemCount(): Int {
        return oldPostList.size
    }

    fun setData(newPostList: List<Post>) {
        val diffUtil = PostDiffUtil(newPostList ,oldPostList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldPostList.clear()
        oldPostList.addAll(newPostList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = oldPostList[position]
        val currentUserId = dbAuth.currentUser!!.uid

        holder.postOwner.text = post.postOwner
        holder.forumDesc.text = post.forumDesc

        FirebaseStorage.getInstance().reference.child("profile/${post.ivProfile}").downloadUrl
            .addOnSuccessListener {
                Picasso.with(context).load(it).into(holder.ivProfile)
            }

        if (post.postImg != null) {
            FirebaseStorage.getInstance().reference.child("post/${post.postImg}").downloadUrl
                .addOnSuccessListener {
                    Picasso.with(context).load(it).into(holder.postImg)
                }
        }
    }

}