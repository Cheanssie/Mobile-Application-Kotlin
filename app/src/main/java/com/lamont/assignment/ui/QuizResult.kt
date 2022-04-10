package com.lamont.assignment.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.ModuleActivity
import com.lamont.assignment.R
import com.lamont.assignment.databinding.ActivityQuizResultBinding

class QuizResult : AppCompatActivity() {

    lateinit var binding: ActivityQuizResultBinding
    private lateinit var dbResult : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize views
        var quizMenuBtn = findViewById<AppCompatButton>(R.id.quizHomeBtn)
        var leaderboardBtn = findViewById<AppCompatButton>(R.id.leaderboardBtn)
        var gratitudeImg = findViewById<ImageView>(R.id.ivCongrats)
        var correctAns = findViewById<TextView>(R.id.tvCorrectAns)
        var incorrectAns = findViewById<TextView>(R.id.tvIncorrectAns)
        var gratitudeMsg = findViewById<TextView>(R.id.tvGratitudeMessage)
        var points = findViewById<TextView>(R.id.tvPoints)

        //Instantiate database
        dbResult = FirebaseFirestore.getInstance()

        //Getting passed value from Quiz Activity upon completion of the quiz
        val getCorrectAns = intent.getIntExtra("correct", 0)
        val getIncorrectAns = intent.getIntExtra("incorrect", 0)
        val getTimesUp : Boolean = intent.getBooleanExtra("timesUp", false)
        val getPoints : Long = intent.getLongExtra("points", 0)

        //Display appropriate message as gratitude message/result message
        if(getTimesUp) {
//            dbResult.collection("result").document("Nice Try")
//                .get()
//                .addOnSuccessListener {
//                    val image : String = it.get("image").toString()
//
//                    Glide.with(gratitudeImg)
//                        .asGif()
//                        .load(image)
//                        .into(gratitudeImg)
//                }
            gratitudeMsg.text = "Time's Up !"
        }else{
//            dbResult.collection("result").document("Good Job")
//                .get()
//                .addOnSuccessListener {
//                    val image : String = it.get("image").toString()
//
//                    Glide.with(gratitudeImg)
//                        .asGif()
//                        .load(image)
//                        .into(gratitudeImg)
//                }
            gratitudeMsg.text = "You have completed the quiz!"
        }


        //Displaying total correct, incorrect answers and points accumulated
        points.text = "Total Points : $getPoints"
        correctAns.text = getCorrectAns.toString()
        incorrectAns.text = getIncorrectAns.toString()

        //Button to Quiz Main Menu
        quizMenuBtn.setOnClickListener {
            Intent(this@QuizResult, QuizFragment::class.java).apply{
                startActivity(this)
            }
            finish()
        }

        //Button to Leaderboard page
        leaderboardBtn.setOnClickListener {
            Intent(this@QuizResult, ModuleActivity::class.java).apply{
                startActivity(this)
            }
            finish()
        }
    }
}