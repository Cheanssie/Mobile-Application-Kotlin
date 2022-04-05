package com.lamont.assignment.ui

import android.app.DatePickerDialog
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
import com.lamont.assignment.ModuleActivity
import com.lamont.assignment.R
import com.lamont.assignment.databinding.FragmentRegisterBinding
import com.lamont.assignment.model.User
import java.text.SimpleDateFormat
import java.util.*

class RegisterFragment : Fragment(){

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    lateinit var sharedPreferences : SharedPreferences

    companion object {
        const val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        const val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences("SHARE_PREF", Context.MODE_PRIVATE)
        binding.loginButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.loginFragment)
        }

        binding.registerButton.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString().lowercase()
            val password = binding.etPassword.text.toString()
            val conPassword = binding.etConPassword.text.toString()
            val phone = binding.etPhone.text.toString()
            val birthdate = binding.etDob.text.toString()

            binding.etEmail.setText(binding.etEmail.text.toString().lowercase())
            if (username != "" && email != "" && password != "" && conPassword != "" && phone != "" && birthdate != "") {
                addUser(username, email, password, conPassword, phone, birthdate)
            }  else {
                Toast.makeText(requireContext(), "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            }
        }

        val systemCal = Calendar.getInstance()
        val year = systemCal.get(Calendar.YEAR)
        val month = systemCal.get(Calendar.MONTH)
        val day = systemCal.get(Calendar.DAY_OF_MONTH)

        binding.etDob.setOnClickListener {
            DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener{ view, mYear, mMonth, mDay ->
                binding.etDob.setText("$mDay/$mMonth/$mYear")
            }, year, month, day).show()
        }
    }

    fun addUser(username:String, email:String, password:String, conPassword:String, phone:String, dob:String) {
        val db = FirebaseFirestore.getInstance()
        val dbAuth = FirebaseAuth.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener {
                val birthdate = SimpleDateFormat("dd/MM/yyyy").parse(dob.toString())
                val age = (Date().time - birthdate.time)/(31556952000)
                val user: User = User(username, email, phone, dob)
                var error = false

                for (doc in it) {
                    when {
                        username.toString() == doc.data.get("username").toString() -> {
                            Toast.makeText(requireContext(), "Username existed", Toast.LENGTH_SHORT).show()
                            error = true
                            break
                        }
                        email.toString() == doc.data.get("email").toString() -> {
                            Toast.makeText(requireContext(), "Email existed", Toast.LENGTH_SHORT).show()
                            error = true
                            break
                        }
                        !email.matches(emailPattern.toRegex()) -> {
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
                        password.toString() != conPassword.toString() -> {
                            Toast.makeText(requireContext(), "Password does not match", Toast.LENGTH_SHORT).show()
                            error = true
                            break
                        }
                        !password.matches(passwordPattern.toRegex()) -> {
                            Toast.makeText(requireContext(), "Password must contains 8 characters with at least one special character, one capital letter and one small letter", Toast.LENGTH_SHORT).show()
                            error = true
                            break
                        }
                        age < 12 -> {
                            Toast.makeText(requireContext(), "Underage", Toast.LENGTH_SHORT).show()
                            error = true
                            break
                        }
                        else -> {
                            error = false
                        }
                    }
                }

                if(!error) {
                    dbAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            db.collection("users").document(dbAuth.currentUser?.uid!!)
                                .set(user).addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Registered successful", Toast.LENGTH_SHORT).show()
                                    val editPref = sharedPreferences.edit()
                                    editPref.putString("email", email)
                                    editPref.putString("password", password)
                                    editPref.putString("username", username)
                                    editPref.commit()
                                    val intent = Intent(requireContext(), ModuleActivity::class.java)
                                    context?.startActivity(intent)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}