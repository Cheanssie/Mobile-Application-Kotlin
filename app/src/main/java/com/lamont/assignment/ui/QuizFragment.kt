package com.lamont.assignment.ui

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.R
import com.lamont.assignment.adapter.QuizAdapter
import com.lamont.assignment.databinding.FragmentQuizBinding
import com.lamont.assignment.model.QuizMenu

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private lateinit var quizAdapter: QuizAdapter
    private lateinit var dbQuiz: FirebaseFirestore
    private var selectedQuiz : String? = null
    private var selectedQuizDuration: Long? = 0
    private var quizQues: MutableList<QuizMenu> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentQuizBinding.inflate(inflater, container, false)


        //Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Instantiate Firestore database
        dbQuiz = FirebaseFirestore.getInstance()

        //Collecting individual quiz data from Firestore and instantiate an object (Quiz menu)
        dbQuiz.collection("quiz")
            .get()
            .addOnSuccessListener {
                for (document in it) {
                    val title: String = document.get("title").toString()
                    val duration: Long = document.get("duration") as Long
                    val image: String = document.get("image").toString()

                    quizQues.add(QuizMenu(title, duration, image))
                }

                //Inflate layout
                quizAdapter = QuizAdapter(requireContext(),quizQues)
                binding.rvQuiz.adapter = quizAdapter
                //User will be directed to selected quiz where relevant questions will be displayed
                quizAdapter.setOnItemClickListener(object : QuizAdapter.OnItemClickListener{
                    override fun onQuizClicked(position: Int) {
                        selectedQuiz = quizQues[position].title
                        selectedQuizDuration = quizQues[position].duration
                        Toast.makeText(requireContext(),selectedQuiz, Toast.LENGTH_SHORT).show()
                        Intent(requireContext(), QuizActivity::class.java).apply {
                            //Passing data to Quiz Activity
                            putExtra("selectedQuiz", selectedQuiz)
                            putExtra("duration", selectedQuizDuration)
                            startActivity(this)
                        }
                    }

                    override fun onLeaderClicked(position: Int) {
                        selectedQuiz = quizQues[position].title
                        val bundle = Bundle()
                        bundle.putString("quiz", selectedQuiz)
                        val leaderboardFragment = LeaderboardFragment()
                        leaderboardFragment.arguments = bundle
                        childFragmentManager.beginTransaction().replace(R.id.quizMenu, leaderboardFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                })
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Firebase error.", error)
            }

        //Refreshing page
        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            binding.rvQuiz.adapter = quizAdapter
        }
    }
}