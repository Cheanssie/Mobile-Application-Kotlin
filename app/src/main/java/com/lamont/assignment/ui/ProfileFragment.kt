package com.lamont.assignment.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.MainActivity
import com.lamont.assignment.R
import com.lamont.assignment.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    var _binding : FragmentProfileBinding? = null
    val binding get() = _binding!!
    lateinit var sharedPreferences: SharedPreferences
    lateinit var db : FirebaseFirestore
    lateinit var dbAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        dbAuth = FirebaseAuth.getInstance()

        db.collection("users").document(dbAuth.currentUser?.uid!!)
            .get()
            .addOnSuccessListener {
                binding.tvUid.setText(getString(R.string.uid_pro,dbAuth.currentUser?.uid.toString()))
                binding.tvUsername.setText(it.get("username").toString())
                binding.etEmail.setText(it.get("email").toString())
                binding.etPhone.setText(it.get("phone").toString())
                binding.etDOB.setText(it.get("dob").toString())
                binding.etAddress.setText(it.get("address").toString())
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }

        binding.editProfile.setOnClickListener {

            if (binding.editProfile.text == getString(R.string.edit)) {
                binding.etEmail.isFocusableInTouchMode = true
                binding.etPhone.isFocusableInTouchMode = true
                binding.etAddress.isFocusableInTouchMode = true
                binding.tvUid.setOnClickListener {
                    Toast.makeText(requireContext(), "UID is not editable!", Toast.LENGTH_SHORT).show()
                }
                binding.tvUsername.setOnClickListener {
                    Toast.makeText(requireContext(), "Username is not editable!", Toast.LENGTH_SHORT).show()
                }
                binding.etDOB.setOnClickListener {
                    Toast.makeText(requireContext(), "DOB is not editable!", Toast.LENGTH_SHORT).show()
                }
                binding.editProfile.text = "Save"
            } else {
                binding.etEmail.isFocusableInTouchMode = false
                binding.etPhone.isFocusableInTouchMode = false
                binding.etAddress.isFocusableInTouchMode = false
                binding.etEmail.isFocusable = false
                binding.etPhone.isFocusable = false
                binding.etAddress.isFocusable = false
                binding.editProfile.text = getString(R.string.edit)

                val db = FirebaseFirestore.getInstance()
                val dbAuth = FirebaseAuth.getInstance()

                val email = binding.etEmail.text.toString()
                val phone = binding.etPhone.text.toString()
                val address = binding.etAddress.text.toString()
                var error = false

                db.collection("users")
                    .get()
                    .addOnSuccessListener {
                        for (doc in it) {
                           if(doc.id.toString() == dbAuth.currentUser?.uid) {
                               continue
                           }
                            when {
                                email.toString() == doc.data.get("email").toString() -> {
                                    Toast.makeText(requireContext(), "Email existed", Toast.LENGTH_SHORT).show()
                                    error = true
                                    break
                                }
                                !email.matches(RegisterFragment.emailPattern.toRegex()) -> {
                                    Toast.makeText(requireContext(), "Invalid email address",Toast.LENGTH_SHORT).show()
                                    error = true
                                    break
                                }
                                phone.toString() == doc.data.get("phone").toString() -> {
                                    Toast.makeText(requireContext(), "Phone existed", Toast.LENGTH_SHORT).show()
                                    error = true
                                    break
                                }
                                phone.toString().length > 11 || phone.toString().length < 10-> {
                                    Toast.makeText(requireContext(), "Phone Invalid", Toast.LENGTH_SHORT).show()
                                    error = true
                                    break
                                }
                                else -> {
                                    error = false
                                }
                            }
                        }
                    }
                if (!error) {
                    val user = mapOf<String, Any>(
                        "email" to binding.etEmail.text.toString(),
                        "phone" to binding.etPhone.text.toString(),
                        "address" to binding.etAddress.text.toString()
                    )

                    db.collection("users").document(dbAuth.currentUser?.uid!!).update(user)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Update Successful", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }



        sharedPreferences = requireActivity().getSharedPreferences("SHARE_PREF", Context.MODE_PRIVATE)
        binding.logout.setOnClickListener {
            val dbAuth = FirebaseAuth.getInstance()
            dbAuth.signOut()
            val editPref = sharedPreferences.edit()
            editPref.remove("email")
            editPref.remove("password")
            editPref.commit()
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
        }

    }

}