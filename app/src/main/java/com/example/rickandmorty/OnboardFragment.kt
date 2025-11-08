package com.example.rickandmorty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rickandmorty.databinding.FragmentOnboardBinding

class OnboardFragment : BaseFragment() {

    private var _binding: FragmentOnboardBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("Non-zero value was expected")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        logEvent("onCreateView() вызван")
        _binding = FragmentOnboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logEvent("onViewCreated() вызван")

        binding.buttonReady.setOnClickListener {
            logEvent("Кнопка 'Готов' нажата")
            findNavController().navigate(OnboardFragmentDirections.actionOnboardFragmentToSignInFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
