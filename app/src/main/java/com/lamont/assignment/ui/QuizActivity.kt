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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import com.lamont.assignment.R
import com.lamont.assignment.databinding.ActivityQuizBinding
import com.lamont.assignment.model.Quiz
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
    private var timesUp : Boolean = false
    private var timePoints : Long = 0
    private var totalPoints : Long = 0
    private lateinit var timer: CountDownTimer

    private val fragmentManager: FragmentManager = supportFragmentManager

    companion object {
        private const val FINAL_SEC_TO_MILLISECOND : Long = 60000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialing views to variables
        val backBtn = findViewById<ImageView>(R.id.backBtn)

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
        getQuizDuration *= FINAL_SEC_TO_MILLISECOND
        //Initialize title
        selectedQuizTitle.text = getSelectedQuiz

        //instantiate Realtime database
        val dbReal : DatabaseReference = FirebaseDatabase.getInstance("https://bewithyouth-28168-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        //Collecting data from Realtime database and creating Quiz objects
        dbReal.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot in dataSnapshot.child(getSelectedQuiz.toString()).children) {
                    val question: String = snapshot.child("question").value.toString()
                    val qOpt1: String = snapshot.child("opt1").value.toString()
                    val qOpt2: String = snapshot.child("opt2").value.toString()
                    val qOpt3: String = snapshot.child("opt3").value.toString()
                    val qOpt4: String = snapshot.child("opt4").value.toString()
                    val answer = snapshot.child("answer").value.toString()

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
                        //Points formula = Correct answers multiply time left (0)
                        totalPoints = correctAnswer * timePoints
                        //Times up flag on = different result message
                        timesUp = true
                        Intent(this@QuizActivity, QuizResult::class.java).apply{
                            putExtra("correct", correctAnswer)
                            putExtra("incorrect", quizQuestions.size - correctAnswer)
                            putExtra("timesUp", timesUp)
                            putExtra("points", totalPoints)
                            putExtra("quiz", getSelectedQuiz)
                            startActivity(this)
                            finish()
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
        return correctAnswers
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
            (currentQuestion + 1) -> nextBtn.text = getString(R.string.completeBtn)
        }

        if(currentQuestion == quizQuestions.size){
            //Cancels timer and collects time for points formula
            timer.cancel()
            //Points formula = Correct answers multiply time left in seconds
            val correctAnswer = correctAnswer()
            totalPoints = correctAnswer * timePoints
            Intent(this, QuizResult::class.java).apply {
                //Total correct and incorrect answers passed to Quiz Result activity
                putExtra("correct", correctAnswer)
                putExtra("incorrect", quizQuestions.size - correctAnswer)
                //Check if times up flag is on
                putExtra("timesUp", timesUp)
                //Get total points accumulated
                putExtra("points", totalPoints)
                //Get selected quiz topic for database and leaderboard
                putExtra("quiz", getSelectedQuiz)
                startActivity(this)
            }
            finish()
        }else{
            resetQuestions()
        }
    }

    //Displaying the question and resetting to its original state
    //Applying default theme
    @SuppressLint("SetTextI18n")
    private fun resetQuestions() {
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

    //Back button function
    override fun onBackPressed() {
        super.onBackPressed()
        //New instance of fragment so contents will not be overlapped
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val quizMenu = QuizFragment()
        fragmentTransaction.replace(R.id.quizActFrame, quizMenu)
            .addToBackStack(null)
            .commit()
        //Cancels timer for quiz
        timer.cancel()
        finish()
    }

}