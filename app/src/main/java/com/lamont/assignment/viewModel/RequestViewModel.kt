package com.lamont.assignment.viewModel

import android.app.DownloadManager
import androidx.lifecycle.LiveData
import com.lamont.assignment.model.Request
import com.lamont.assignment.repository.RequestRepository

class RequestViewModel() {

    private var requestRepo: RequestRepository
    private lateinit var requestList : LiveData<MutableList<Request>>

    init {
        requestRepo = RequestRepository()
        requestList = requestRepo.loadRequestList()
    }

    fun getRequestList() : LiveData<MutableList<Request>>{
        requestList = requestRepo.loadRequestList()
        return requestList
    }

}