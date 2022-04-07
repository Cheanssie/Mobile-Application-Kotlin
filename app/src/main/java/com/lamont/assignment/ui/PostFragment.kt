package com.lamont.assignment.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lamont.assignment.R
import com.lamont.assignment.databinding.FragmentPostBinding
import com.lamont.assignment.databinding.FragmentRequestBinding
import com.lamont.assignment.model.Post
import com.lamont.assignment.model.Request
import com.lamont.assignment.viewModel.RequestViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PostFragment : Fragment() {

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences : SharedPreferences

    //Img Uri
    private var imgUri: Uri? = null
    private var  imgBit : Bitmap? = null

    //database
    private lateinit var db : FirebaseFirestore
    private lateinit var dbAuth : FirebaseAuth
    private lateinit var storageRef : FirebaseStorage

    companion object {
        const val IMAGE_REQUEST_CODE = 100
        const val CAMERA_REQUEST_CODE = 200
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("Tag", "PostFragment.onCreateView() has been called.")
        _binding = FragmentPostBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Tag", "PostFragment.onViewCreated() has been called.")

        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.share_pref), Context.MODE_PRIVATE)
        db = FirebaseFirestore.getInstance()
        dbAuth = FirebaseAuth.getInstance()

        storageRef = FirebaseStorage.getInstance()

        binding.btnUploadImg.setOnClickListener {
            when (binding.ivImg.drawable) {
                null -> {
                    showDialog()
                }
                else -> {
                    binding.ivImg.setImageDrawable(null)
                    imgBit = null
                    imgUri = null
                    binding.btnUploadImg.text = getString(R.string.uploadPhoto)
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSubmit.setOnClickListener {
            val validity: Boolean
            when {
                binding.etRequestDesc.text.toString() == "" -> {
                    validity = false
                    showToast("Please describe your request!")
                }
                else -> {
                    validity = true
                }
            }

            if (validity) {
                val forumDesc = binding.etRequestDesc.text.toString()
                val username = sharedPreferences.getString("username", null)!!
                val formatter = SimpleDateFormat("yy_MM_dd_HH_mm_ss", Locale.getDefault())
                val postImg = "${username}_${formatter.format(Date())}" //Kae Lun_22_03_28_11_11_11
                val ivProfile = db.collection("users").document(dbAuth.currentUser?.uid!!)
                    .addSnapshotListener { doc, error ->
                        doc?.let {
                            val localFile = File.createTempFile("tempImg", "jpg")
                            FirebaseStorage.getInstance().reference.child("profile/${doc.data?.get("imgName")}").getFile(localFile)
                        }
                    }

                val post = Post(null.toString(), ivProfile.toString(), username, forumDesc, postImg)
                addPost(post)
            }
        }

    }

    override fun onDestroyView() {
        _binding = null
        Log.d("Tag", "PostFragment.onDestroyView() has been called.")
        super.onDestroyView()
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun addPost(post: Post) {

        db.collection("post")
            .add(post)
            .addOnSuccessListener {
                if(imgBit != null) {
                    val baos = ByteArrayOutputStream()
                    imgBit!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    storageRef.reference.child("post/${post.postImg}").putBytes(data)
                } else if(imgUri != null) {
                    storageRef.reference.child("post/${post.postImg}").putFile(imgUri!!)
                }
                Toast.makeText(requireActivity().applicationContext, getString(R.string.uploadSuccess), Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                Toast.makeText(requireActivity().applicationContext, getString(R.string.uploadFail), Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.selectPhoto))
            .setMessage(getString(R.string.getPhotoMethod))
            .setNeutralButton(getString(R.string.cancel)) { dialog, which ->

            }
            .setNegativeButton(getString(R.string.gallery)) { dialog, which ->
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_REQUEST_CODE)
            }
            .setPositiveButton(getString(R.string.camera)) { dialog, which ->
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)

            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_REQUEST_CODE) {
                imgUri = data?.data!!
                binding.ivImg.setImageURI(imgUri)
                binding.btnUploadImg.text = getString(R.string.remove)
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                imgBit = data?.extras?.get("data")!! as Bitmap
                binding.ivImg.setImageBitmap(imgBit)
                binding.btnUploadImg.text = getString(R.string.remove)
            }
        }
    }
}