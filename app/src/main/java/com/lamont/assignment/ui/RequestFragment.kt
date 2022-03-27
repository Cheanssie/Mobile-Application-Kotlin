package com.lamont.assignment.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.R
import com.lamont.assignment.databinding.FragmentRequestBinding
import com.lamont.assignment.model.Request

class RequestFragment : Fragment() {

    private var _binding: FragmentRequestBinding? = null
    private  val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestBinding.inflate(inflater, container, false)
        val sharedPreferences = this.activity?.getSharedPreferences("SHARE_PREF", Context.MODE_PRIVATE)

        binding.btnUploadImg.setOnClickListener {
           when{
               binding.ivImg.drawable == null -> {
                   showDialog()
               }
               else ->
               {
                   binding.ivImg.setImageDrawable(null)
                   binding.btnUploadImg.text = "Upload Photo"
               }
           }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSubmit.setOnClickListener {
            var validity = true
            when {
                binding.rgCategory.checkedRadioButtonId == -1 -> {
                    validity = false
                    showToast("Please select a category!")
                }
                binding.etRequestDesc.text.toString() == "" -> {
                    validity = false
                    showToast("Please describe your request!")
                }
                else -> {
                    validity = true
                }
            }

            if (validity) {
                val category: String = when(binding.rgCategory.checkedRadioButtonId) {
                    R.id.foodBeverage -> "Food&Beverage"
                    R.id.education -> "Education"
                    else -> "Other"
                }
                val description = binding.etRequestDesc.text.toString()
                val username = sharedPreferences?.getString("username", null)!!
                val request = Request(username, description, category)
                addRequest(request)

            }

        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun addRequest(request: Request) {
        val db = FirebaseFirestore.getInstance()

        db.collection("request")
            .add(request)
            .addOnSuccessListener {
                Toast.makeText(requireContext(),request.desc, Toast.LENGTH_SHORT).show()
            }

    }

    private fun showDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Select Photo")
            .setMessage("Please choose a method to upload photo.")
            .setNeutralButton("Cancel") { dialog, which->

            }
            .setNegativeButton("Gallery") { dialog, which->
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_PICK
                startActivityForResult(intent, 100)
            }
            .setPositiveButton("Camera") { dialog, which->
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, 100)

            }
            .show()


    }
}