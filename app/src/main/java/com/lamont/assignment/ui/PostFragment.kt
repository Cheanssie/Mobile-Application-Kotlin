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
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lamont.assignment.R
import com.lamont.assignment.databinding.FragmentPostBinding
import com.lamont.assignment.model.Post
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PostFragment : Fragment() {

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences : SharedPreferences
    lateinit var currentPhotoPath: String

    //Img Uri
    private var imgUri: Uri? = null
    private var videoUri: Uri? = null
    private var  imgBit : Bitmap? = null

    //database
    private lateinit var db : FirebaseFirestore
    private lateinit var dbAuth : FirebaseAuth
    private lateinit var storageRef : FirebaseStorage

    companion object {
        const val IMAGE_REQUEST_CODE = 100
        const val CAMERA_REQUEST_CODE = 200
        const val VIDEO_REQUEST_CODE = 300
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

        //Declaring necessary variables for data access
        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.share_pref), Context.MODE_PRIVATE)
        db = FirebaseFirestore.getInstance()
        dbAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()

        //When image upload imageButton is clicked, show dialog to prompt for method of insert image
        //Methods are taking photo using camera or from gallery
        binding.btnImg.setOnClickListener {
            imageUploadDialog()
        }

        //Turn on camera to capture a maximum of 5 seconds video
        binding.btnVideo.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5)
            startActivityForResult(intent, VIDEO_REQUEST_CODE)
        }

        //Remove the image/video from the view
        binding.btnRemove.setOnClickListener {
            binding.ivVideo.setVideoURI(null)
            binding.ivImg.setImageDrawable(null)
            imgBit = null
            imgUri = null
            videoUri = null
            it.visibility = View.GONE
            binding.ivVideo.visibility = View.GONE
            binding.btnVideo.visibility = View.VISIBLE
            binding.btnImg.visibility = View.VISIBLE

        }

        //Cancel to post a forum, return to previous fragment
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        //Submit post, add into database
        //Only succeed if description is not empty
        binding.btnSubmit.setOnClickListener {
            val validity: Boolean
            when {
                binding.etDesc.text.toString() == "" -> {
                    validity = false
                    showToast("Please describe your request!")
                }
                else -> {
                    validity = true
                }
            }

            if (validity) {
                val forumDesc = binding.etDesc.text.toString()
                val username = sharedPreferences.getString("username", null)!!
                val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                db.collection("users").document(dbAuth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener { doc->
                        //Obtaining user profile picture URI, to ease in showing out in post recycler view
                        storageRef.reference.child("profile/${doc.data?.get("imgName")}").downloadUrl
                            .addOnSuccessListener {
                                val post = Post(null.toString(), it.toString(), username, forumDesc, null, null, formatter.format(
                                    Date()), dbAuth.currentUser!!.uid)
                                addPost(post)
                            }
                            .addOnFailureListener {
                                val post = Post(null.toString(), null, username, forumDesc, null, null, formatter.format(
                                    Date()), dbAuth.currentUser!!.uid)
                                addPost(post)
                            }
                    }
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    //Add post with checking of existence of image/video, only upload exist content
    private fun addPost(post: Post) {
        db.collection("post")
            .add(post)
            .addOnSuccessListener { postResult->
                db.collection("post")
                    .document(postResult.id)
                    .update("postId", postResult.id)
                val storageFolderRef = storageRef.reference.child("post/${post.postOwner}_${post.createdDate}")
                if(imgBit != null) {
                    val baos = ByteArrayOutputStream()
                    imgBit!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    storageFolderRef.putBytes(data)
                        .addOnSuccessListener {
                            storageFolderRef.downloadUrl
                                .addOnSuccessListener {
                                    db.collection("post")
                                        .document(postResult.id)
                                        .update("imgUri", it.toString())
                                }
                        }
                } else if(imgUri != null) {
                    storageFolderRef.putFile(imgUri!!)
                        .addOnSuccessListener {
                            storageFolderRef.downloadUrl
                                .addOnSuccessListener {
                                    db.collection("post")
                                        .document(postResult.id)
                                        .update("imgUri", it.toString())
                                }
                        }
                } else if (videoUri != null) {
                    storageFolderRef.putFile(videoUri!!)
                        .addOnSuccessListener {
                            storageFolderRef.downloadUrl
                                .addOnSuccessListener {
                                    db.collection("post")
                                        .document(postResult.id)
                                        .update("videoUri", it.toString())
                                }
                        }
                }

                Toast.makeText(requireActivity().applicationContext, getString(R.string.uploadSuccess), Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                Toast.makeText(requireActivity().applicationContext, getString(R.string.uploadFail), Toast.LENGTH_SHORT).show()
            }
    }

    //Function for image upload dialog
    private fun imageUploadDialog() {
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
                var photoFile: File? = null
                photoFile = createImageFile()
                val photoUri: Uri = FileProvider.getUriForFile(
                    activity?.applicationContext!!,
                    "com.example.android.fileprovider", photoFile!!
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }
            .show()
    }

    //Function to obtain the real size of image while not thumbnail only
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_" + timeStamp + "_"
        val storageDir: File? = activity?.applicationContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentPhotoPath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_REQUEST_CODE) {
                imgUri = data?.data!!
                binding.ivImg.setImageURI(imgUri)
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                imgBit = BitmapFactory.decodeFile(currentPhotoPath)
                binding.ivImg.setImageBitmap(imgBit)
            } else if(requestCode == VIDEO_REQUEST_CODE) {
                videoUri = data?.data
                binding.ivVideo.setVideoURI(videoUri)
                binding.ivVideo.visibility = View.VISIBLE
                binding.ivVideo.start()
            }
            binding.btnVideo.visibility = View.GONE
            binding.btnImg.visibility = View.GONE
            binding.btnRemove.visibility = View.VISIBLE
        }
    }
}