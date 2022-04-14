package com.lamont.assignment.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.adapter.RequestAdapter
import com.lamont.assignment.databinding.FragmentWhiteFlagBinding
import com.lamont.assignment.viewModel.RequestViewModel
import com.lamont.assignment.R
import com.lamont.assignment.model.Request

class WhiteFlagFragment : Fragment() {

    private var _binding: FragmentWhiteFlagBinding? = null
    private val binding get() = _binding!!
    private lateinit var requestAdapter : RequestAdapter
    lateinit var dbAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("Tag", "WFFragment.onCreateView() has been called.")
        _binding = FragmentWhiteFlagBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Tag", "WFFragment.onViewCreated() has been called.")

        dbAuth = FirebaseAuth.getInstance()
        val requestModel = RequestViewModel()
        requestAdapter = RequestAdapter(requireContext())
        binding.requestRecycler.adapter = requestAdapter

        requestModel.loadRequestList().observe(requireActivity(), Observer {
            var existRequest = false
            for (request in it) {
                if(request.ownerId == dbAuth.currentUser!!.uid || request.donorId == dbAuth.currentUser!!.uid || request.donorId == "null") {
                    existRequest = true
                    break
                }
            }
            if(!existRequest){
                binding.emptyContainer.visibility = View.VISIBLE
            } else {
                binding.emptyContainer.visibility = View.GONE
            }
            requestAdapter.setData(it)

        })

        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            binding.requestRecycler.adapter = requestAdapter
        }

        requestAdapter.onItemClickListener(object: RequestAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val item = requestModel.requestList.value!![position].copy()
                val action = binding.requestRecycler.findViewHolderForAdapterPosition(position)!!.itemView.findViewById<Button>(R.id.btnDonate).text
                val dialog = AlertDialog.Builder(requireContext())

                when(action){
                    getString(R.string.donate) -> {
                        dialog.setTitle(getString(R.string.donate))
                            .setMessage(getString(R.string.donateConfirmation))
                            .setNeutralButton(getString(R.string.cancel), null)
                            .setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                                item.donorId = dbAuth.currentUser!!.uid
                                item.status = 2
                                RequestViewModel.updateStatus(item.requestId!!, item.status)
                                RequestViewModel.updateDonor(item.requestId, item.donorId!!)
                            }.show()
                    }
                    getString(R.string.info) -> {
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
                                    intent.data = Uri.parse("mailto:$email")
                                    startActivity(Intent.createChooser(intent, "Send"))
                                }

                                val phone = it.data?.get("phone").toString()
                                infoDialogView.findViewById<TextView>(R.id.tvPhone).text = phone
                                infoDialogView.findViewById<ImageButton>(R.id.btnPhone).setOnClickListener {
                                    intent = Intent(Intent.ACTION_DIAL)
                                    intent.data = Uri.parse("tel:$phone")
                                    startActivity(intent)
                                }

                                val address = it.data?.get("address").toString()
                                if (it.data?.get("address") != "") {
                                    infoDialogView.findViewById<TextView>(R.id.tvAddress).text = address
                                    infoDialogView.findViewById<ImageButton>(R.id.btnAddress).setOnClickListener {
                                        intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$address"))
                                        startActivity(intent)
                                    }
                                } else {
                                    infoDialogView.findViewById<TextView>(R.id.tvAddress).text = getString(R.string.noAddressFound)
                                    infoDialogView.findViewById<ImageButton>(R.id.btnAddress).setOnClickListener {
                                        Toast.makeText(requireContext(), getString(R.string.nullAddressMsg), Toast.LENGTH_SHORT).show()
                                    }
                                }

                                infoDialogView.findViewById<ImageButton>(R.id.btnAddress)
                                infoDialogView.findViewById<ImageButton>(R.id.btnPhone)

                                dialog.setTitle(getString(R.string.recipientInfo))
                                    .setView(infoDialogView)
                                    .setMessage(getString(R.string.contactForInfo))
                                    .setNegativeButton(getString(R.string.close), null)
                                    .setPositiveButton(getString(R.string.donated)) { dialog, which ->
                                        RequestViewModel.updateStatus(item.requestId!!, 3)
                                    }
                                    .show()
                            }

                    }
                    getString(R.string.remove) -> {
                        dialog.setTitle(getString(R.string.rmReq))
                            .setMessage(getString(R.string.rmConfirmation))
                            .setNeutralButton(getString(R.string.cancel), null)
                            .setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                                RequestViewModel.removeRequest(item.requestId!!)
                            }.show()
                    }
                    getString(R.string.received) -> {
                        dialog.setTitle(getString(R.string.reqFulfilled))
                            .setMessage(getString(R.string.finishConfirmation))
                            .setNeutralButton(getString(R.string.cancel), null)
                            .setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                                RequestViewModel.removeRequest(item.requestId!!)
                            }.show()
                    }

                    getString(R.string.n_a) -> {
                        Toast.makeText(requireContext(), getString(R.string.otherAccpeted), Toast.LENGTH_SHORT).show()
                    }
                    getString(R.string.done) -> {
                        Toast.makeText(requireContext(), getString(R.string.pendForConfirmation), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
