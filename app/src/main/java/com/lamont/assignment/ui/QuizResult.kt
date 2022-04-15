package com.lamont.assignment.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.R
import com.lamont.assignment.databinding.ActivityQuizResultBinding


class QuizResult : AppCompatActivity() {

    lateinit var binding: ActivityQuizResultBinding
    private lateinit var dbResult : FirebaseFirestore
    private lateinit var dbAuth : FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize views
        val quizMenuBtn = findViewById<AppCompatButton>(R.id.quizHomeBtn)
        val leaderboardBtn = findViewById<AppCompatButton>(R.id.leaderboardBtn)
        val gratitudeImg = findViewById<ImageView>(R.id.ivCongrats)
        val correctAns = findViewById<TextView>(R.id.tvCorrectAns)
        val incorrectAns = findViewById<TextView>(R.id.tvIncorrectAns)
        val gratitudeMsg = findViewById<TextView>(R.id.tvGratitudeMessage)
        val points = findViewById<TextView>(R.id.tvPoints)

        //Instantiate database
        dbResult = FirebaseFirestore.getInstance()
        dbAuth = FirebaseAuth.getInstance()

        //Getting passed value from Quiz Activity upon completion of the quiz
        val getCorrectAns = intent.getIntExtra("correct", 0)
        val getIncorrectAns = intent.getIntExtra("incorrect", 0)
        val getTimesUp : Boolean = intent.getBooleanExtra("timesUp", false)
        val getPoints : Long = intent.getLongExtra("points", 0)
        val quiz : String = intent.getStringExtra("quiz").toString()
        Log.d("quiz", quiz)

        //Display appropriate message as gratitude message/result message
        if(getTimesUp) {
            dbResult.collection("result").document("Nice Try")
                .get()
                .addOnSuccessListener {
                    val image : String = it.get("image").toString()

                    Glide.with(gratitudeImg)
                        .asGif()
                        .load(image)
                        .into(gratitudeImg)
                }
            gratitudeMsg.text = getString(R.string.timesUpText)
        }else{
            dbResult.collection("result").document("Good Job")
                .get()
                .addOnSuccessListener {
                    val image : String = it.get("image").toString()

                    Glide.with(gratitudeImg)
                        .asGif()
                        .load(image)
                        .into(gratitudeImg)
                }

            gratitudeMsg.text = getString(R.string.quizCompletionText)
        }

        //Adding results of the selected quiz into Firestore
        dbResult.collection("users").document(dbAuth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                //To check if quiz exists. If yes : Compare points and update else : Append quiz with points into map
                @Suppress("UNCHECKED_CAST") //100% a map from Firestore (https://stackoverflow.com/questions/58537743/kotlin-checked-cast-from-any-to-mapstring-any)
                val quizPoints: MutableMap<String, Long> =
                    it.get("quiz") as MutableMap<String, Long>
                //Check for null
                if (quizPoints.contains(quiz)) {
                    if (quizPoints[quiz]!! < getPoints) {
                        quizPoints[quiz] = getPoints
                        dbResult.collection("users").document(dbAuth.currentUser!!.uid)
                            .update("quiz", quizPoints)
                    }
                } else {
                    quizPoints[quiz] = getPoints
                    dbResult.collection("users").document(dbAuth.currentUser!!.uid)
                        .update("quiz", quizPoints)
                }
            }


        //Displaying total correct, incorrect answers and points accumulated
        points.text = "Total Points : $getPoints"
        correctAns.text = getCorrectAns.toString()
        incorrectAns.text = getIncorrectAns.toString()

        val fragmentManager: FragmentManager = supportFragmentManager

        //Button to Quiz Main Menu
        quizMenuBtn.setOnClickListener {
            //New instance of fragment so contents will not be overlapped
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            val quizMenu = QuizFragment()
            fragmentTransaction.add(R.id.frameLayout, quizMenu).commit()
            finish()
        }

        //Button to Leaderboard page
        leaderboardBtn.setOnClickListener {
            //New instance of fragment so contents will not be overlapped
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            val leaderboardFragment = LeaderboardFragment()
            //Passing data as bundle = arguments
            val bundle = Bundle()
            bundle.putString("quiz", quiz)
            bundle.putLong("points", getPoints)
            leaderboardFragment.arguments = bundle
            fragmentTransaction.replace(R.id.frameLayout, leaderboardFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
