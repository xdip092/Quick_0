package com.example.quickcart.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quickcart.ui.screens.AddressScreen
import com.example.quickcart.ui.screens.AdminOrdersScreen
import com.example.quickcart.ui.screens.CartScreen
import com.example.quickcart.ui.screens.CategoriesScreen
import com.example.quickcart.ui.screens.HomeScreen
import com.example.quickcart.ui.screens.LoginScreen
import com.example.quickcart.ui.screens.OrderTrackingScreen
import com.example.quickcart.ui.screens.OrdersScreen
import com.example.quickcart.ui.screens.PaymentScreen
import com.example.quickcart.ui.screens.ProfileScreen
import com.example.quickcart.ui.screens.VendorDashboardScreen
import com.example.quickcart.viewmodel.AppViewModel

enum class AppTab(val route: String, val label: String) {
    Home("home", "Home"),
    Categories("categories", "Categories"),
    Cart("cart", "Cart"),
    Profile("profile", "Profile")
}

private object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val CATEGORIES = "categories"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val ORDERS = "orders"
    const val ADDRESS = "address"
    const val PAYMENT = "payment"
    const val ADMIN = "admin"
    const val VENDOR = "vendor"
    const val ORDER = "order/{orderId}"
    const val ORDER_BASE = "order"
}

@Composable
fun QuickCartApp(viewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val loggedIn = !viewModel.authUserId.isNullOrBlank()

    LaunchedEffect(loggedIn) {
        if (loggedIn && currentRoute == Routes.LOGIN) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        } else if (!loggedIn && currentRoute != Routes.LOGIN) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    LaunchedEffect(viewModel.appMessage) {
        val message = viewModel.appMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
    }

    val showBottomBar = loggedIn && currentRoute in setOf(Routes.HOME, Routes.CATEGORIES, Routes.CART, Routes.PROFILE)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    listOf(AppTab.Home, AppTab.Categories, AppTab.Cart, AppTab.Profile).forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                }
                            },
                            label = { Text(tab.label) },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        AppTab.Home -> Icons.Default.Home
                                        AppTab.Categories -> Icons.Default.Category
                                        AppTab.Cart -> Icons.Default.ShoppingCart
                                        AppTab.Profile -> Icons.Default.Person
                                    },
                                    contentDescription = tab.label
                                )
                            }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (loggedIn) Routes.HOME else Routes.LOGIN,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    selectedLoginType = viewModel.selectedLoginType,
                    phoneInput = viewModel.phoneInput,
                    otpInput = viewModel.otpInput,
                    emailInput = viewModel.emailInput,
                    passwordInput = viewModel.passwordInput,
                    verificationRequested = !viewModel.verificationId.isNullOrBlank(),
                    loading = viewModel.authLoading,
                    error = viewModel.authError,
                    onSelectLoginType = { viewModel.selectLoginType(it) },
                    onPhoneChange = { viewModel.phoneInput = it },
                    onOtpChange = { viewModel.otpInput = it },
                    onEmailChange = { viewModel.emailInput = it },
                    onPasswordChange = { viewModel.passwordInput = it },
                    onSendOtp = {
                        if (activity != null) {
                            viewModel.sendOtp(activity)
                        } else {
                            viewModel.authError = "Unable to access activity"
                        }
                    },
                    onVerifyOtp = { viewModel.verifyOtp() },
                    onEmailLogin = { viewModel.loginWithEmailPassword() },
                    onUseDemoLogin = { viewModel.forceDemoLogin() }
                )
            }

            composable(Routes.HOME) {
                val categories = listOf("All") + viewModel.products.map { it.category }.distinct().sorted()
                val filtered = viewModel.products.filter {
                    viewModel.selectedCategory == "All" || it.category == viewModel.selectedCategory
                }
                HomeScreen(
                    categories = categories,
                    selectedCategory = viewModel.selectedCategory,
                    products = filtered,
                    cartItemsCount = viewModel.cart.sumOf { it.quantity },
                    onSelectCategory = { viewModel.selectedCategory = it },
                    onAddToCart = { viewModel.addToCart(it) }
                )
            }

            composable(Routes.CATEGORIES) {
                CategoriesScreen(
                    products = viewModel.products,
                    onAddToCart = { viewModel.addToCart(it) }
                )
            }

            composable(Routes.CART) {
                CartScreen(
                    cart = viewModel.cart,
                    onIncrease = { item ->
                        val product = viewModel.products.firstOrNull { it.id == item.productId }
                        if (product != null) viewModel.addToCart(product)
                    },
                    onDecrease = { item -> viewModel.decreaseFromCart(item) },
                    onCheckout = { navController.navigate(Routes.ADDRESS) }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    phone = viewModel.phoneInput,
                    address = viewModel.address,
                    currentOrder = viewModel.activeOrder,
                    onOpenOrders = { navController.navigate(Routes.ORDERS) },
                    onOpenVendor = if (viewModel.canManageCatalog) ({ navController.navigate(Routes.VENDOR) }) else null,
                    onOpenAdmin = if (viewModel.isAdmin) ({ navController.navigate(Routes.ADMIN) }) else null,
                    onLogout = { viewModel.signOut() }
                )
            }

            composable(Routes.ORDERS) {
                OrdersScreen(
                    order = viewModel.activeOrder,
                    onTrackOrder = { orderId -> navController.navigate("${Routes.ORDER_BASE}/$orderId") }
                )
            }

            composable(Routes.ADDRESS) {
                AddressScreen(
                    address = viewModel.address,
                    onAddressChange = { viewModel.address = it },
                    onContinueToPayment = {
                        if (viewModel.validateAddress()) {
                            navController.navigate(Routes.PAYMENT)
                        }
                    }
                )
            }

            composable(Routes.PAYMENT) {
                PaymentScreen(
                    total = viewModel.cart.sumOf { it.price * it.quantity },
                    selectedMethod = viewModel.selectedPaymentMethod,
                    paymentSession = viewModel.paymentSession,
                    paymentInProgress = viewModel.paymentInProgress || viewModel.orderInProgress,
                    onSelectMethod = { viewModel.selectPaymentMethod(it) },
                    onCreateSession = { viewModel.createPaymentSession() },
                    onOpenCheckout = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    onVerifyAndPlaceOrder = {
                        viewModel.verifyPaymentAndPlaceOrder { orderId ->
                            navController.navigate("${Routes.ORDER_BASE}/$orderId")
                        }
                    }
                )
            }

            composable(Routes.ADMIN) {
                AdminOrdersScreen(
                    orders = viewModel.adminOrders,
                    onAdvanceStatus = { order -> viewModel.advanceOrderStatus(order) }
                )
            }

            composable(Routes.VENDOR) {
                VendorDashboardScreen(
                    products = viewModel.products,
                    onSaveProduct = { viewModel.saveProduct(it) },
                    onDeleteProduct = { viewModel.removeProduct(it) }
                )
            }

            composable(
                route = Routes.ORDER,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStack ->
                val orderId = backStack.arguments?.getString("orderId") ?: ""
                LaunchedEffect(orderId) {
                    if (orderId.isNotBlank()) {
                        viewModel.trackOrder(orderId)
                    }
                }
                OrderTrackingScreen(order = viewModel.activeOrder)
            }
        }
    }
}
