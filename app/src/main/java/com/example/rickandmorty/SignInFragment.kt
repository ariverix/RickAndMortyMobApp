package com.example.rickandmorty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.rickandmorty.databinding.FragmentSignInBinding

class SignInFragment : BaseFragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHelper
    private val args: SignInFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        logEvent("onCreateView() вызван")
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logEvent("onViewCreated() вызван")

        dbHelper = DBHelper(requireContext())

        val emailEdit = binding.editTextTextEmailAddress
        val passEdit = binding.editTextTextPassword
        val userInfoText = binding.userInfoText

        val navController = findNavController()

        binding.buttonLog.setOnClickListener {
            val email = emailEdit.text.toString()
            val password = passEdit.text.toString()
            logEvent("Попытка входа: email=$email")

            if (dbHelper.checkUser(email, password)) {
                logEvent("Вход успешен")
                Toast.makeText(requireContext(), "Вход выполнен", Toast.LENGTH_SHORT).show()
                val action = SignInFragmentDirections.actionSignInFragmentToHomeFragment()
                navController.navigate(action)
            } else {
                logEvent("Вход неуспешен")
                Toast.makeText(requireContext(), "Неверный email или пароль", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerLink.setOnClickListener {
            logEvent("Переход на регистрацию")
            navController.navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment())
        }

        binding.arrowBackLog.setOnClickListener {
            logEvent("Возврат назад")
            navController.popBackStack()
        }

        displayUserInfo(userInfoText, args)
    }

    private fun displayUserInfo(targetView: TextView, navArgs: SignInFragmentArgs) {
        val userName = navArgs.userName
        val userEmail = navArgs.userEmail
        val userObject = navArgs.userObject

        logEvent("Получены данные: name=$userName, email=$userEmail, user=$userObject")

        val displayName = userName ?: userObject?.name
        val displayEmail = userEmail ?: userObject?.email

        if (!displayName.isNullOrEmpty() && !displayEmail.isNullOrEmpty()) {
            targetView.text = "Пользователь: $displayName\nEmail: $displayEmail"
            targetView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logEvent("onDestroyView() вызван")
        _binding = null
    }
}
