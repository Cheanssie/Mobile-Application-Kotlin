package com.lamont.assignment.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lamont.assignment.R
import com.lamont.assignment.diffUtil.RequestDiffUtil
import com.lamont.assignment.model.Request
import com.squareup.picasso.Picasso
import java.io.File

class RequestAdapter(val context: Context): RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    private lateinit var itemListener : onItemClickListener
    var dbAuth : FirebaseAuth = FirebaseAuth.getInstance()

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    var oldRequestList: MutableList<Request> = mutableListOf()

    class RequestViewHolder(private val view: View, listener: onItemClickListener): RecyclerView.ViewHolder(view) {
        val cardViewParent = view.findViewById<ConstraintLayout>(R.id.cardRequestParent)
        val cardView = view.findViewById<CardView>(R.id.cardRequest)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvDesc = view.findViewById<TextView>(R.id.tvDesc)
        val tvCat = view.findViewById<TextView>(R.id.tvCat)
        val ivImg = view.findViewById<ImageView>(R.id.ivImg)
        val btnDonate = view.findViewById<Button>(R.id.btnDonate)

        init {
            btnDonate.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }

    }

    fun onItemClickListner(listener: onItemClickListener) {
        itemListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_item, parent, false)

        return RequestViewHolder(adapterLayout, itemListener)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = oldRequestList[position]

        if(request.ownerId == dbAuth.currentUser!!.uid || request.donorId == dbAuth.currentUser!!.uid || request.donorId == "null") {
            val currentUserId = dbAuth.currentUser!!.uid
            holder.tvName.text = request.owner
            holder.tvDesc.text = request.desc
            holder.tvCat.text = request.category

            var buttonText = ""

            if (currentUserId == request.ownerId) {
                when (request.status) {
                    1 -> buttonText = "REMOVE"
                    2, 3 -> buttonText = "RECEIVED"
                }
            } else if (currentUserId == request.donorId) {
                when (request.status) {
                    1 -> buttonText = "DONATE"
                    2 -> buttonText = "INFO"
                    3 -> buttonText = "DONE"

                }
            } else {
                when (request.status) {
                    1 -> buttonText = "DONATE"
                    2, 3 -> buttonText = "N/A"
                }
            }
            holder.btnDonate.text = buttonText

            //Retrieve images
            val storageRef =
                FirebaseStorage.getInstance().reference.child("images/${request.imgName}")
            storageRef.downloadUrl
                .addOnSuccessListener {
                    Picasso.with(context).load(it).into(holder.ivImg)
                }
        } else {
            holder.btnDonate.visibility = View.GONE
            holder.tvName.visibility = View.GONE
            holder.tvDesc.visibility = View.GONE
            holder.tvCat.visibility = View.GONE
            holder.cardView.visibility = View.GONE
            holder.cardViewParent.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return oldRequestList.size
    }

    fun setData(newRequestList: List<Request>) {
        val diffUtil = RequestDiffUtil(newRequestList ,oldRequestList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldRequestList.clear()
        oldRequestList.addAll(newRequestList)
        diffResult.dispatchUpdatesTo(this)
    }

}