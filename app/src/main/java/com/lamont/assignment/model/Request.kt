package com.lamont.assignment.model

data class Request(val requestId: String?, val ownerId:String, val owner: String, val desc: String, val category: String, val imgName: String?, var donorId: String?, val createdDate: String, var status: Int = 1) {
    //username is unique
    //username of the post's owner will be stored as owner
    //donor can be null first, then later username of donor will be stored as donor
    //status 1(Pending to be accepted) 2(Accepted, only can be view by owner and donor) 3(Completed, remove the request)
    //button O:Remove  D:Donate          O:Received  D:Done                                Remove request from database
}