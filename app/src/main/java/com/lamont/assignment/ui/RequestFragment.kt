package com.lamont.assignment.ui

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
import com.lamont.assignment.databinding.FragmentRequestBinding
import com.lamont.assignment.model.Request
import com.lamont.assignment.viewModel.RequestViewModel
import kotlinx.coroutines.currentCoroutineContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class RequestFragment : Fragment() {

    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences : SharedPreferences
    lateinit var currentPhotoPath: String

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
        Log.d("Tag", "RequestFragment.onCreateView() has been called.")
        _binding = FragmentRequestBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Tag", "RequestFragment.onViewCreated() has been called.")

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
                val username = sharedPreferences.getString("username", null)!!
                val ownerId = dbAuth.currentUser!!.uid
                val formatter = SimpleDateFormat("yy_MM_dd_HH_mm_ss", Locale.getDefault())

                val request = Request(null, ownerId, username, description, category, null, null, formatter.format(Date()))
                addRequest(request)
            }
        }

    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun addRequest(request: Request) {

        db.collection("request")
            .add(request)
            .addOnSuccessListener { requestResult ->
                RequestViewModel.updateId(requestResult.id)
                val storageFolderRef = storageRef.reference.child("images/${request.owner}_${request.createdDate}")
                if(imgBit != null) {
                    val baos = ByteArrayOutputStream()
                    imgBit!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    storageFolderRef.putBytes(data)
                        .addOnSuccessListener {
                            storageFolderRef.downloadUrl.addOnSuccessListener {
                                RequestViewModel.updateImgUri(requestResult.id, it)
                            }
                        }
                } else if(imgUri != null) {
                    storageFolderRef.putFile(imgUri!!)
                        .addOnSuccessListener {
                            storageFolderRef.downloadUrl.addOnSuccessListener {
                                RequestViewModel.updateImgUri(requestResult.id, it)
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
                var photoFile: File? = null
                photoFile = createImageFile()
                val photoUri:Uri = FileProvider.getUriForFile(
                    activity?.applicationContext!!,
                    "com.example.android.fileprovider", photoFile!!
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)

            }
            .show()
    }

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

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_REQUEST_CODE) {
                imgUri = data?.data!!
                binding.ivImg.setImageURI(imgUri)
                binding.btnUploadImg.text = getString(R.string.remove)
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                imgBit = BitmapFactory.decodeFile(currentPhotoPath)
                binding.ivImg.setImageBitmap(imgBit)
                binding.btnUploadImg.text = getString(R.string.remove)
            }
        }
    }

}