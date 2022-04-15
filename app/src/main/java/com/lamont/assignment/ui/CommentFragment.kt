package com.lamont.assignment.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.R
import com.lamont.assignment.adapter.CommentAdapter
import com.lamont.assignment.databinding.FragmentCommentBinding
import com.lamont.assignment.model.Comment
import com.lamont.assignment.viewModel.CommentViewModel
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class CommentFragment : Fragment() {

    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!
    private lateinit var commentAdapter : CommentAdapter
    lateinit var dbAuth : FirebaseAuth
    lateinit var db : FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("Tag", "CommentFragment.onCreateView() has been called.")
        _binding = FragmentCommentBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        //Remove the visibility of the floating action button
        activity?.findViewById<FloatingActionButton>(R.id.addPost)?.hide()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Tag", "CommentFragment.onViewCreated() has been called.")


        //Declaring necessary variable for data access
        db = FirebaseFirestore.getInstance()
        dbAuth = FirebaseAuth.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.share_pref), Context.MODE_PRIVATE)

        //Obtaining the particular post's information
        val postId = arguments?.get("postId").toString()
        val imgUri = arguments?.get("imgUri").toString()
        val videoUri = arguments?.get("videoUri").toString()
        val ownerName = arguments?.get("ownerName").toString()
        val forumDesc = arguments?.get("forumDesc").toString()
        val ivProfile = arguments?.get("ivProfile").toString()
        val dateTime = arguments?.get("dateTime").toString()

        //Placing the data into respective view
        binding.postOwner.text = ownerName
        binding.forumDesc.text = forumDesc
        binding.postDateTime.text = dateTime
        Picasso.with(requireContext()).load(ivProfile).into(binding.ivProfile)

        //Declaring comment view model and recycle view
        val commentModel = CommentViewModel()
        commentAdapter = CommentAdapter(requireContext())
        binding.commentRecycler.adapter = commentAdapter

        //Observe live data, update if there is any change
        commentModel.loadCommentList().observe(requireActivity(), Observer {
            val filteredComment = mutableListOf<Comment>()
            it.forEach {
                if (it.postId == postId) {
                    filteredComment.add(it)
                }
            }
            commentAdapter.setData(filteredComment)
        })

        //Send comment when comment is written
        binding.btnSend.setOnClickListener {
            val validity: Boolean
            when {
                binding.etComment.text.toString() == "" -> {
                    validity = false
                    Toast.makeText(requireContext(), getString(R.string.writeComment), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    validity = true
                }
            }
            if (validity) {
                val formatter = SimpleDateFormat("yy_MM_dd_HH_mm_ss", Locale.getDefault())
                val comment = Comment(postId, sharedPreferences.getString("username", null).toString(), binding.etComment.text.toString(), formatter.format(Date()))
                CommentViewModel.addComment(comment)
                binding.etComment.text.clear()
            }
        }

        //Checking the content height, if exceeds a value, show more/less feature is activated
        binding.forumDesc.measure(0, 0)
        if (binding.forumDesc.measuredHeight > 200) {
            binding.showMode.visibility = View.VISIBLE
        }

        //Checking existence of image or video, if exist the view will be visible, else vice versa
        if (imgUri != "null") {
            Picasso.with(requireContext()).load(imgUri).into(binding.postImg)
            binding.showMode.visibility = View.VISIBLE
        } else if (videoUri != "null") {
            binding.showMode.visibility = View.VISIBLE
            val player: ExoPlayer
            player = ExoPlayer.Builder(requireContext()).build()
            binding.postVideo.player = player
            val mediaItem = MediaItem.fromUri(videoUri)
            player.addMediaItem(mediaItem)
            player.repeatMode = Player.REPEAT_MODE_ONE
            player.volume = 0f
            player.playWhenReady = true
            binding.postVideo.visibility = View.VISIBLE
            player.prepare()
        }

        //Show mode feature, allow users to expand or compact the view if the content is too long
        binding.showMode.setOnClickListener {
            val showModeView = binding.showMode
            when(showModeView.text.toString()) {
                "Less..." -> {
                    showModeView.text = getString(R.string.showMore)
                    binding.forumDesc.isSingleLine = true
                    if (imgUri != "null") {
                        binding.postImg.visibility = View.GONE
                    } else if (videoUri != "null") {
                        binding.postVideo.visibility = View.GONE
                    }
                }
                "More..." -> {
                    showModeView.text = getString(R.string.showLess)
                    binding.forumDesc.isSingleLine = false
                    if (imgUri != "null") {
                        binding.postImg.visibility = View.VISIBLE
                    } else if (videoUri != "null") {
                        binding.postVideo.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //When comment fragment is destroyed, floating action button appears
        activity?.findViewById<FloatingActionButton>(R.id.addPost)?.show()
    }
}