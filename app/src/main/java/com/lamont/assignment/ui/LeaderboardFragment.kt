package com.lamont.assignment.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.ModuleActivity
import com.lamont.assignment.adapter.LeaderboardAdapter
import com.lamont.assignment.databinding.FragmentLeaderboardBinding
import com.lamont.assignment.model.Leaderboard


class LeaderboardFragment : Fragment() {

        private var _binding: FragmentLeaderboardBinding? = null
        private val binding get() = _binding!!
        private lateinit var leaderboardAdapter : LeaderboardAdapter
        private var leaderboard : MutableList<Leaderboard> = ArrayList()
        private var points : Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        // Inflate layout for this fragment

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Instantiate Firebase database
        val db : FirebaseFirestore = FirebaseFirestore.getInstance()

        //Getting passed data from QuizResult as bundle
        val bundle = arguments
        Log.d("bundle", bundle.toString())
        val quiz : String = bundle!!.getString("quiz").toString()

        //Getting all users' points for the selected quiz to display
        db.collection("users")
            .get()
            .addOnSuccessListener { snapShot ->
                for (document in snapShot) {
                    //Resetting points in case null appears
                    points = 0
                    val username: String = document.get("username").toString()
                    val quizPoints: Map<*, *> = document.get("quiz") as Map<*, *>
                    //Check if null, no = initialize points for leaderboard view else = points = 0
                    if(quizPoints.contains(quiz)) {
                        points = quizPoints[quiz] as Long
                    }
                    leaderboard.add(Leaderboard("", username, points))
                }
                //Making sure leaderboard is displayed with correct positioning
                leaderboard.sortByDescending { leader ->
                    leader.points
                }
                //Inflate recycler views with adapter
                leaderboardAdapter = LeaderboardAdapter(requireContext(), leaderboard)
                binding.rvLeaderboard.adapter = leaderboardAdapter
            }

            //Return to main menu on click/press
            binding.leaderModuleBtn.text = "Return to Main Menu"
            binding.leaderModuleBtn.setOnClickListener{
                //Removes fragment from backstack
                activity?.let{
                    val intent = Intent(it, ModuleActivity::class.java)
                        it.startActivity(intent)
                }
            }

            //Refreshing page
            binding.swipeToRefresh.setOnRefreshListener {
                binding.swipeToRefresh.isRefreshing = false
                binding.rvLeaderboard.adapter = leaderboardAdapter
            }
    }
}