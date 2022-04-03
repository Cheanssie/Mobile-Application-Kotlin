package com.lamont.assignment.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.lamont.assignment.adapter.RequestAdapter
import com.lamont.assignment.data.RequestSource
import com.lamont.assignment.databinding.FragmentWhiteFlagBinding
import com.lamont.assignment.model.Request
import com.lamont.assignment.viewModel.RequestViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.sql.DataSource

class WhiteFlagFragment : Fragment() {

    private var _binding: FragmentWhiteFlagBinding? = null
    private val binding get() = _binding!!
    private val requestAdapter = RequestAdapter()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWhiteFlagBinding.inflate(inflater, container, false)

        val requestModel = RequestViewModel()
        binding.requestRecycler.adapter = requestAdapter

        requestModel.getRequestList().observe(requireActivity(), Observer {
            requestAdapter.setData(it)
        })


        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            binding.requestRecycler.adapter = requestAdapter
        }

        requestAdapter.onItemClickListner(object: RequestAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                Toast.makeText(requireContext(), requestModel.getRequestList().value?.get(position)!!.desc, Toast.LENGTH_SHORT).show()
            }

        })

        // Inflate the layout for this fragment
        return binding.root
    }

}
