package com.lamont.assignment.ui

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.FirebaseStorageKtxRegistrar
import com.google.firebase.storage.ktx.storage
import com.lamont.assignment.R
import com.lamont.assignment.databinding.FragmentRequestBinding
import com.lamont.assignment.model.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class RequestFragment : Fragment() {

    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!

    //Img Uri
    lateinit var imgUri: Uri
    lateinit var  imgBit : Bitmap

    companion object {
        const val IMAGE_REQUEST_CODE = 100
        const val CAMERA_REQUEST_CODE = 200
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestBinding.inflate(inflater, container, false)
        val sharedPreferences =
            this.activity?.getSharedPreferences("SHARE_PREF", Context.MODE_PRIVATE)

        binding.btnUploadImg.setOnClickListener {
            when {
                binding.ivImg.drawable == null -> {
                    showDialog()
                }
                else -> {
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
                val category: String = when (binding.rgCategory.checkedRadioButtonId) {
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
        val formatter = SimpleDateFormat("yy_MM_dd_HH_mm_ss", Locale.getDefault())
        val imgName = formatter.format(Date())
        val storageRef = FirebaseStorage.getInstance().reference.child("images/$imgName")

        db.collection("request")
            .add(request)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Upload Successful", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Upload Fail", Toast.LENGTH_SHORT).show()
            }

        if(imgBit != null) {
            val baos = ByteArrayOutputStream()
            imgBit.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            storageRef.putBytes(data)
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "FirebaseStorage API Error", Toast.LENGTH_SHORT).show()
                }

        } else if(imgUri != null) {
            storageRef.putFile(imgUri)
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "FirebaseStorage API Error", Toast.LENGTH_SHORT).show()
                }
        }


    }

    private fun showDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Select Photo")
            .setMessage("Please choose a method to upload photo.")
            .setNeutralButton("Cancel") { dialog, which ->

            }
            .setNegativeButton("Gallery") { dialog, which ->
//                val intent = Intent()
//                intent.type = "image/*"
//                intent.action = Intent.ACTION_PICK
//                startActivityForResult(intent, 100)
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_REQUEST_CODE)
            }
            .setPositiveButton("Camera") { dialog, which ->
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)

            }
            .show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_REQUEST_CODE) {
                imgUri = data?.data!!
                binding.ivImg.setImageURI(imgUri)
                binding.btnUploadImg.text = "Remove"
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                imgBit = data?.extras?.get("data")!! as Bitmap
                binding.ivImg.setImageBitmap(imgBit)
                binding.btnUploadImg.text = "Remove"
            }
        }
    }
}