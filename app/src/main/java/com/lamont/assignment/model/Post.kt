package com.lamont.assignment.model

import android.net.Uri

data class Post(val postId: String, val ivProfile: Uri?, val postOwner: String, val forumDesc: String?, val imgUri: Uri?, val videoUri: Uri?, val createdDate: String, val ownerId: String)