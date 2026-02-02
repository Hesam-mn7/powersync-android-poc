package com.example.powersync.presentation.product

import com.example.powersync.domain.model.Product

/**
 * Created by H.Mousavioun on 2/1/2026
 */
data class ProductState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,

    val nameInput: String = "",
    val codeInput: String = "",

    val editingId: String? = null
) {
    val isEditing: Boolean get() = editingId != null
}
