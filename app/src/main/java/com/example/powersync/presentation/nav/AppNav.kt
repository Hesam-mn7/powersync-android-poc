package com.example.powersync.presentation.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.powersync.data.local.AppDatabase
import com.example.powersync.data.repository.ProductRepositoryImpl
import com.example.powersync.presentation.customer.CustomerScreen
import com.example.powersync.presentation.customer.CustomerViewModel
import com.example.powersync.presentation.home.HomeScreen
import com.example.powersync.presentation.product.ProductScreen
import com.example.powersync.presentation.product.ProductViewModel

/**
 * Created by H.Mousavioun on 2/1/2026
 */
object Routes {
    const val HOME = "home"
    const val CUSTOMERS = "customers"
    const val PRODUCTS = "products"
}

@Composable
fun AppNav(
    customerVm: CustomerViewModel,
    productVm: ProductViewModel
) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onCustomers = { nav.navigate(Routes.CUSTOMERS) },
                onProducts = { nav.navigate(Routes.PRODUCTS) }
            )
        }
        composable(Routes.CUSTOMERS) {
            CustomerScreen(viewModel = customerVm)
        }
        composable(Routes.PRODUCTS) {
            ProductScreen(
                viewModel = productVm,
                onBack = { nav.popBackStack() }
            )
        }
    }
}