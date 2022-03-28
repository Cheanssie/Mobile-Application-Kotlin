package com.lamont.assignment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.databinding.ActivityModuleBinding
import com.lamont.assignment.ui.RequestFragment


class ModuleActivity : AppCompatActivity() {

    lateinit var binding: ActivityModuleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Bottom Navigation Bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navController = findNavController(R.id.main_fragment)
        bottomNavigationView.setupWithNavController(navController)

        //Check user status when auto login
        val sharedPreferences = getSharedPreferences("SHARE_PREF", Context.MODE_PRIVATE)
        var username = sharedPreferences!!.getString("username", null)
        var password = sharedPreferences!!.getString("password", null)
        checkUserStatus(username, password, sharedPreferences)


    }

    fun displayPopupMenu(view: View) {
        val popupMenu = PopupMenu(applicationContext, findViewById(R.id.addPost))
        popupMenu.inflate(R.menu.post_menu)
        popupMenu.setOnMenuItemClickListener {
            val navController = findNavController(R.id.main_fragment)
            navController.navigateUp()

            when(it.itemId) {
                R.id.addForum -> {

                }
                else -> {
                    navController.navigate(R.id.requestFragment)
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    private fun checkUserStatus(username:String?, password:String?, sharedPreferences:SharedPreferences) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(username!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.data?.get("password") != password) {
                    val sharedPreferencesEditor = sharedPreferences.edit()
                    sharedPreferencesEditor.remove("password")
                    sharedPreferencesEditor.remove("username")
                    sharedPreferencesEditor.commit()
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Please login again!", Toast.LENGTH_SHORT).show()
                }
            }
    }

}