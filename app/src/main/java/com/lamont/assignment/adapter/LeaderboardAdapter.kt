package com.lamont.assignment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.contentValuesOf
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lamont.assignment.R
import com.lamont.assignment.model.Leaderboard

class LeaderboardAdapter(val context: Context,var leaderUser: List<Leaderboard>) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>()  {

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        //initialize views
        val ivBadgeIcon = itemView.findViewById<ImageView>(R.id.ivBadgeIcon)!!
        val ivUserImg = itemView.findViewById<ImageView>(R.id.ivUserImage)!!
        val tvLeaderName = itemView.findViewById<TextView>(R.id.tvLeaderName)!!
        val tvPoints = itemView.findViewById<TextView>(R.id.tvPoints)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_item, parent, false)

        return LeaderboardViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {

        val leaderUsers : Leaderboard = leaderUser[position]
        Glide.with(context)
            .load(leaderUsers.userImg)
            .into(holder.ivUserImg)
        holder.tvLeaderName.text = leaderUsers.username
        holder.tvPoints.text = leaderUsers.points.toString()
    }

    override fun getItemCount(): Int {
        return leaderUser.size
    }
}