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
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.databinding.ActivityModuleBinding
import com.lamont.assignment.ui.RequestFragment


class ModuleActivity : AppCompatActivity() {

    lateinit var binding: ActivityModuleBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var db : FirebaseFirestore
    lateinit var dbAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()
        dbAuth = FirebaseAuth.getInstance()

        //Bottom Navigation Bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navController = findNavController(R.id.main_fragment)
        bottomNavigationView.setupWithNavController(navController)

        //Check user status when auto login
        sharedPreferences = getSharedPreferences("SHARE_PREF", Context.MODE_PRIVATE)
        var email = sharedPreferences!!.getString("email", null)
        checkUserStatus(email, sharedPreferences)


    }

    fun displayPopupMenu(view: View) {
        val popupMenu = PopupMenu(applicationContext, findViewById(R.id.addPost))
        popupMenu.inflate(R.menu.post_menu)
        popupMenu.setOnMenuItemClickListener {
            val navController = findNavController(R.id.main_fragment)
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

    fun checkUserStatus(email:String?, sharedPreferences:SharedPreferences) {
        db.collection("users").document(dbAuth.currentUser?.uid!!)
            .get()
            .addOnSuccessListener {
                if(it.get("email").toString() != email) {
                    dbAuth.signOut()
                    val editPref = sharedPreferences.edit()
                    editPref.remove("email")
                    editPref.remove("password")
                    editPref.remove("username")
                    editPref.commit()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

}