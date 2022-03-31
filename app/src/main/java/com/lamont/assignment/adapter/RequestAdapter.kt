package com.lamont.assignment.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.lamont.assignment.R
import com.lamont.assignment.diffUtil.RequestDiffUtil
import com.lamont.assignment.model.Request
import java.io.File

class RequestAdapter(): RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    var oldRequestList: MutableList<Request> = mutableListOf()

    class RequestViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvDesc = view.findViewById<TextView>(R.id.tvDesc)
        val tvCat = view.findViewById<TextView>(R.id.tvCat)
        val ivImg = view.findViewById<ImageView>(R.id.ivImg)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_item, parent, false)

        return RequestViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = oldRequestList[position]
        holder.tvName.text = request.name.toString()
        holder.tvDesc.text = request.desc.toString()
        holder.tvCat.text = request.category.toString()

        //Retrieve images
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${request.imgName}")
        val localFile = File.createTempFile("tempImg", "jpg")
        storageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            holder.ivImg.setImageBitmap(bitmap)
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