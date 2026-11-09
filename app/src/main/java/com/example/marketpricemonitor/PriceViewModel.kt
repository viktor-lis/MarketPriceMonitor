package com.example.marketpricemonitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SortOrder { NONE, PRICE_ASC, PRICE_DESC }

data class UiState(
    val loading: Boolean = false,
    val items: List<Product> = emptyList(),
    val error: String? = null,
    val query: String = "",
    val sortOrder: SortOrder = SortOrder.NONE
)
class PriceViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun onQueryChange(newQuery: String) {
        _state.value = _state.value.copy(query = newQuery)
    }
    fun clear() {
        _state.value = UiState()
    }
    fun setSortOrder(order: SortOrder) {
        _state.value = _state.value.copy(sortOrder = order)
    }

    fun search() {
        val q = _state.value.query.trim()
        if (q.isEmpty()) {
            _state.value = _state.value.copy(error = "Введите запрос")
            return
        }
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val response = ApiClient.api.search(q, 5)
                _state.value = _state.value.copy(loading = false, items = response.items)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = "Ошибка: ${e.message}"
                )
            }
        }
    }
}