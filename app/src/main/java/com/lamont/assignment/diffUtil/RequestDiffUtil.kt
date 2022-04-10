package com.lamont.assignment.diffUtil

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
            oldRequestList[oldItemPosition].requestId != newRequestList[newItemPosition].requestId ->
                false

            else -> true

        }
    }

}