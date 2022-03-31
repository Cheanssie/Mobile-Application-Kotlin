package com.lamont.assignment.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.lamont.assignment.ModuleActivity
import com.lamont.assignment.R
import com.lamont.assignment.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    lateinit var sharedPreferences: SharedPreferences
    lateinit var db : FirebaseFirestore
    lateinit var dbAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences("SHARE_PREF", Context.MODE_PRIVATE)

        var email = sharedPreferences!!.getString("email", null)
        var password = sharedPreferences!!.getString("password", null)

        if (email != null && password != null) {
            enterModuleActivity()
        }

        binding.registerButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.registerFragment)
        }
        db = FirebaseFirestore.getInstance()
        dbAuth = FirebaseAuth.getInstance()
        binding.loginButton.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email != "" && password != "") {
                dbAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        db.collection("users").document(dbAuth.currentUser!!.uid)
                            .get()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                                val editPref = sharedPreferences.edit()
                                editPref.putString("email", email)
                                editPref.putString("password", password)
                                editPref.putString("username", it.data!!["username"].toString())
                                editPref.commit()
                                enterModuleActivity()
                            }

                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Incorrect Email or Password", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun enterModuleActivity() {
        val intent = Intent(requireContext(), ModuleActivity::class.java)
        context?.startActivity(intent)
        activity?.finish()
    }


}