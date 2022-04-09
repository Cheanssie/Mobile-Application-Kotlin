package com.lamont.assignment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.firebase.auth.FirebaseAuth
import com.lamont.assignment.R
import com.lamont.assignment.diffUtil.PostDiffUtil
import com.lamont.assignment.model.Post
import com.squareup.picasso.Picasso

class PostAdapter(val context: Context): RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private lateinit var itemListener : OnItemClickListener
    private var dbAuth : FirebaseAuth = FirebaseAuth.getInstance()
//    var _postList : MutableLiveData<MutableList<Post>> = MutableLiveData(mutableListOf())

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

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
        val postVideo = view.findViewById<PlayerView>(R.id.postVideo)!!

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

        //Retrieve image or video
        if (post.imgUri.toString() != "null") {
            Picasso.with(context).load(post.imgUri).into(holder.postImg)

        } else if (post.videoUri.toString() != "null") {
            // this part
            player = ExoPlayer.Builder(context).build()
            holder.postVideo.player = player
            val mediaItem = MediaItem.fromUri(post.videoUri.toString())
            player.addMediaItem(mediaItem)
            player.prepare()





//            holder.postVideo.()
//            holder.postVideo.visibility = View.VISIBLE
        }
    }

}