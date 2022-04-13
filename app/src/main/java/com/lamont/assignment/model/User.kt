package com.lamont.assignment.model

data class User(val username: String = "", val email: String = "", val password: String = "", val phone:String = "", val dob: String = "", val quiz: MutableMap<String, String>, val address: String = "", val imgName: String = "user-default.png") {
}