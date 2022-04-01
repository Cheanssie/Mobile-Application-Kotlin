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
import android.widget.EditText
import android.widget.Toast
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
        sharedPreferences = requireActivity().getSharedPreferences("SHARE_PREF", Context.MODE_PRIVATE)

        db.collection("users").document(dbAuth.currentUser?.uid!!)
            .addSnapshotListener { doc, error ->
                doc?.let {
                    binding.tvUid.setText(getString(R.string.uid_pro,dbAuth.currentUser?.uid.toString()))
                    binding.tvUsername.setText(doc.data?.get("username").toString())
                    binding.etEmail.setText(doc.data?.get("email").toString())
                    binding.etPhone.setText(doc.data?.get("phone").toString())
                    binding.etDOB.setText(doc.data?.get("dob").toString())
                    binding.etAddress.setText(doc.data?.get("address").toString())
                }
            }

        binding.editProfile.setOnClickListener {

            if (binding.editProfile.text == getString(R.string.edit)) {
                binding.etEmail.isFocusableInTouchMode = true
                binding.etPhone.isFocusableInTouchMode = true
                binding.etAddress.isFocusableInTouchMode = true
                binding.tvUid.setOnClickListener {
                    Toast.makeText(requireContext(), "UID is not editable!", Toast.LENGTH_SHORT)
                        .show()
                }
                binding.tvUsername.setOnClickListener {
                    Toast.makeText(
                        requireContext(),
                        "Username is not editable!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.etDOB.setOnClickListener {
                    Toast.makeText(requireContext(), "DOB is not editable!", Toast.LENGTH_SHORT)
                        .show()
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
                //val address = binding.etAddress.text.toString()
                var error = false

                db.collection("users")
                    .get()
                    .addOnSuccessListener {
                        for (doc in it) {
                            if (doc.id.toString() == dbAuth.currentUser?.uid) {
                                continue
                            }
                            when {
                                email == doc.data.get("email").toString() -> {
                                    Toast.makeText(requireContext(), "Email existed", Toast.LENGTH_SHORT).show()
                                    error = true
                                    break
                                }
                                !email.matches(RegisterFragment.emailPattern.toRegex()) -> {
                                    Toast.makeText(requireContext(), "Invalid email address", Toast.LENGTH_SHORT).show()
                                    error = true
                                    break
                                }
                                phone == doc.data.get("phone").toString() -> {
                                    Toast.makeText(requireContext(), "Phone existed", Toast.LENGTH_SHORT).show()
                                    error = true
                                    break
                                }
                                phone.length > 11 || phone.length < 10 -> {
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
                            dbAuth.currentUser?.updateEmail(user["email"]!!.toString())
                            Toast.makeText(requireContext(), "Update Successful", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                    binding.etEmail.setText(binding.etEmail.text.toString().lowercase())
                }
            }
        }

        binding.logout.setOnClickListener {
            if (binding.logout.text == "Logout") {
                dbAuth.signOut()
                val editPref = sharedPreferences.edit()
                editPref.remove("email")
                editPref.remove("password")
                editPref.remove("username")
                editPref.commit()
                val intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
            }
        }
        binding.changePassword.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.reset_password, null, false)
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Reset Password")
                .setView(dialogView)
                .setNeutralButton("Cancel") { dialog, which -> }
                .setPositiveButton("Confirm", null)
                .show()
            dialog.getButton(AlertDialog. BUTTON_POSITIVE)
                .setOnClickListener {
                    val password = dialogView.findViewById<EditText>(R.id.etOldPswd).text.toString()
                    val newPswd = dialogView.findViewById<EditText>(R.id.etNewPswd).text.toString()
                    val conNewPswd = dialogView.findViewById<EditText>(R.id.etConNewPswd).text.toString()

                    if (password != sharedPreferences.getString("password", null).toString()) {
                        Toast.makeText(requireContext(), "Wrong Old Password", Toast.LENGTH_SHORT).show()
                    } else if (newPswd != conNewPswd) {
                        Toast.makeText(requireContext(), "New Password not Match", Toast.LENGTH_SHORT).show()
                    } else if (!newPswd.matches(RegisterFragment.passwordPattern.toRegex())) {
                        Toast.makeText(requireContext(), "Please enter 8 characters with at least a special character, capital letter, and small letter", Toast.LENGTH_SHORT).show()
                    } else if (newPswd == password) {
                        Toast.makeText(requireContext(), "Old Password Same With New Password", Toast.LENGTH_SHORT).show()
                    } else {
                        val user = mapOf<String, Any>(
                            "password" to newPswd
                        )
                        db.collection("users").document(dbAuth.currentUser?.uid!!).update(user)
                            .addOnSuccessListener {
                                dbAuth.currentUser?.updatePassword(newPswd)
                                dialog.dismiss()
                                sharedPreferences.edit().putString("password", newPswd).commit()
                                Toast.makeText(requireContext(), "Password Update Successful", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }
    }
}