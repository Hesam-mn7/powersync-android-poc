package com.example.powersync.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.powersync.domain.model.Product
import com.example.powersync.domain.repository.ProductRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Created by H.Mousavioun on 2/1/2026
 */

class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProductState())
    val state: StateFlow<ProductState> = _state.asStateFlow()

    private val intentChannel = Channel<ProductIntent>(Channel.UNLIMITED)

    init {
        handleIntents()
        sendIntent(ProductIntent.LoadProducts)
    }

    fun sendIntent(intent: ProductIntent) {
        viewModelScope.launch { intentChannel.send(intent) }
    }

    private fun handleIntents() {
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                when (intent) {
                    is ProductIntent.LoadProducts -> load()
                    is ProductIntent.AddProduct -> add(intent.name, intent.code)
                    is ProductIntent.UpdateProduct -> update(intent.id, intent.name, intent.code)
                    is ProductIntent.DeleteProduct -> delete(intent.id)
                    is ProductIntent.DeleteAll -> deleteAll()
                    is ProductIntent.StartEdit -> startEdit(intent.productId)
                    is ProductIntent.CancelEdit -> cancelEdit()
                }
            }
        }
    }

    private fun load() {
        repository.getProducts()
            .onStart { _state.update { it.copy(isLoading = true, error = null) } }
            .onEach { list ->
                _state.update { it.copy(products = list, isLoading = false, error = null) }
            }
            .catch { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }

    private fun add(name: String, code: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            repository.addProduct(
                Product(
                    id = UUID.randomUUID().toString(),
                    productname = name,
                    productcode = code
                )
            )
            _state.update { it.copy(nameInput = "", codeInput = "", editingId = null) }
        }
    }

    private fun update(id: String, name: String, code: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            repository.updateProduct(
                Product(
                    id = id,
                    productname = name,
                    productcode = code
                )
            )
            _state.update { it.copy(nameInput = "", codeInput = "", editingId = null) }
        }
    }

    private fun delete(id: String) {
        viewModelScope.launch {
            val current = _state.value.products.find { it.id == id } ?: return@launch
            repository.deleteProduct(current)
            _state.update { s ->
                if (s.editingId == id) s.copy(nameInput = "", codeInput = "", editingId = null) else s
            }
        }
    }

    private fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
            _state.update { it.copy(nameInput = "", codeInput = "", editingId = null) }
        }
    }

    private fun startEdit(productId: String) {
        val p = _state.value.products.find { it.id == productId } ?: return
        _state.update {
            it.copy(
                editingId = p.id,
                nameInput = p.productname,
                codeInput = p.productcode
            )
        }
    }

    private fun cancelEdit() {
        _state.update { it.copy(nameInput = "", codeInput = "", editingId = null) }
    }

    fun onNameChanged(value: String) {
        _state.update { it.copy(nameInput = value) }
    }

    fun onCodeChanged(value: String) {
        _state.update { it.copy(codeInput = value) }
    }

    fun submit() {
        val s = _state.value
        if (s.isEditing) {
            sendIntent(ProductIntent.UpdateProduct(s.editingId!!, s.nameInput, s.codeInput))
        } else {
            sendIntent(ProductIntent.AddProduct(s.nameInput, s.codeInput))
        }
    }

    fun generateFakeProducts(count: Int = 1000) {
        viewModelScope.launch {
            val list = (0 until count).map {
                Product(
                    id = UUID.randomUUID().toString(),
                    productname = "Product $it",
                    productcode = it.toString()
                )
            }
            repository.addProducts(list)
        }
    }
}
