package com.lamont.assignment.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.R
import com.lamont.assignment.diffUtil.PostDiffUtil
import com.lamont.assignment.model.Post
import com.squareup.picasso.Picasso

class PostAdapter(val context: Context): RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private lateinit var itemListener : OnItemClickListener
    private var dbAuth : FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var player: ExoPlayer

    interface OnItemClickListener {
        fun onItemClick(position: Int, view: View)
    }

    private var oldPostList: MutableList<Post> = mutableListOf()

    class PostViewHolder(view: View, listener: OnItemClickListener): RecyclerView.ViewHolder(view) {
        val like = view.findViewById<Button>(R.id.like)!!
        val forumDesc = view.findViewById<TextView>(R.id.forumDesc)!!
        val postOwner = view.findViewById<TextView>(R.id.postOwner)!!
        val postImg = view.findViewById<ImageView>(R.id.postImg)!!
        val postVideo = view.findViewById<PlayerView>(R.id.postVideo)!!
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)!!
        val postDateTime = view.findViewById<TextView>(R.id.postDateTime)!!
        val comment = view.findViewById<Button>(R.id.comment)!!

        init {
            like.setOnClickListener {
                listener.onItemClick(adapterPosition, it)
            }
            comment.setOnClickListener {
                listener.onItemClick(adapterPosition, it)
            }
            btnDelete.setOnClickListener {
                listener.onItemClick(adapterPosition, it)
            }
        }
    }

    fun onItemClickListener(listener: OnItemClickListener) {
        itemListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.forum_item, parent, false)

        return PostViewHolder(adapterLayout, itemListener)
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

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = oldPostList[position]

        holder.postOwner.text = post.postOwner
        holder.postDateTime.text = post.createdDate
        holder.forumDesc.text = post.forumDesc

        db.collection("like")
            .whereEqualTo("postId", post.postId)
            .get()
            .addOnSuccessListener{
                var exist = false
                for ( like in it ) {
                    if (like["ownerId"] == dbAuth.currentUser!!.uid) {
                        exist = true
                        break
                    }
                }
                if(exist) {
                    holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.love_red, 0, 0, 0)
                    holder.like.setTextColor(R.color.red)
                } else {
                    holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.love_black, 0, 0, 0)
                    holder.like.setTextColor(R.color.black)
                }
            }

        db.collection("like").whereEqualTo("postId", post.postId)
            .addSnapshotListener {querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    return@addSnapshotListener
                }
                querySnapshot?.let {
                    if (!it.isEmpty) {
                        holder.like.text = it.size().toString()
                    } else {
                        holder.like.text = context.getString(R.string.like)
                    }
                }
            }

        if (dbAuth.currentUser!!.uid == post.ownerId) {
            holder.btnDelete.visibility = View.VISIBLE
        }

        //Retrieve image or video
        if (post.imgUri.toString() != "null") {
            Picasso.with(context).load(post.imgUri).into(holder.postImg)

        } else if (post.videoUri.toString() != "null") {
            // this part
            player = ExoPlayer.Builder(context).build()
            holder.postVideo.player = player
            val mediaItem = MediaItem.fromUri(post.videoUri.toString())
            player.addMediaItem(mediaItem)
            player.repeatMode = Player.REPEAT_MODE_ONE
            player.playWhenReady = true
            player.volume = 0f
            holder.postVideo.visibility = View.VISIBLE
            player.prepare()
        }
    }

}