package com.example.quickcart.repository

import com.example.quickcart.data.Address
import com.example.quickcart.data.AdminOrder
import com.example.quickcart.data.CartItem
import com.example.quickcart.data.Order
import com.example.quickcart.data.OrderStatus
import com.example.quickcart.data.Product
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class StoreRepository(
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() }
) {
    private fun firestore(): FirebaseFirestore = firestoreProvider()

    fun observeProducts(
        onUpdate: (List<Product>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return firestore().collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val products = snapshot?.documents?.map { doc ->
                    Product(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "",
                        qtyLabel = doc.getString("qtyLabel") ?: "",
                        price = (doc.getLong("price") ?: 0).toInt(),
                        strikePrice = doc.getLong("strikePrice")?.toInt(),
                        imageUrl = doc.getString("imageUrl") ?: "",
                        inStock = doc.getBoolean("inStock") ?: true
                    )
                } ?: emptyList()

                onUpdate(products)
            }
    }

    suspend fun seedProductsIfMissing() {
        val productsRef = firestore().collection("products")
        val existing = productsRef.limit(1).get().await()
        if (!existing.isEmpty) return

        val seed = listOf(
            mapOf("name" to "Banana", "category" to "Fruits", "qtyLabel" to "500 g", "price" to 38, "strikePrice" to 45, "inStock" to true),
            mapOf("name" to "Apple", "category" to "Fruits", "qtyLabel" to "1 kg", "price" to 140, "strikePrice" to 175, "inStock" to true),
            mapOf("name" to "Milk", "category" to "Dairy", "qtyLabel" to "1 L", "price" to 62, "inStock" to true),
            mapOf("name" to "Greek Yogurt", "category" to "Dairy", "qtyLabel" to "400 g", "price" to 99, "strikePrice" to 129, "inStock" to true),
            mapOf("name" to "Potato Chips", "category" to "Snacks", "qtyLabel" to "120 g", "price" to 45, "strikePrice" to 60, "inStock" to true),
            mapOf("name" to "Orange Juice", "category" to "Beverages", "qtyLabel" to "1 L", "price" to 110, "strikePrice" to 135, "inStock" to true)
        )

        val batch = firestore().batch()
        seed.forEach { payload -> batch.set(productsRef.document(), payload) }
        batch.commit().await()
    }

    suspend fun ensureUserProfile(userId: String) {
        val userRef = firestore().collection("users").document(userId)
        val snapshot = userRef.get().await()
        if (!snapshot.exists()) {
            userRef.set(
                mapOf(
                    "role" to "customer",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    fun observeUserRole(
        userId: String,
        onUpdate: (String) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return firestore().collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onUpdate(snapshot?.getString("role") ?: "customer")
            }
    }

    suspend fun getUserRole(userId: String): String {
        val snapshot = firestore().collection("users").document(userId).get().await()
        return snapshot.getString("role") ?: "customer"
    }

    fun observeCart(
        userId: String,
        onUpdate: (List<CartItem>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return firestore().collection("users")
            .document(userId)
            .collection("cart")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val cart = snapshot?.documents?.map { doc ->
                    CartItem(
                        productId = doc.id,
                        name = doc.getString("name") ?: "",
                        qtyLabel = doc.getString("qtyLabel") ?: "",
                        price = (doc.getLong("price") ?: 0).toInt(),
                        quantity = (doc.getLong("quantity") ?: 0).toInt()
                    )
                } ?: emptyList()

                onUpdate(cart.sortedBy { it.name })
            }
    }

    suspend fun addProductToCart(userId: String, product: Product) {
        val doc = firestore().collection("users")
            .document(userId)
            .collection("cart")
            .document(product.id)

        firestore().runTransaction { tx ->
            val snapshot = tx.get(doc)
            val oldQty = (snapshot.getLong("quantity") ?: 0L).toInt()
            tx.set(
                doc,
                mapOf(
                    "name" to product.name,
                    "qtyLabel" to product.qtyLabel,
                    "price" to product.price,
                    "quantity" to oldQty + 1,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }.await()
    }

    suspend fun decreaseProductFromCart(userId: String, item: CartItem) {
        val doc = firestore().collection("users")
            .document(userId)
            .collection("cart")
            .document(item.productId)

        if (item.quantity <= 1) {
            doc.delete().await()
            return
        }

        doc.update("quantity", item.quantity - 1).await()
    }

    suspend fun placeOrder(
        userId: String,
        address: Address,
        cart: List<CartItem>,
        paymentMethod: String,
        paymentStatus: String
    ): String {
        val orderDoc = firestore().collection("users")
            .document(userId)
            .collection("orders")
            .document()

        val total = cart.sumOf { it.price * it.quantity }
        val payload = mapOf(
            "userId" to userId,
            "status" to OrderStatus.PLACED,
            "total" to total,
            "paymentMethod" to paymentMethod,
            "paymentStatus" to paymentStatus,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "address" to mapOf(
                "fullName" to address.fullName,
                "phone" to address.phone,
                "line1" to address.line1,
                "line2" to address.line2,
                "city" to address.city,
                "pincode" to address.pincode
            ),
            "items" to cart.map {
                mapOf(
                    "productId" to it.productId,
                    "name" to it.name,
                    "qtyLabel" to it.qtyLabel,
                    "price" to it.price,
                    "quantity" to it.quantity
                )
            }
        )

        val batch = firestore().batch()
        batch.set(orderDoc, payload)

        cart.forEach {
            val cartDoc = firestore().collection("users")
                .document(userId)
                .collection("cart")
                .document(it.productId)
            batch.delete(cartDoc)
        }

        batch.commit().await()
        return orderDoc.id
    }

    fun observeOrder(
        userId: String,
        orderId: String,
        onUpdate: (Order) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return firestore().collection("users")
            .document(userId)
            .collection("orders")
            .document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val data = snapshot ?: return@addSnapshotListener
                if (!data.exists()) return@addSnapshotListener
                onUpdate(data.toOrder())
            }
    }

    fun observeAllOrdersForAdmin(
        onUpdate: (List<AdminOrder>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return firestore().collectionGroup("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val orders = snapshot?.documents?.mapNotNull { doc ->
                    val userId = doc.getString("userId") ?: return@mapNotNull null
                    val address = doc.get("address") as? Map<*, *> ?: emptyMap<String, Any>()
                    AdminOrder(
                        id = doc.id,
                        userId = userId,
                        customerName = address["fullName"]?.toString() ?: "Unknown",
                        total = (doc.getLong("total") ?: 0).toInt(),
                        status = doc.getString("status") ?: OrderStatus.PLACED,
                        paymentMethod = doc.getString("paymentMethod") ?: "COD"
                    )
                } ?: emptyList()

                onUpdate(orders.sortedByDescending { it.id })
            }
    }

    suspend fun updateOrderStatus(userId: String, orderId: String, status: String) {
        val orderDoc = firestore().collection("users")
            .document(userId)
            .collection("orders")
            .document(orderId)

        orderDoc.update(
            mapOf(
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun upsertProduct(product: Product) {
        val collection = firestore().collection("products")
        val ref = if (product.id.isBlank()) collection.document() else collection.document(product.id)
        ref.set(
            mapOf(
                "name" to product.name,
                "category" to product.category,
                "qtyLabel" to product.qtyLabel,
                "price" to product.price,
                "strikePrice" to product.strikePrice,
                "imageUrl" to product.imageUrl,
                "inStock" to product.inStock,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun deleteProduct(productId: String) {
        firestore().collection("products").document(productId).delete().await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toOrder(): Order {
        val addressMap = get("address") as? Map<*, *> ?: emptyMap<String, String>()
        val itemsRaw = get("items") as? List<*> ?: emptyList<Map<String, Any>>()
        val items = itemsRaw.mapNotNull { raw ->
            val map = raw as? Map<*, *> ?: return@mapNotNull null
            CartItem(
                productId = map["productId"]?.toString() ?: "",
                name = map["name"]?.toString() ?: "",
                qtyLabel = map["qtyLabel"]?.toString() ?: "",
                price = (map["price"] as? Number)?.toInt() ?: 0,
                quantity = (map["quantity"] as? Number)?.toInt() ?: 0
            )
        }

        return Order(
            id = id,
            userId = getString("userId") ?: "",
            items = items,
            total = (getLong("total") ?: 0).toInt(),
            status = getString("status") ?: OrderStatus.PLACED,
            paymentMethod = getString("paymentMethod") ?: "COD",
            paymentStatus = getString("paymentStatus") ?: "PAID",
            address = Address(
                fullName = addressMap["fullName"]?.toString() ?: "",
                phone = addressMap["phone"]?.toString() ?: "",
                line1 = addressMap["line1"]?.toString() ?: "",
                line2 = addressMap["line2"]?.toString() ?: "",
                city = addressMap["city"]?.toString() ?: "",
                pincode = addressMap["pincode"]?.toString() ?: ""
            )
        )
    }
}
