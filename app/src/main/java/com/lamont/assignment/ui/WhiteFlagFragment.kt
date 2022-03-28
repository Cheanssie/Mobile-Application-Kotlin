package com.lamont.assignment.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.firestore.*
import com.lamont.assignment.adapter.RequestAdapter
import com.lamont.assignment.data.RequestSource
import com.lamont.assignment.databinding.FragmentWhiteFlagBinding
import com.lamont.assignment.model.Request
import javax.sql.DataSource

class WhiteFlagFragment : Fragment() {

    private var _binding: FragmentWhiteFlagBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWhiteFlagBinding.inflate(inflater, container, false)

        val db = FirebaseFirestore.getInstance()
        val requestList = arrayListOf<Request>()

        db.collection("request")
            .get()
            .addOnSuccessListener{
                for (doc in it) {
                    val name = doc.get("name").toString()
                    val desc= doc.get("desc").toString()
                    val category = doc.get("category").toString()
                    val imgName = doc.get("imgName").toString()

                    val request = Request(name, desc, category, imgName)
                    requestList.add(request)
                }
                binding.requestRecycler.adapter = RequestAdapter(requireContext(), requestList)
                }




        // Inflate the layout for this fragment
        return binding.root
    }

}
