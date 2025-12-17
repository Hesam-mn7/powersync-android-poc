package com.example.powersync.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.powersync.domain.repository.CustomerRepository
import com.example.powersync.domain.model.Customer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by H.Mousavioun on 12/2/2025
 */
class CustomerViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerState())
    val state: StateFlow<CustomerState> = _state.asStateFlow()

    // برای MVI می‌تونیم Intent ها رو بفرستیم به یه Channel
    private val intentChannel = Channel<CustomerIntent>(Channel.UNLIMITED)

    init {
        handleIntents()
        // اولین بار لیست رو لود کن
        sendIntent(CustomerIntent.LoadCustomers)
    }

    fun sendIntent(intent: CustomerIntent) {
        viewModelScope.launch {
            intentChannel.send(intent)
        }
    }

    private fun handleIntents() {
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                when (intent) {
                    is CustomerIntent.LoadCustomers -> loadCustomers()
                    is CustomerIntent.AddCustomer -> addCustomer(intent.name, intent.description)
                    is CustomerIntent.UpdateCustomer -> updateCustomer(intent)
                    is CustomerIntent.DeleteCustomer -> deleteCustomer(intent.id)
                    is CustomerIntent.DeleteAll -> deleteAll()
                }
            }
        }
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            repository
                .getCustomers()
                .onStart { _state.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { list ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            customers = list,
                            error = null
                        )
                    }
                }
        }
    }

    private fun addCustomer(name: String, description: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            repository.addCustomer(
                Customer(
                    customerName = name,
                    description = description,
                )
            )
            // بعد از insert، جریان Flow خودش لیست جدید رو برمی‌گردونه
        }
    }

    private fun updateCustomer(intent: CustomerIntent.UpdateCustomer) {
        viewModelScope.launch {
            repository.updateCustomer(
                Customer(
                    id = intent.id,
                    customerName = intent.name,
                    description = intent.description
                )
            )
        }
    }

    private fun deleteCustomer(id: String) {
        viewModelScope.launch {
            val current = _state.value.customers.find { it.id == id } ?: return@launch
            repository.deleteCustomer(current)
        }
    }

    private fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun onNameChanged(value: String) {
        _state.update { it.copy(nameInput = value) }
    }

    fun onDescriptionChanged(value: String) {
        _state.update { it.copy(descriptionInput = value) }
    }

    fun submitNewCustomer() {
        val s = _state.value
        sendIntent(CustomerIntent.AddCustomer(s.nameInput, s.descriptionInput))
        _state.update { it.copy(nameInput = "", descriptionInput = "") }
    }

    fun generateFakeCustomers(count: Int = 1000) {
        viewModelScope.launch {
            repeat(count) {
                repository.addCustomer(
                    Customer(
                        customerName = "Customer $it",
                        description = "Description $it"
                    )
                )
            }
        }
    }
}