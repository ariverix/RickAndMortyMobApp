package com.example.rickandmorty

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rickandmorty.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        logEvent("onCreateView() вызван")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logEvent("onViewCreated() вызван")

        binding.charactersRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    URL("https://rickandmortyapi.com/api/character").readText()
                }

                val json = JSONObject(data)
                val results = json.getJSONArray("results")
                val characters = mutableListOf<Character>()

                for (i in 0 until results.length()) {
                    val char = results.getJSONObject(i)
                    characters.add(
                        Character(
                            name = char.getString("name"),
                            image = char.getString("image"),
                            status = char.getString("status"),
                            species = char.getString("species")
                        )
                    )
                }

                binding.charactersRecyclerView.adapter = CharacterAdapter(characters)
                logEvent("Отображено ${characters.size} персонажей")

            } catch (e: Exception) {
                Log.e(logTag, "Ошибка загрузки персонажей", e)
                Toast.makeText(
                    requireContext(),
                    "Ошибка загрузки персонажей: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logEvent("onDestroyView() вызван")
        _binding = null
    }
}
