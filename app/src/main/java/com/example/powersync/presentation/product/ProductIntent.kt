package com.example.powersync.presentation.product

/**
 * Created by H.Mousavioun on 2/1/2026
 */
sealed class ProductIntent {
    data object LoadProducts : ProductIntent()

    data class AddProduct(val name: String, val code: String) : ProductIntent()
    data class UpdateProduct(val id: String, val name: String, val code: String) : ProductIntent()

    data class DeleteProduct(val id: String) : ProductIntent()
    data object DeleteAll : ProductIntent()

    data class StartEdit(val productId: String) : ProductIntent()
    data object CancelEdit : ProductIntent()
}