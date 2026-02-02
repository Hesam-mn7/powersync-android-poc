package com.example.powersync.presentation.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.powersync.data.local.entity.ProductEntity
import com.example.powersync.domain.model.Product

/**
 * Created by H.Mousavioun on 2/1/2026
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products (PowerSync)") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    TextButton(onClick = { viewModel.sendIntent(ProductIntent.DeleteAll) }) {
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
                label = { Text("Product Name") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.codeInput,
                onValueChange = { input ->
                    val digitsOnly = input.filter { it.isDigit() }
                    viewModel.onCodeChanged(digitsOnly)
                },
                label = { Text("Product Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

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
                    Text(if (state.isEditing) "Update Product" else "Add Product")
                }

                if (state.isEditing) {
                    OutlinedButton(onClick = { viewModel.sendIntent(ProductIntent.CancelEdit) }) {
                        Text("Cancel")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) CircularProgressIndicator()

            state.error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            ProductList(
                products = state.products,
                onEdit = { id -> viewModel.sendIntent(ProductIntent.StartEdit(id)) },
                onDelete = { id -> viewModel.sendIntent(ProductIntent.DeleteProduct(id)) }
            )
        }
    }
}

@Composable
private fun ProductList(
    products: List<Product>,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn {
        items(products, key = { it.id }) { p ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onEdit(p.id) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = p.productname, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (p.productcode.isNotBlank()) {
                            Text(text = "Code: ${p.productcode}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    TextButton(onClick = { onDelete(p.id) }) { Text("Delete") }
                }
            }
        }
    }
}