package com.lamont.assignment.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lamont.assignment.R
import com.lamont.assignment.model.Leaderboard

class LeaderboardAdapter(val context: Context, private var leaderUser: List<Leaderboard>) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>()  {



    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        //initialize views
        val ivBadgeIcon = itemView.findViewById<ImageView>(R.id.ivBadgeIcon)!!
        val tvLeaderPosition = itemView.findViewById<TextView>(R.id.tvLeaderPosition)!!
        val tvLeaderName = itemView.findViewById<TextView>(R.id.tvLeaderName)!!
        val tvPoints = itemView.findViewById<TextView>(R.id.tvLeaderPoints)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_item, parent, false)

        return LeaderboardViewHolder(adapterLayout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        //Binding contents to views
        val leaderUsers : Leaderboard = leaderUser[position]
        if(position > 2){
            Glide.with(context)
                .load(R.drawable.participation_medal)
                .into(holder.ivBadgeIcon)
        }else{
            Glide.with(context)
                .load(R.drawable.top_medal)
                .into(holder.ivBadgeIcon)
        }
        holder.tvLeaderPosition.text = (position + 1).toString()
        holder.tvLeaderName.text = leaderUsers.username
        holder.tvPoints.text = leaderUsers.points.toString()
    }

    override fun getItemCount(): Int {
        return leaderUser.size
    }
}