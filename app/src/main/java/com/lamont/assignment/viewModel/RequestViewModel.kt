package com.lamont.assignment.viewModel

import android.app.DownloadManager
import androidx.lifecycle.LiveData
import com.lamont.assignment.model.Request
import com.lamont.assignment.repository.RequestRepository

class RequestViewModel() {

    private var requestRepo: RequestRepository
    var requestList : LiveData<MutableList<Request>>

    init {
        requestRepo = RequestRepository()
        requestList = requestRepo.loadRequestList()

    }

    fun loadRequestList() : LiveData<MutableList<Request>>{
        //requestList = requestRepo.loadRequestList()
        return requestList
    }

    fun updateStatus(requestId: String, status: Int) {
        requestRepo.updateStatus(requestId, status)
    }

    fun updateDonor(requestId: String, donor:String) {
        requestRepo.updateDonor(requestId, donor)
    }

    fun removeRequest(requestId: String) {
        requestRepo.removeRequest(requestId)
    }

}