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
    private var isLoading = false

    private lateinit var repository: CharacterRepository

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

        repository = CharacterRepository(requireContext())

        setupRecyclerView()
        setupButtons()
        observeCharacters() // Реактивное обновление через Flow
        performColdStart() // Холодный старт
    }

    private fun setupRecyclerView() {
        adapter = CharacterAdapter(allCharacters)
        binding.charactersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.charactersRecyclerView.adapter = adapter

        // Слушатель для пагинации
        binding.charactersRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()
                    val total = layoutManager.itemCount

                    if (!isLoading && lastVisible >= total - 2) {
                        loadMoreCharacters()
                    }
                }
            }
        })
    }

    private fun setupButtons() {
        // Кнопка настроек
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
            )
        }

        // Кнопка обновления
        binding.btnRefresh.setOnClickListener {
            refreshCharacters()
        }

        // SwipeRefreshLayout для обновления свайпом
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshCharacters()
        }
    }

    // C. Реактивное обновление через Flow
    private fun observeCharacters() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.charactersFlow.collect { characters ->
                logEvent("Flow: получено ${characters.size} персонажей")

                // Обновляем список
                allCharacters.clear()
                allCharacters.addAll(characters)

                // Уведомляем адаптер
                adapter.notifyDataSetChanged()

                // Скрываем прогресс
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false

                // Показывае и скрытие сообщение о пустом списке
                if (characters.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.text = "Нет данных.\nПотяните вниз для обновления"
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                }
            }
        }
    }

    // B. Холодный старт
    private fun performColdStart() {
        logEvent("Выполняется холодный старт")
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val isEmpty = repository.isDatabaseEmpty()

                if (isEmpty) {
                    logEvent("БД пуста, загружаем данные из API")
                    // БД пуста - загружаем из API
                    loadCharactersFromApi(1)
                } else {
                    logEvent("БД содержит данные, отображаем из Room")
                    // БД содержит данные автоматически отобразятся через Flow
                    binding.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                logEvent("Ошибка холодного старта: ${e.message}")
                binding.progressBar.visibility = View.GONE
                showError("Ошибка загрузки данных: ${e.message}")
            }
        }
    }

    // B. Обновление списка
    private fun refreshCharacters() {
        logEvent("Обновление списка персонажей")
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = repository.refreshCharacters()

                result.onSuccess {
                    logEvent("Список успешно обновлен")
                    Snackbar.make(binding.root, "Список обновлен", Snackbar.LENGTH_SHORT).show()
                }

                result.onFailure { exception ->
                    logEvent("Ошибка обновления: ${exception.message}")
                    showError("Ошибка обновления: ${exception.message}")
                }
            } catch (e: Exception) {
                logEvent("Ошибка обновления: ${e.message}")
                showError("Ошибка обновления: ${e.message}")
            } finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    // B. Загрузка следующей страницы
    private fun loadMoreCharacters() {
        logEvent("Загрузка следующей страницы")
        isLoading = true
        adapter.showLoadingFooter(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = repository.loadNextPage()

                result.onSuccess { page ->
                    logEvent("Загружена страница $page")
                }

                result.onFailure { exception ->
                    logEvent("Ошибка загрузки страницы: ${exception.message}")
                    showError("Больше персонажей нет или ошибка загрузки")
                }
            } catch (e: Exception) {
                logEvent("Ошибка загрузки страницы: ${e.message}")
                showError("Ошибка загрузки: ${e.message}")
            } finally {
                isLoading = false
                adapter.showLoadingFooter(false)
            }
        }
    }

    // Загрузка из API (при холодном старте)
    private fun loadCharactersFromApi(page: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                isLoading = true
                val result = repository.fetchAndSaveCharacters(page)

                result.onSuccess {
                    logEvent("Данные успешно загружены и сохранены в БД")
                }

                result.onFailure { exception ->
                    logEvent("Ошибка загрузки: ${exception.message}")
                    showError("Ошибка загрузки: ${exception.message}")
                }
            } catch (e: Exception) {
                logEvent("Ошибка загрузки: ${e.message}")
                showError("Ошибка загрузки: ${e.message}")
            } finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Повторить") {
                performColdStart()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}