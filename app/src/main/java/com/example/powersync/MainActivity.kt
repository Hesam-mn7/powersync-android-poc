package com.example.powersync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.powersync.data.local.AppDatabase
import com.example.powersync.data.repository.CustomerRepositoryImpl
import com.example.powersync.presentation.customer.CustomerScreen
import com.example.powersync.presentation.customer.CustomerViewModel
import com.example.powersync.ui.theme.MVIRoomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(applicationContext)
        val repo = CustomerRepositoryImpl(db.customerDao())

        setContent {
            MVIRoomTheme {
                val vm: CustomerViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return CustomerViewModel(repo) as T
                        }
                    }
                )
                CustomerScreen(viewModel = vm)
            }
        }
    }
}
