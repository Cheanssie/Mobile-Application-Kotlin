package com.lamont.assignment.repository

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.ModuleActivity
import com.lamont.assignment.model.Request
import kotlin.coroutines.coroutineContext

class RequestRepository() {
    lateinit var db : FirebaseFirestore
    lateinit var dbAuth : FirebaseAuth
    var _requestList : MutableLiveData<MutableList<Request>> = MutableLiveData(mutableListOf())

    fun loadRequestList(): MutableLiveData<MutableList<Request>> {
        readRequestList()
        return _requestList
    }

    private fun readRequestList() {
        db = FirebaseFirestore.getInstance()
        db.collection("request").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                return@addSnapshotListener
            }
            querySnapshot?.let {
                var requestData : MutableList<Request> = mutableListOf()
                for (document in it) {
                    val requestId = document.get("requestId").toString()
                    val name = document.get("owner").toString()
                    val desc= document.get("desc").toString()
                    val category = document.get("category").toString()
                    val imgName = document.get("imgName").toString()
                    val status = document.get("status").toString().toIntOrNull()
                    val donor = document.get("donor").toString()
                    val request = Request(requestId, name, desc, category, imgName, donor, status!!)
                    requestData.add(request)
                }
                _requestList.value = requestData
            }
        }
    }
}

internal var _requestList : MutableLiveData<MutableList<Request>>
get() {return _requestList}
set(value) { _requestList = value}