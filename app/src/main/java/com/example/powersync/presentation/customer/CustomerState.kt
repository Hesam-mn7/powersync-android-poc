package com.example.powersync.presentation.customer

import com.example.powersync.domain.model.Customer

/**
 * Created by H.Mousavioun on 12/2/2025
 */

data class CustomerState(
    val isLoading: Boolean = false,
    val customers: List<Customer> = emptyList(),
    val error: String? = null,
    val nameInput: String = "",
    val descriptionInput: String = "",
    val editingId: String? = null
) {
    val isEditing: Boolean get() = editingId != null
}