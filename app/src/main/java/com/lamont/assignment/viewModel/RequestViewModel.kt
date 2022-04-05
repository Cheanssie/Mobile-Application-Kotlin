package com.lamont.assignment.viewModel

import android.app.DownloadManager
import android.net.Uri
import androidx.lifecycle.LiveData
import com.lamont.assignment.model.Request
import com.lamont.assignment.model.User
import com.lamont.assignment.repository.RequestRepository

class RequestViewModel() {

    private var requestRepo: RequestRepository = RequestRepository()
    lateinit var requestList : LiveData<MutableList<Request>>

    fun loadRequestList() : LiveData<MutableList<Request>>{
        requestList = requestRepo.loadRequestList()
        return requestList
    }

    fun updateStatus(requestId: String, status: Int) {
        requestRepo.updateStatus(requestId, status)
    }

    fun updateDonor(requestId: String, donorId:String) {
        requestRepo.updateDonor(requestId, donorId)
    }

    fun removeRequest(requestId: String) {
        requestRepo.removeRequest(requestId)
    }

    fun updateId(requestId: String) {
        requestRepo.updateId(requestId)
    }


}