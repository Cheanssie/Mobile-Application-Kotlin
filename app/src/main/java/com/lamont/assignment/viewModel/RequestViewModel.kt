package com.lamont.assignment.viewModel

import android.app.DownloadManager
import androidx.lifecycle.LiveData
import com.lamont.assignment.model.Request
import com.lamont.assignment.repository.RequestRepository

class RequestViewModel() {

    private var requestRepo: RequestRepository

    init {
        requestRepo = RequestRepository()
    }

    fun getRequestList() : LiveData<MutableList<Request>>{
        return requestRepo.loadRequestList()
    }

}