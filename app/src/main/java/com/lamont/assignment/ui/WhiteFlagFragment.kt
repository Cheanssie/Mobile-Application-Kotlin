package com.lamont.assignment.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lamont.assignment.adapter.RequestAdapter
import com.lamont.assignment.databinding.FragmentWhiteFlagBinding
import com.lamont.assignment.viewModel.RequestViewModel
import com.lamont.assignment.R
import com.lamont.assignment.model.Request


class WhiteFlagFragment : Fragment() {

    private var _binding: FragmentWhiteFlagBinding? = null
    private val binding get() = _binding!!
    lateinit var requestAdapter : RequestAdapter
    lateinit var dbAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWhiteFlagBinding.inflate(inflater, container, false)
        dbAuth = FirebaseAuth.getInstance()
        val requestModel = RequestViewModel()
        requestAdapter = RequestAdapter(requireContext())
        binding.requestRecycler.adapter = requestAdapter

        requestModel.loadRequestList().observe(requireActivity(), Observer {
            var requestAvailable = mutableListOf<Request>()
            for (request in it) {
                if(request.ownerId == dbAuth.currentUser!!.uid || request.donorId == dbAuth.currentUser!!.uid || request.donorId == "null") {
                    requestAvailable.add(request)
                }
            }
                    requestAvailable.sortByDescending{ it.createdDate
                it.status }
            requestAdapter.setData(requestAvailable)
        })


        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            binding.requestRecycler.adapter = requestAdapter
        }

        requestAdapter.onItemClickListner(object: RequestAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                val item = requestModel.requestList.value!![position]!!.copy()
                val action = binding.requestRecycler.findViewHolderForAdapterPosition(position)!!.itemView.findViewById<Button>(R.id.btnDonate).text
                val dialog = AlertDialog.Builder(requireContext())
                when(action){
                    "DONATE" -> {
                            dialog.setTitle("Donate")
                                .setMessage("Are you sure to donate?")
                                .setNeutralButton("Cancel", null)
                                .setPositiveButton("Confirm") { dialog, which ->
                                    item.donorId = dbAuth.currentUser!!.uid
                                    item.status = 2
                                    requestModel.updateStatus(item.requestId!!, item.status)
                                    requestModel.updateDonor(item.requestId, item.donorId!!)
                                }.show()
                            Toast.makeText(requireContext(), "You have exceeded the maximum of on-hold donation", Toast.LENGTH_SHORT).show()
                    }
                    "INFO" -> {
                        val infoDialogView = layoutInflater.inflate(R.layout.recipient_information_dialog, null, false)
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users")
                            .document(item.ownerId)
                            .get()
                            .addOnSuccessListener {
                                var intent: Intent
                                val email = it.data?.get("email").toString()
                                infoDialogView.findViewById<TextView>(R.id.tvEmail).text = email
                                infoDialogView.findViewById<ImageButton>(R.id.btnEmail).setOnClickListener {
                                    intent = Intent(Intent.ACTION_SENDTO)
                                    intent.data = Uri.parse("mailto:")
                                    intent.type = "message/rfc822"
                                    intent.putExtra(Intent.EXTRA_EMAIL, email)
                                    startActivity(Intent.createChooser(intent, "Send"))
                                }

                                val phone = it.data?.get("phone").toString()
                                infoDialogView.findViewById<TextView>(R.id.tvPhone).text = phone
                                infoDialogView.findViewById<ImageButton>(R.id.btnPhone).setOnClickListener {
                                    intent = Intent(Intent.ACTION_DIAL)
                                    intent.data = Uri.parse("tel:" + phone)
                                    startActivity(intent)
                                }

                                val address = it.data?.get("address").toString()
                                if (it.data?.get("address") != "") {
                                    infoDialogView.findViewById<TextView>(R.id.tvAddress).text = address
                                    infoDialogView.findViewById<ImageButton>(R.id.btnAddress).setOnClickListener {
                                        intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + address))
                                        startActivity(intent)
                                    }
                                } else {
                                    infoDialogView.findViewById<TextView>(R.id.tvAddress).text = "No address found!"
                                    infoDialogView.findViewById<ImageButton>(R.id.btnAddress).setOnClickListener {
                                        Toast.makeText(requireContext(), "The user doesn't provide address", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                infoDialogView.findViewById<ImageButton>(R.id.btnAddress)
                                infoDialogView.findViewById<ImageButton>(R.id.btnPhone)


                                dialog.setTitle("Recipient's Information")
                                    .setView(infoDialogView)
                                    .setMessage("Contact recipient for more information")
                                    .setNegativeButton("Close", null)
                                    .show()
                            }

                    }
                    "REMOVE" -> {
                        dialog.setTitle("Remove Request")
                            .setMessage("Are you sure to remove?")
                            .setNeutralButton("Cancel", null)
                            .setPositiveButton("Confirm") { dialog, which ->
                                requestModel.removeRequest(item.requestId!!)
                            }.show()
                    }
                    "RECEIVED" -> {
                        dialog.setTitle("Request Fulfilled")
                            .setMessage("Are you sure to finish this request?")
                            .setNeutralButton("Cancel", null)
                            .setPositiveButton("Confirm") { dialog, which ->
                                requestModel.removeRequest(item.requestId!!)
                            }.show()
                    }

                    "N/A" -> {
                        Toast.makeText(requireContext(), "The request is accepted by other", Toast.LENGTH_SHORT).show()
                    }
                }

            }

        })

        // Inflate the layout for this fragment
        return binding.root
    }
}
