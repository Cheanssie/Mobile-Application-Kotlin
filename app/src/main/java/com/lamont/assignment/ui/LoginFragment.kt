package com.lamont.assignment.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.ModuleActivity
import com.lamont.assignment.R
import com.lamont.assignment.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.registerButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.registerFragment)
        }

        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences =
            requireActivity().getSharedPreferences("SHARE_PREF", Context.MODE_PRIVATE)
        val db = FirebaseFirestore.getInstance()
        var username = sharedPreferences!!.getString("username", null)
        var password = sharedPreferences!!.getString("password", null)

            if (username != null && password != null) {
                enterModuleActivity()
            }

        binding.loginButton.setOnClickListener {
            username = binding.etUsername.text.toString()
            password = binding.etPassword.text.toString()

            db.collection("users")
                .document(username!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        when {
                            password.toString() != document.data?.get("password").toString() -> {
                                Toast.makeText(requireContext(), "Login fail", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            else -> {
                                val sharedPreferencesEditor = sharedPreferences!!.edit()
                                sharedPreferencesEditor.putString("username", username)
                                sharedPreferencesEditor.putString("password", password)
                                sharedPreferencesEditor.commit()

                                Toast.makeText(
                                    requireContext(),
                                    "Login Successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                enterModuleActivity()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Username not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }

    }

    private fun enterModuleActivity() {
        val intent = Intent(requireContext(), ModuleActivity::class.java)
        context?.startActivity(intent)
        activity?.finish()
    }


}