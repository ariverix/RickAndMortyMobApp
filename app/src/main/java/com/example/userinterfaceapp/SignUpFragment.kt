package com.example.userinterfaceapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.userinterfaceapp.databinding.FragmentSignUpBinding

class SignUpFragment : BaseFragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding?: throw RuntimeException("Non-zero value was expected")
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        logEvent("onCreateView() вызван")
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logEvent("onViewCreated() вызван")

        dbHelper = DBHelper(requireContext())
        val navController = findNavController()

        binding.buttonInput.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextTextPassword.text.toString()
            val age = binding.etAge.text.toString()
            val genderId = binding.rgGender.checkedRadioButtonId
            val gender = if (genderId != -1) {
                binding.rgGender.findViewById<RadioButton>(genderId)?.text?.toString().orEmpty()
            } else {
                ""
            }

            logEvent("Попытка регистрации: name=$name, email=$email")

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || age.isEmpty() || gender.isEmpty()) {
                logEvent("Не все поля заполнены")
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    dbHelper.addUser(email, password)
                    logEvent("Пользователь добавлен в БД")
                    Toast.makeText(requireContext(), "Аккаунт создан", Toast.LENGTH_SHORT).show()

                    val user = User(name, email, password, age, gender)
                    val action = SignUpFragmentDirections.actionSignUpFragmentToSignInFragment(
                        userName = name,
                        userEmail = email,
                        userObject = user,
                    )
                    navController.navigate(action)
                } catch (e: Exception) {
                    logEvent("Ошибка регистрации: ${e.message}")
                    Toast.makeText(requireContext(), "Пользователь уже существует", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.loginLink.setOnClickListener {
            logEvent("Возврат к входу")
            navController.popBackStack()
        }

        binding.arrowBack.setOnClickListener {
            logEvent("Возврат назад")
            navController.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
