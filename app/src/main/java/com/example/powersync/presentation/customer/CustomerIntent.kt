package com.example.powersync.presentation.customer

/**
 * Created by H.Mousavioun on 12/2/2025
 */
sealed class CustomerIntent {
    object LoadCustomers : CustomerIntent()

    data class AddCustomer(
        val name: String,
        val description: String
    ) : CustomerIntent()

    data class UpdateCustomer(
        val id: String,
        val name: String,
        val description: String
    ) : CustomerIntent()

    data class DeleteCustomer(val id: String) : CustomerIntent()

    object DeleteAll : CustomerIntent()

    data class StartEdit(val customerId: String) : CustomerIntent()
    object CancelEdit : CustomerIntent()
}