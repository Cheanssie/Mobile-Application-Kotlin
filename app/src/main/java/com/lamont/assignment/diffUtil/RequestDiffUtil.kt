package com.lamont.assignment.diffUtil

import android.app.DownloadManager
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import com.lamont.assignment.model.Request

class RequestDiffUtil (
    private val newRequestList : List<Request>,
    private val oldRequestList : List<Request>
        ): DiffUtil.Callback(){
    override fun getOldListSize(): Int {
        return oldRequestList.size
    }

    override fun getNewListSize(): Int {
        return newRequestList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldRequestList[oldItemPosition] == newRequestList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldRequestList[oldItemPosition]?.requestId != newRequestList[newItemPosition].requestId ->
                false
            oldRequestList[oldItemPosition]?.owner != newRequestList[newItemPosition].owner ->
                false
            oldRequestList[oldItemPosition]?.desc != newRequestList[newItemPosition].desc ->
                false
            oldRequestList[oldItemPosition]?.category != newRequestList[newItemPosition].category ->
                false
            oldRequestList[oldItemPosition]?.imgName != newRequestList[newItemPosition].imgName ->
                false
            else -> true

        }
    }

}