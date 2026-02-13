package com.example.quickcart.viewmodel

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickcart.auth.AuthRepository
import com.example.quickcart.data.Address
import com.example.quickcart.data.AdminOrder
import com.example.quickcart.data.CartItem
import com.example.quickcart.data.Order
import com.example.quickcart.data.OrderStatus
import com.example.quickcart.data.PaymentMethod
import com.example.quickcart.data.PaymentSession
import com.example.quickcart.data.Product
import com.example.quickcart.repository.PaymentRepository
import com.example.quickcart.repository.StoreRepository
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val storeRepository: StoreRepository = StoreRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    companion object {
        const val LOGIN_USER = "USER"
        const val LOGIN_VENDOR = "VENDOR"
        const val LOGIN_ADMIN = "ADMIN"
    }

    var authUserId by mutableStateOf<String?>(null)
        private set

    var phoneInput by mutableStateOf("+91")
    var otpInput by mutableStateOf("")
    var emailInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")
    var selectedLoginType by mutableStateOf(LOGIN_USER)
    var verificationId by mutableStateOf<String?>(null)
    var authLoading by mutableStateOf(false)
    var authError by mutableStateOf<String?>(null)

    var isAdmin by mutableStateOf(false)
        private set

    var userRole by mutableStateOf("customer")
        private set

    val canManageCatalog: Boolean
        get() = userRole == "admin" || userRole == "vendor"

    var isDemoMode by mutableStateOf(false)
        private set

    var products by mutableStateOf<List<Product>>(emptyList())
        private set

    var cart by mutableStateOf<List<CartItem>>(emptyList())
        private set

    var selectedCategory by mutableStateOf("All")

    var address by mutableStateOf(Address())
    var selectedPaymentMethod by mutableStateOf(PaymentMethod.COD)
    var paymentSession by mutableStateOf<PaymentSession?>(null)
        private set

    var paymentInProgress by mutableStateOf(false)
    var orderInProgress by mutableStateOf(false)
    var activeOrder by mutableStateOf<Order?>(null)
    var adminOrders by mutableStateOf<List<AdminOrder>>(emptyList())
    var appMessage by mutableStateOf<String?>(null)

    private val demoOrderMap = mutableMapOf<String, Order>()

    private var productsListener: ListenerRegistration? = null
    private var cartListener: ListenerRegistration? = null
    private var orderListener: ListenerRegistration? = null
    private var adminOrdersListener: ListenerRegistration? = null
    private var roleListener: ListenerRegistration? = null
    private var authJob: Job? = null

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        authJob?.cancel()
        authJob = viewModelScope.launch {
            runCatching {
                authRepository.observeUserId().collect { userId ->
                    authUserId = userId
                    clearRealtimeListeners()

                    if (userId == null) {
                        isAdmin = false
                        userRole = "customer"
                        products = emptyList()
                        cart = emptyList()
                        activeOrder = null
                        adminOrders = emptyList()
                        return@collect
                    }

                    initializeUserData(userId)
                }
            }.onFailure {
                enableDemoMode()
            }
        }
    }

    private fun enableDemoMode() {
        if (isDemoMode) return
        isDemoMode = true
        appMessage = "Running in demo mode (Firebase not configured yet)"
        products = demoProducts()
    }

    private fun initializeUserData(userId: String) {
        viewModelScope.launch {
            runCatching { storeRepository.ensureUserProfile(userId) }
                .onFailure { appMessage = it.message ?: "Could not initialize profile" }
        }

        seedProducts()

        productsListener = storeRepository.observeProducts(
            onUpdate = { products = it },
            onError = { appMessage = it.message ?: "Failed loading products" }
        )

        cartListener = storeRepository.observeCart(
            userId = userId,
            onUpdate = { cart = it },
            onError = { appMessage = it.message ?: "Failed loading cart" }
        )

        roleListener = storeRepository.observeUserRole(
            userId = userId,
            onUpdate = { role ->
                userRole = role
                isAdmin = role == "admin"
                if (isAdmin) observeAdminOrders() else {
                    adminOrdersListener?.remove()
                    adminOrdersListener = null
                    adminOrders = emptyList()
                }
            },
            onError = { appMessage = it.message ?: "Failed loading role" }
        )
    }

    private fun observeAdminOrders() {
        adminOrdersListener?.remove()
        adminOrdersListener = storeRepository.observeAllOrdersForAdmin(
            onUpdate = { adminOrders = it },
            onError = { appMessage = it.message ?: "Failed loading admin orders" }
        )
    }

    private fun seedProducts() {
        viewModelScope.launch {
            runCatching { storeRepository.seedProductsIfMissing() }
                .onFailure { appMessage = it.message ?: "Could not seed products" }
        }
    }

    fun sendOtp(activity: Activity) {
        if (selectedLoginType != LOGIN_USER) {
            authError = "Switch to User login for OTP"
            return
        }
        authError = null
        val phone = phoneInput.trim()
        if (phone.length < 10) {
            authError = "Enter valid phone with country code"
            return
        }

        if (isDemoMode) {
            verificationId = "demo-verification"
            authLoading = false
            appMessage = "Demo OTP sent. Use any 6 digits."
            return
        }

        authLoading = true
        authRepository.sendOtp(
            activity = activity,
            phoneNumber = phone,
            onCodeSent = { id, _ ->
                verificationId = id
                authLoading = false
                appMessage = "OTP sent"
            },
            onVerified = { credential -> signInWithCredential(credential) },
            onFailure = { error ->
                authError = error.message ?: "Failed to send OTP"
                authLoading = false
            }
        )
    }

    fun verifyOtp() {
        if (selectedLoginType != LOGIN_USER) {
            authError = "OTP login is only for User"
            return
        }
        val currentVerificationId = verificationId
        if (currentVerificationId.isNullOrBlank()) {
            authError = "Request OTP first"
            return
        }

        if (otpInput.length < 6) {
            authError = "Enter valid 6-digit OTP"
            return
        }

        if (isDemoMode) {
            authUserId = "demo-user"
            isAdmin = true
            verificationId = null
            otpInput = ""
            products = demoProducts()
            appMessage = "Demo login successful"
            return
        }

        authLoading = true
        viewModelScope.launch {
            runCatching {
                authRepository.verifyOtpAndSignIn(currentVerificationId, otpInput)
            }.onSuccess {
                authLoading = false
                verificationId = null
                otpInput = ""
            }.onFailure {
                authLoading = false
                authError = it.message ?: "OTP verification failed"
            }
        }
    }

    fun forceDemoLogin() {
        enableDemoMode()
        authUserId = "demo-user"
        userRole = when (selectedLoginType) {
            LOGIN_ADMIN -> "admin"
            LOGIN_VENDOR -> "vendor"
            else -> "customer"
        }
        isAdmin = userRole == "admin"
        verificationId = null
        otpInput = ""
        emailInput = ""
        passwordInput = ""
        authLoading = false
        authError = null
        products = demoProducts()
        appMessage = "Demo login enabled"
    }

    fun selectLoginType(type: String) {
        selectedLoginType = type
        authError = null
        verificationId = null
        otpInput = ""
    }

    fun loginWithEmailPassword() {
        if (selectedLoginType == LOGIN_USER) {
            authError = "Use OTP for User login"
            return
        }
        val email = emailInput.trim()
        val password = passwordInput
        if (email.isBlank() || password.isBlank()) {
            authError = "Enter email and password"
            return
        }

        authLoading = true
        authError = null
        viewModelScope.launch {
            runCatching {
                val uid = authRepository.signInWithEmailPassword(email, password)
                val actualRole = storeRepository.getUserRole(uid)
                val expectedRole = if (selectedLoginType == LOGIN_ADMIN) "admin" else "vendor"
                if (actualRole != expectedRole) {
                    authRepository.signOut()
                    throw IllegalStateException("This account is '$actualRole'. Please use $actualRole login.")
                }
            }.onSuccess {
                authLoading = false
                passwordInput = ""
            }.onFailure {
                authLoading = false
                authError = it.message ?: "Email login failed"
            }
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            runCatching {
                authRepository.signInWithCredential(credential)
            }.onSuccess {
                authLoading = false
                verificationId = null
                otpInput = ""
            }.onFailure {
                authLoading = false
                authError = it.message ?: "Login failed"
            }
        }
    }

    fun signOut() {
        if (isDemoMode) {
            authUserId = null
            cart = emptyList()
            activeOrder = null
            adminOrders = emptyList()
            isAdmin = false
            userRole = "customer"
            paymentSession = null
            return
        }
        authRepository.signOut()
    }

    fun addToCart(product: Product) {
        val userId = authUserId ?: return
        if (!product.inStock) {
            appMessage = "Item is out of stock"
            return
        }
        if (isDemoMode) {
            val existing = cart.firstOrNull { it.productId == product.id }
            cart = if (existing == null) {
                cart + CartItem(productId = product.id, name = product.name, qtyLabel = product.qtyLabel, price = product.price, quantity = 1)
            } else {
                cart.map { if (it.productId == product.id) it.copy(quantity = it.quantity + 1) else it }
            }
            appMessage = "${product.name} added to cart"
            return
        }

        viewModelScope.launch {
            runCatching { storeRepository.addProductToCart(userId, product) }
                .onSuccess { appMessage = "${product.name} added to cart" }
                .onFailure { appMessage = it.message ?: "Could not add to cart" }
        }
    }

    fun decreaseFromCart(item: CartItem) {
        val userId = authUserId ?: return
        if (isDemoMode) {
            cart = if (item.quantity <= 1) {
                cart.filterNot { it.productId == item.productId }
            } else {
                cart.map { if (it.productId == item.productId) it.copy(quantity = it.quantity - 1) else it }
            }
            return
        }

        viewModelScope.launch {
            runCatching { storeRepository.decreaseProductFromCart(userId, item) }
                .onFailure { appMessage = it.message ?: "Could not update cart" }
        }
    }

    fun selectPaymentMethod(method: String) {
        selectedPaymentMethod = method
        paymentSession = null
    }

    fun validateAddress(): Boolean {
        if (address.fullName.isBlank() || address.phone.isBlank() || address.line1.isBlank() || address.city.isBlank() || address.pincode.isBlank()) {
            appMessage = "Fill complete address"
            return false
        }
        return true
    }

    fun createPaymentSession() {
        val userId = authUserId ?: return
        if (cart.isEmpty()) {
            appMessage = "Cart is empty"
            return
        }
        if (!validateAddress()) return

        if (selectedPaymentMethod == PaymentMethod.COD) {
            appMessage = "COD does not need a payment session"
            return
        }

        paymentInProgress = true
        viewModelScope.launch {
            if (isDemoMode) {
                val sessionId = "demo-session-" + UUID.randomUUID().toString().take(8)
                paymentSession = PaymentSession(
                    sessionId = sessionId,
                    gateway = selectedPaymentMethod,
                    checkoutUrl = "https://example.com/demo-checkout/$sessionId"
                )
                paymentInProgress = false
                appMessage = "Demo payment session created"
                return@launch
            }

            runCatching {
                paymentRepository.createSession(
                    userId = userId,
                    amount = cart.sumOf { it.price * it.quantity },
                    gateway = selectedPaymentMethod
                )
            }.onSuccess {
                paymentSession = it
                paymentInProgress = false
                appMessage = "Payment session created"
            }.onFailure {
                paymentInProgress = false
                appMessage = it.message ?: "Could not create payment session"
            }
        }
    }

    fun verifyPaymentAndPlaceOrder(onPlaced: (String) -> Unit) {
        if (selectedPaymentMethod == PaymentMethod.COD) {
            placeOrderAfterPayment(paymentStatus = "PAID", onPlaced = onPlaced)
            return
        }

        val session = paymentSession
        if (session == null) {
            appMessage = "Create payment session first"
            return
        }

        paymentInProgress = true
        viewModelScope.launch {
            val paid = if (isDemoMode) true else runCatching {
                paymentRepository.verifySession(session.sessionId)
            }.getOrDefault(false)

            if (!paid) {
                paymentInProgress = false
                appMessage = "Payment not verified yet"
                return@launch
            }

            placeOrderAfterPayment(paymentStatus = "PAID", onPlaced = onPlaced)
        }
    }

    private fun placeOrderAfterPayment(paymentStatus: String, onPlaced: (String) -> Unit) {
        val userId = authUserId ?: return
        if (cart.isEmpty()) {
            paymentInProgress = false
            appMessage = "Cart is empty"
            return
        }
        if (!validateAddress()) {
            paymentInProgress = false
            return
        }

        viewModelScope.launch {
            if (isDemoMode) {
                val orderId = "demo-" + UUID.randomUUID().toString().take(8)
                val order = Order(
                    id = orderId,
                    userId = userId,
                    items = cart,
                    total = cart.sumOf { it.price * it.quantity },
                    status = OrderStatus.PLACED,
                    paymentMethod = selectedPaymentMethod,
                    paymentStatus = paymentStatus,
                    address = address
                )
                demoOrderMap[orderId] = order
                activeOrder = order
                adminOrders = listOf(
                    AdminOrder(
                        id = order.id,
                        userId = order.userId,
                        customerName = order.address.fullName,
                        total = order.total,
                        status = order.status,
                        paymentMethod = order.paymentMethod
                    )
                ) + adminOrders
                cart = emptyList()
                paymentSession = null
                paymentInProgress = false
                onPlaced(orderId)
                return@launch
            }

            orderInProgress = true
            runCatching {
                storeRepository.placeOrder(
                    userId = userId,
                    address = address,
                    cart = cart,
                    paymentMethod = selectedPaymentMethod,
                    paymentStatus = paymentStatus
                )
            }.onSuccess { orderId ->
                paymentInProgress = false
                orderInProgress = false
                paymentSession = null
                trackOrder(orderId)
                onPlaced(orderId)
            }.onFailure {
                paymentInProgress = false
                orderInProgress = false
                appMessage = it.message ?: "Order placement failed"
            }
        }
    }

    fun trackOrder(orderId: String) {
        if (isDemoMode) {
            activeOrder = demoOrderMap[orderId]
            return
        }

        val userId = authUserId ?: return
        orderListener?.remove()
        orderListener = storeRepository.observeOrder(
            userId = userId,
            orderId = orderId,
            onUpdate = { activeOrder = it },
            onError = { appMessage = it.message ?: "Failed to track order" }
        )
    }

    fun advanceOrderStatus(order: AdminOrder) {
        val next = OrderStatus.next(order.status)
        if (next == order.status) {
            appMessage = "Order already in final status"
            return
        }

        if (isDemoMode) {
            adminOrders = adminOrders.map {
                if (it.id == order.id && it.userId == order.userId) it.copy(status = next) else it
            }
            val updated = demoOrderMap[order.id]?.copy(status = next)
            if (updated != null) {
                demoOrderMap[order.id] = updated
                if (activeOrder?.id == order.id) activeOrder = updated
            }
            return
        }

        viewModelScope.launch {
            runCatching { storeRepository.updateOrderStatus(order.userId, order.id, next) }
                .onFailure { appMessage = it.message ?: "Status update failed" }
        }
    }

    fun clearMessage() {
        appMessage = null
    }

    fun saveProduct(product: Product) {
        if (!canManageCatalog) {
            appMessage = "Only vendor/admin can manage products"
            return
        }

        if (product.name.isBlank() || product.category.isBlank() || product.qtyLabel.isBlank() || product.price <= 0) {
            appMessage = "Fill valid product details"
            return
        }

        if (isDemoMode) {
            products = if (product.id.isBlank()) {
                products + product.copy(id = UUID.randomUUID().toString().take(8))
            } else {
                products.map { if (it.id == product.id) product else it }
            }
            appMessage = "Product saved"
            return
        }

        viewModelScope.launch {
            runCatching { storeRepository.upsertProduct(product) }
                .onSuccess { appMessage = "Product saved" }
                .onFailure { appMessage = it.message ?: "Failed to save product" }
        }
    }

    fun removeProduct(productId: String) {
        if (!canManageCatalog) {
            appMessage = "Only vendor/admin can manage products"
            return
        }
        if (productId.isBlank()) return

        if (isDemoMode) {
            products = products.filterNot { it.id == productId }
            appMessage = "Product deleted"
            return
        }

        viewModelScope.launch {
            runCatching { storeRepository.deleteProduct(productId) }
                .onSuccess { appMessage = "Product deleted" }
                .onFailure { appMessage = it.message ?: "Failed to delete product" }
        }
    }

    private fun clearRealtimeListeners() {
        productsListener?.remove()
        productsListener = null
        cartListener?.remove()
        cartListener = null
        orderListener?.remove()
        orderListener = null
        adminOrdersListener?.remove()
        adminOrdersListener = null
        roleListener?.remove()
        roleListener = null
    }

    override fun onCleared() {
        clearRealtimeListeners()
        authJob?.cancel()
        super.onCleared()
    }

    private fun demoProducts(): List<Product> {
        return listOf(
            Product(id = "1", name = "Banana", category = "Fruits", qtyLabel = "500 g", price = 38, strikePrice = 45),
            Product(id = "2", name = "Apple", category = "Fruits", qtyLabel = "1 kg", price = 140, strikePrice = 175),
            Product(id = "3", name = "Milk", category = "Dairy", qtyLabel = "1 L", price = 62),
            Product(id = "4", name = "Greek Yogurt", category = "Dairy", qtyLabel = "400 g", price = 99, strikePrice = 129),
            Product(id = "5", name = "Potato Chips", category = "Snacks", qtyLabel = "120 g", price = 45, strikePrice = 60),
            Product(id = "6", name = "Orange Juice", category = "Beverages", qtyLabel = "1 L", price = 110, strikePrice = 135)
        )
    }
}
