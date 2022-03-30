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
                binding.tvUsername.text = it.get("username").toString()
                binding.tvEmail.text = getString(R.string.email_address_pro, it.get("email").toString())
                binding.tvPhone.text = getString(R.string.phone_pro, it.get("phone").toString())
                binding.tvDOB.text = getString(R.string.dob_pro, it.get("dob").toString())
                binding.tvAddress.text = getString(R.string.address_pro, it.get("address").toString())
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }

        binding.editProfile.setOnClickListener {
            binding.tvUid.isFocusableInTouchMode = true
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