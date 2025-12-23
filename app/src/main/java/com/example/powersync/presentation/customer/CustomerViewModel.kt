package com.example.powersync.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.powersync.domain.repository.CustomerRepository
import com.example.powersync.domain.model.Customer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Created by H.Mousavioun on 12/2/2025
 */
class CustomerViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerState())
    val state: StateFlow<CustomerState> = _state.asStateFlow()

    private val intentChannel = Channel<CustomerIntent>(Channel.UNLIMITED)

    init {
        handleIntents()
        sendIntent(CustomerIntent.LoadCustomers)
    }

    fun sendIntent(intent: CustomerIntent) {
        viewModelScope.launch { intentChannel.send(intent) }
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
                    is CustomerIntent.StartEdit -> startEdit(intent.customerId)
                    is CustomerIntent.CancelEdit -> cancelEdit()
                }
            }
        }
    }

    private fun loadCustomers() {
        repository.getCustomers()
            .onStart { _state.update { it.copy(isLoading = true, error = null) } }
            .onEach { list ->
                _state.update {
                    it.copy(
                        customers = list,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { e ->
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun addCustomer(name: String, description: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            repository.addCustomer(
                Customer(
                    id = UUID.randomUUID().toString(),
                    customername = name,
                    description = description
                )
            )
            _state.update { it.copy(nameInput = "", descriptionInput = "", editingId = null) }
        }
    }

    private fun updateCustomer(intent: CustomerIntent.UpdateCustomer) {
        if (intent.name.isBlank()) return

        viewModelScope.launch {
            repository.updateCustomer(
                Customer(
                    id = intent.id,
                    customername = intent.name,
                    description = intent.description
                )
            )
            _state.update { it.copy(nameInput = "", descriptionInput = "", editingId = null) }
        }
    }

    private fun deleteCustomer(id: String) {
        viewModelScope.launch {
            val current = _state.value.customers.find { it.id == id } ?: return@launch
            repository.deleteCustomer(current)
            _state.update { s ->
                if (s.editingId == id) s.copy(
                    nameInput = "",
                    descriptionInput = "",
                    editingId = null
                )
                else s
            }
        }
    }

    private fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
            _state.update { it.copy(nameInput = "", descriptionInput = "", editingId = null) }
        }
    }

    private fun startEdit(customerId: String) {
        val c = _state.value.customers.find { it.id == customerId } ?: return
        _state.update {
            it.copy(
                editingId = c.id,
                nameInput = c.customername,
                descriptionInput = c.description
            )
        }
    }

    private fun cancelEdit() {
        _state.update { it.copy(nameInput = "", descriptionInput = "", editingId = null) }
    }

    fun onNameChanged(value: String) {
        _state.update { it.copy(nameInput = value) }
    }

    fun onDescriptionChanged(value: String) {
        _state.update { it.copy(descriptionInput = value) }
    }

    fun submit() {
        val s = _state.value
        if (s.isEditing) {
            sendIntent(
                CustomerIntent.UpdateCustomer(
                    id = s.editingId!!,
                    name = s.nameInput,
                    description = s.descriptionInput
                )
            )
        } else {
            sendIntent(CustomerIntent.AddCustomer(s.nameInput, s.descriptionInput))
        }
    }

    fun generateFakeCustomers(count: Int = 1000) {
        viewModelScope.launch {
            val list = (0 until count).map {
                Customer(
                    id = UUID.randomUUID().toString(),
                    customername = "Customer $it",
                    description = "Description $it"
                )
            }
            repository.addCustomers(list)
        }
    }
}