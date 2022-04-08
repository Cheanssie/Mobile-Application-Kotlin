package com.lamont.assignment.model

import android.net.Uri

data class Post(val postId: String, val ivProfile: String?, val postOwner: String, val forumDesc: String?, val imgUri: Uri?, val videoUri: Uri?, val createdDate: String) {
}