package com.example.rickandmorty.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.data.repository.CharacterRepository
import com.example.rickandmorty.databinding.FragmentHomeBinding
import com.example.rickandmorty.ui.base.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val allCharacters = mutableListOf<CharacterUi>()
    private lateinit var adapter: CharacterAdapter
    private var currentPage = 1
    private var isLoading = false
    private var hasNextPage = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CharacterAdapter(allCharacters)
        binding.charactersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.charactersRecyclerView.adapter = adapter

        binding.homeToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == com.example.rickandmorty.R.id.action_settings) {
                findNavController().navigate(com.example.rickandmorty.R.id.action_homeFragment_to_settingsFragment)
                true
            } else {
                false
            }
        }

        loadPage(currentPage)

        binding.charactersRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()
                    val total = layoutManager.itemCount

                    if (!isLoading && hasNextPage && lastVisible >= total - 2) {
                        loadPage(++currentPage)
                    }
                }
            }
        })
    }

    private fun loadPage(page: Int) {
        lifecycleScope.launch {
            try {
                isLoading = true
                if (page == 1) binding.progressBar.visibility = View.VISIBLE
                else adapter.showLoadingFooter(true)

                val response = CharacterRepository.getCharacters(page)

                binding.progressBar.visibility = View.GONE
                adapter.showLoadingFooter(false)
                isLoading = false

                if (response == null) {
                    Snackbar.make(binding.root, "Ошибка загрузки", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Повторить") { loadPage(page) }
                        .show()
                    return@launch
                }

                val mapped = response.results.map {
                    CharacterUi(
                        name = it.name,
                        image = it.image,
                        status = it.status,
                        species = it.species
                    )
                }

                if (mapped.isEmpty()) {
                    hasNextPage = false
                    return@launch
                }

                val start = allCharacters.size
                allCharacters.addAll(mapped)

                binding.charactersRecyclerView.post {
                    adapter.notifyItemRangeInserted(start, mapped.size)
                }

                logEvent("Загружено ${mapped.size} персонажей (страница $page)")
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                adapter.showLoadingFooter(false)
                isLoading = false
                Snackbar.make(binding.root, "Ошибка сети: ${e.message}", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Повторить") { loadPage(page) }
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
