package com.lamont.assignment.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lamont.assignment.ModuleActivity
import com.lamont.assignment.R
import com.lamont.assignment.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db : FirebaseFirestore
    private lateinit var dbAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        Log.d("Tag", "LoginFragment.onCreateView() has been called.")

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Tag", "LoginFragment.onViewCreated() has been called.")

        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.share_pref), Context.MODE_PRIVATE)
        db = FirebaseFirestore.getInstance()
        dbAuth = FirebaseAuth.getInstance()

        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)

        if (email != null && password != null) {
            enterModuleActivity()
        }

        binding.registerButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.registerFragment)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email != "" && password != "") {
                dbAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        db.collection("users").document(dbAuth.currentUser!!.uid)
                            .get()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), getString(R.string.loginSuccess), Toast.LENGTH_SHORT).show()
                                val editPref = sharedPreferences.edit()
                                editPref.putString("email", email)
                                editPref.putString("password", password)
                                editPref.putString("username", it.data!!["username"].toString())
                                editPref.commit()
                                enterModuleActivity()
                            }

                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), getString(R.string.incorrectLoginMsg), Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), getString(R.string.requireFillIn), Toast.LENGTH_SHORT).show()
            }
        }

        binding.forgotPassword.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            val emailLayout = layoutInflater.inflate(R.layout.cutom_edit_text, null, false)
            dialog
                .setTitle(getString(R.string.resetPassword))
                .setMessage(getString(R.string.enterEmailResetPswd))
                .setView(emailLayout)
                .setNeutralButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                    val emailInput = emailLayout.findViewById<EditText>(R.id.etEmail)
                    db.collection("users")
                        .whereEqualTo("email", emailInput.text.toString())
                        .get()
                        .addOnSuccessListener {
                            if(!it.isEmpty) {
                                dbAuth.sendPasswordResetEmail(emailInput.text.toString())


                                Toast.makeText(requireContext(), getString(R.string.chkEmailPswd), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.emailNotExist), Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                }
                .show()
        }

    }

    override fun onDestroyView() {
        _binding = null
        Log.d("Tag", "LoginFragment.onDestroyView() has been called.")
        super.onDestroyView()
    }

    private fun enterModuleActivity() {
        val intent = Intent(requireContext(), ModuleActivity::class.java)
        context?.startActivity(intent)
        activity?.finish()
    }


}