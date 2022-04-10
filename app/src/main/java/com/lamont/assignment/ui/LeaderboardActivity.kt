package com.lamont.assignment.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.lamont.assignment.R
import com.lamont.assignment.adapter.LeaderboardAdapter
import com.lamont.assignment.adapter.QuizAdapter
import com.lamont.assignment.databinding.ActivityLeaderboardBinding
import com.lamont.assignment.databinding.ActivityQuizBinding
import com.lamont.assignment.model.Leaderboard

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLeaderboardBinding
    private lateinit var leaderboardAdapter : LeaderboardAdapter
    private var leaderboard : MutableList<Leaderboard> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)




    }
}