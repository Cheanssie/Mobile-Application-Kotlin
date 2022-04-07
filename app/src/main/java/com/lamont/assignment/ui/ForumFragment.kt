package com.lamont.assignment.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.adapter.PostAdapter
import com.lamont.assignment.adapter.RequestAdapter
import com.lamont.assignment.databinding.FragmentForumBinding
import com.lamont.assignment.model.Post

class ForumFragment : Fragment() {

    private var _binding: FragmentForumBinding? = null
    private val binding get() = _binding!!
    private lateinit var postAdapter : PostAdapter
    lateinit var dbAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("Tag", "ForumFragment.onCreateView() has been called.")
        _binding = FragmentForumBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Tag", "ForumFragment.onViewCreated() has been called.")

        dbAuth = FirebaseAuth.getInstance()
        postAdapter = PostAdapter(requireContext())
        binding.forumRecycler.adapter = postAdapter

            var db : FirebaseFirestore = FirebaseFirestore.getInstance()
            db.collection("post").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    return@addSnapshotListener
                }
                querySnapshot?.let {
                    var postData : MutableList<Post> = mutableListOf()
                    for (document in it) {
                        val forumDesc = document.get("forumDesc").toString()
                        val postOwner = document.get("postOwner").toString()
                        val ivProfile = document.get("ivProfile").toString()
                        val postImg = document.get("postImg").toString()
                        val post = Post(null.toString(), ivProfile, postOwner, forumDesc, postImg)
                        postData.add(post)
                    }
                    postAdapter.setData(postData)
                }
            }

        postAdapter.onItemClickListner(object: PostAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {

            }
        })

        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            binding.forumRecycler.adapter = postAdapter
        }

    }

    override fun onDestroy() {
        Log.d("Tag", "ForumFragment.onDestroy() has been called.")
        _binding = null
        super.onDestroy()
    }

}