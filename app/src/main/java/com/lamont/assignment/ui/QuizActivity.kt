package com.lamont.assignment.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import com.lamont.assignment.R
import com.lamont.assignment.model.Quiz
import com.lamont.assignment.databinding.ActivityQuizBinding
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class QuizActivity : AppCompatActivity() {

    lateinit var binding: ActivityQuizBinding
    private var getSelectedQuiz : String? = null
    private var getQuizDuration : Long = 0
    private lateinit var tvQuizQuestion : TextView
    private lateinit var pbQuestionNumber: ProgressBar
    private lateinit var tvQuestionNumber : TextView
    private lateinit var opt1 : AppCompatButton
    private lateinit var opt2 : AppCompatButton
    private lateinit var opt3 : AppCompatButton
    private lateinit var opt4 : AppCompatButton
    private lateinit var nextBtn : AppCompatButton
    private lateinit var dbQuestion : Quiz
    private lateinit var timerText : TextView
    private lateinit var selectedQuizTitle : TextView
    private var quizQuestions : MutableList<Quiz> = ArrayList()
    private var currentQuestion : Int = 0
    private var selectedAnswer : String = ""
    private val FINAL_SECTOMILI_VALUE : Long = 60000
    private var timesUp : Boolean = false
    private var timePoints : Long = 0
    private var totalPoints : Long = 0
    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialing views to variables
        var backBtn = findViewById<ImageView>(R.id.backBtn)

        tvQuizQuestion = findViewById(R.id.tvQuizQuestion)
        pbQuestionNumber = findViewById(R.id.pbQuestionNumber)
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber)

        opt1 = findViewById(R.id.optionBtn1)
        opt2 = findViewById(R.id.optionBtn2)
        opt3 = findViewById(R.id.optionBtn3)
        opt4 = findViewById(R.id.optionBtn4)
        nextBtn = findViewById(R.id.nextBtn)
        timerText = findViewById(R.id.tvTimer)

        selectedQuizTitle = findViewById(R.id.tvSelectedQuizTitle)

        //Getting appropriate passed value from selected quiz (main quiz page)
        getSelectedQuiz = intent.getStringExtra("selectedQuiz")
        getQuizDuration = intent.getLongExtra("duration", 0)
        //Prep timer in milliseconds
        getQuizDuration *= FINAL_SECTOMILI_VALUE
        //Initialize title
        selectedQuizTitle.text = getSelectedQuiz

        //instantiate Realtime database
        val dbReal : DatabaseReference = FirebaseDatabase.getInstance("https://bewithyouth-28168-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        //Collecting dt from Realtime database and creating Quiz objects
        dbReal.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot in dataSnapshot.child(getSelectedQuiz.toString()).children) {
                    var question: String = snapshot.child("question").value.toString()
                    var qOpt1: String = snapshot.child("opt1").value.toString()
                    var qOpt2: String = snapshot.child("opt2").value.toString()
                    var qOpt3: String = snapshot.child("opt3").value.toString()
                    var qOpt4: String = snapshot.child("opt4").value.toString()
                    var answer = snapshot.child("answer").value.toString()

                    dbQuestion = Quiz(question, qOpt1, qOpt2, qOpt3, qOpt4, answer, "")
                    quizQuestions.add(dbQuestion)
                }

                //Starting timer for selected quiz
                timer = object : CountDownTimer(getQuizDuration, 1000) {

                    @SuppressLint("SetTextI18n")
                    override fun onTick(miliUntilFinished: Long) {
                        val timeLeft : String = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(miliUntilFinished),
                            TimeUnit.MILLISECONDS.toSeconds(miliUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(miliUntilFinished)))
                            //Subtract minutes and convert to seconds
                        timerText.text = timeLeft
                        timePoints = TimeUnit.MILLISECONDS.toSeconds(miliUntilFinished)
                    }

                    //Time's up = All unanswered questions will be incorrect and user will be redirected to result page
                    //Correct answers are kept as usual for points collecting
                    override fun onFinish() {
                        val correctAnswer = correctAnswer()
                        totalPoints = correctAnswer * timePoints
                        timesUp = true
                        Intent(this@QuizActivity, QuizResult::class.java).apply{
                            putExtra("correct", correctAnswer)
                            putExtra("incorrect", quizQuestions.size - correctAnswer)
                            putExtra("timesUp", timesUp)
                            putExtra("points", totalPoints)
                            startActivity(this)
                        }
                    }
                }.start()
                //Reusable function for question calling / Resetting
                resetQuestions()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("error", databaseError.message)

            }
        })
        //Only visible upon selecting answer
        nextBtn.visibility = View.INVISIBLE

        //opt{1..4} = next button available upon click
        //Validate selected answers. (green) correct else (red) wrong.
        //Valid answer will be displayed
        opt1.setOnClickListener {
            nextBtn.visibility = View.VISIBLE
            if(selectedAnswer.isEmpty()){
                selectedAnswer = opt1.text.toString()
                opt1.setTextColor(Color.WHITE)
                when(selectedAnswer){
                    quizQuestions[currentQuestion].answer -> opt1.setBackgroundResource(R.drawable.correct_answer)
                    else -> {
                        opt1.setBackgroundResource(R.drawable.wrong_answer)
                        answer()
                    }
                }
            }
        }

        opt2.setOnClickListener {
            nextBtn.visibility = View.VISIBLE
            if(selectedAnswer.isEmpty()){
                opt2.setTextColor(Color.WHITE)
                selectedAnswer = opt2.text.toString()
                when(selectedAnswer){
                    quizQuestions[currentQuestion].answer -> opt2.setBackgroundResource(R.drawable.correct_answer)
                    else -> {
                        opt2.setBackgroundResource(R.drawable.wrong_answer)
                        answer()
                    }
                }
            }
        }

        opt3.setOnClickListener {
            nextBtn.visibility = View.VISIBLE
            if(selectedAnswer.isEmpty()){
                opt3.setTextColor(Color.WHITE)
                selectedAnswer = opt3.text.toString()
                when(selectedAnswer){
                    quizQuestions[currentQuestion].answer -> opt3.setBackgroundResource(R.drawable.correct_answer)
                    else -> {
                        opt3.setBackgroundResource(R.drawable.wrong_answer)
                        answer()
                    }
                }
            }
        }

        opt4.setOnClickListener {
            nextBtn.visibility = View.VISIBLE
            if(selectedAnswer.isEmpty()){
                opt4.setTextColor(Color.WHITE)
                selectedAnswer = opt4.text.toString()
                when(selectedAnswer){
                    quizQuestions[currentQuestion].answer -> opt4.setBackgroundResource(R.drawable.correct_answer)
                    else -> {
                        opt4.setBackgroundResource(R.drawable.wrong_answer)
                        answer()
                    }
                }
            }
        }

        //Selected answers are kept in object for leaderboard points (stored in user's object)
        nextBtn.setOnClickListener {
                nextBtn.visibility = View.INVISIBLE
                quizQuestions[currentQuestion].selectedAnswer = selectedAnswer
                nextQuestion()
        }

        backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    //Getting total correct answers
    private fun correctAnswer(): Int {
        var correctAnswers = 0
        for (i in quizQuestions.indices){

            if(quizQuestions[i].answer == quizQuestions[i].selectedAnswer){
                correctAnswers++
            }

        }
        //db.collection("users").document(dbAuth.currentUser!!.uid).update("score", mark)

        return correctAnswers
    }

    //Back button function
    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this, QuizFragment::class.java).apply {
            startActivity(this)
        }
        finish()
    }

    //Displaying the correct answer upon validating selected option
    private fun answer(){

        when (quizQuestions[currentQuestion].answer) {
            opt1.text.toString() -> {
                opt1.apply {
                    setBackgroundResource(R.drawable.correct_answer)
                    setTextColor(Color.WHITE)}
            }
            opt2.text.toString() -> {
                opt2.apply {
                    setBackgroundResource(R.drawable.correct_answer)
                    setTextColor(Color.WHITE)}
            }
            opt3.text.toString() -> {
                opt3.apply {
                    setBackgroundResource(R.drawable.correct_answer)
                    setTextColor(Color.WHITE)}
            }
            opt4.text.toString() -> {
                opt4.apply {
                    setBackgroundResource(R.drawable.correct_answer)
                    setTextColor(Color.WHITE)}
            }
        }
    }

    //Increment question and check current question number
    private fun nextQuestion(){
        currentQuestion += 1

        when (quizQuestions.size) {
            (currentQuestion + 1) -> nextBtn.text = "Complete"
        }

        if(currentQuestion == quizQuestions.size){
            timer.cancel()
            val correctAnswer = correctAnswer()
            totalPoints = correctAnswer * timePoints
            Intent(this, QuizResult::class.java).apply {
                putExtra("correct", correctAnswer)
                putExtra("incorrect", quizQuestions.size - correctAnswer)
                putExtra("timesUp", timesUp)
                putExtra("points", totalPoints)
                startActivity(this)
            }
            finish()
        }else{
            resetQuestions()
        }
    }

    //Displaying the question and resetting to its original state
    //Applying default theme
    private fun resetQuestions(){
        pbQuestionNumber.progress = currentQuestion
        Log.d("value", pbQuestionNumber.progress.toString())
        tvQuestionNumber.text = (currentQuestion + 1).toString() + "/" + quizQuestions.size
        tvQuizQuestion.text = quizQuestions[currentQuestion].questions
        selectedAnswer = ""

        opt1.apply {
            setBackgroundResource(R.drawable.radius_theme_blue)
            setTextColor(Color.WHITE)
        }
        opt2.apply {
            setBackgroundResource(R.drawable.radius_theme_blue)
            setTextColor(Color.WHITE)
        }
        opt3.apply {
            setBackgroundResource(R.drawable.radius_theme_blue)
            setTextColor(Color.WHITE)
        }
        opt4.apply {
            setBackgroundResource(R.drawable.radius_theme_blue)
            setTextColor(Color.WHITE)
        }
        opt1.text = quizQuestions[currentQuestion].opt1
        opt2.text = quizQuestions[currentQuestion].opt2
        opt3.text = quizQuestions[currentQuestion].opt3
        opt4.text = quizQuestions[currentQuestion].opt4
    }

}