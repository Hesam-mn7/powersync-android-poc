package com.example.powersync.presentation.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.powersync.domain.model.Customer

/**
 * Created by H.Mousavioun on 12/2/2025
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    viewModel: CustomerViewModel
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers (PowerSync)") },
                actions = {
                    TextButton(onClick = {
                        viewModel.sendIntent(CustomerIntent.DeleteAll)
                    }) {
                        Text("Delete All")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Inputs
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.nameInput,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Customer Name") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.descriptionInput,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Description") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.submitNewCustomer() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Customer")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.generateFakeCustomers(10000) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate 1000 Customers (Performance Test)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            CustomerList(
                customers = state.customers,
                onDelete = { id ->
                    viewModel.sendIntent(CustomerIntent.DeleteCustomer(id))
                }
            )
        }
    }
}

@Composable
private fun CustomerList(
    customers: List<Customer>,
    onDelete: (String) -> Unit
) {
    LazyColumn {
        items(customers) { customer ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        // اینجا می‌تونی بعداً برای Edit دیالوگ باز کنی
                    }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = customer.customerName, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = customer.description, style = MaterialTheme.typography.bodyMedium)
                    }
                    TextButton(onClick = { onDelete(customer.id) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
