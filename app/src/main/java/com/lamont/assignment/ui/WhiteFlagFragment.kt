package com.lamont.assignment.ui

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
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
    lateinit var requestAdapter : RequestAdapter
    lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWhiteFlagBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        val requestModel = RequestViewModel()
        requestAdapter = RequestAdapter(requireContext())
        binding.requestRecycler.adapter = requestAdapter

        requestModel.requestList.observe(requireActivity(), Observer {
            requestAdapter.setData(it)
        })


        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            binding.requestRecycler.adapter = requestAdapter
        }

        requestAdapter.onItemClickListner(object: RequestAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                Toast.makeText(requireContext(), requestModel.requestList.value?.get(position)!!.desc, Toast.LENGTH_SHORT).show()
                val item = requestModel.requestList.value!!.get(position)!!.copy()
//                item.donor = activity?.getSharedPreferences("SHARE_PREF", Context.MODE_PRIVATE)!!.getString("username", null).toString()
//                item.status = 2
//                updateStatus(item.requestId!!, item.status, item.donor!!)
            }

        })

        // Inflate the layout for this fragment
        return binding.root
    }

    fun updateStatus(requestId: String, status: Int, donor: String) {
        var request = mapOf<String, Any>(
            "status" to status,
            "donor" to donor
        )

        db.collection("request")
            .document(requestId)
            .update(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
