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
                    TextButton(onClick = { viewModel.sendIntent(CustomerIntent.DeleteAll) }) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.submit() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isEditing) "Update Customer" else "Add Customer")
                }

                if (state.isEditing) {
                    OutlinedButton(
                        onClick = { viewModel.sendIntent(CustomerIntent.CancelEdit) }
                    ) {
                        Text("Cancel")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.generateFakeCustomers(1000) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate 1000 Customers (Performance Test)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) CircularProgressIndicator()

            state.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            CustomerList(
                customers = state.customers,
                onEdit = { id -> viewModel.sendIntent(CustomerIntent.StartEdit(id)) },
                onDelete = { id -> viewModel.sendIntent(CustomerIntent.DeleteCustomer(id)) }
            )
        }
    }
}

@Composable
private fun CustomerList(
    customers: List<Customer>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn {
        items(customers, key = { it.id }) { customer ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onEdit(customer.id) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = customer.customername,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = customer.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(onClick = { onDelete(customer.id) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
