package com.lamont.assignment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.databinding.ActivityModuleBinding
import com.lamont.assignment.viewModel.RequestViewModel

class ModuleActivity : AppCompatActivity() {

    lateinit var binding: ActivityModuleBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var db : FirebaseFirestore
    lateinit var dbAuth : FirebaseAuth

    //notification
    private val CHANNEL_ID = "Request_Fulfilled"
    private val NOTIFICATION_ID = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()
        dbAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences(getString(R.string.share_pref), Context.MODE_PRIVATE)

        //Bottom Navigation Bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navController = findNavController(R.id.main_fragment)
        bottomNavigationView.setupWithNavController(navController)

        //Check user status when auto login
        checkUserStatus()

        //Notification call
        whiteFlagNotification()
    }

    fun displayPopupMenu(view: View) {
        val popupMenu = PopupMenu(applicationContext, findViewById(R.id.addPost))
        popupMenu.inflate(R.menu.post_menu)
        popupMenu.setOnMenuItemClickListener {
            val navController = findNavController(R.id.main_fragment)
            when(it.itemId) {
                R.id.addForum -> {
                    navController.navigate(R.id.postFragment)
                }
                else -> {
                    navController.navigate(R.id.requestFragment)
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    fun whiteFlagNotification() {
        val requestModel = RequestViewModel()
        requestModel.loadRequestList().observe(this, Observer {
            for (request in it) {
                if(request.ownerId == dbAuth.currentUser!!.uid && request.status == 3) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val name = CHANNEL_ID
                        val descriptionText = getString(R.string.descText)
                        val importance = NotificationManager.IMPORTANCE_HIGH
                        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                            description = descriptionText
                        }
                        val notificationManager: NotificationManager =  getSystemService(
                            NOTIFICATION_SERVICE
                        ) as NotificationManager
                        notificationManager.createNotificationChannel(channel)

                        Intent(this, ModuleActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        val pendingIntent = NavDeepLinkBuilder(this)
                            .setComponentName(ModuleActivity::class.java)
                            .setGraph(R.navigation.general_nav)
                            .setDestination(R.id.whiteFlagFragment)
                            .createPendingIntent()


                        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.logo_dark)
                            .setContentTitle(CHANNEL_ID)
                            .setContentText(descriptionText)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                            .setOnlyAlertOnce(true)
                        with(NotificationManagerCompat.from(this)) {
                            notify(NOTIFICATION_ID, builder.build())
                        }

                    }
                }
            }
        })
    }

    fun checkUserStatus() {
        db.collection("users").document(dbAuth.currentUser?.uid!!)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                var email = sharedPreferences!!.getString("email", null)
                var password = sharedPreferences!!.getString("password", null)
                if (querySnapshot != null) {
                    if(querySnapshot.get("email").toString() != email || querySnapshot.get("password").toString() != password) {
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
            }
    }

}