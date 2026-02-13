package com.example.quickcart.data

data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val qtyLabel: String = "",
    val price: Int = 0,
    val strikePrice: Int? = null,
    val imageUrl: String = "",
    val inStock: Boolean = true
)

data class CartItem(
    val productId: String = "",
    val name: String = "",
    val qtyLabel: String = "",
    val price: Int = 0,
    val quantity: Int = 0
)

data class Address(
    val fullName: String = "",
    val phone: String = "",
    val line1: String = "",
    val line2: String = "",
    val city: String = "",
    val pincode: String = ""
)

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val total: Int = 0,
    val status: String = OrderStatus.PLACED,
    val paymentMethod: String = PaymentMethod.COD,
    val paymentStatus: String = "PAID",
    val address: Address = Address()
)

data class PaymentSession(
    val sessionId: String,
    val gateway: String,
    val checkoutUrl: String
)

data class AdminOrder(
    val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val total: Int = 0,
    val status: String = OrderStatus.PLACED,
    val paymentMethod: String = PaymentMethod.COD
)

object PaymentMethod {
    const val COD = "COD"
    const val RAZORPAY = "RAZORPAY"
    const val STRIPE = "STRIPE"

    val all = listOf(COD, RAZORPAY, STRIPE)
}

object OrderStatus {
    const val PLACED = "PLACED"
    const val PACKED = "PACKED"
    const val OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY"
    const val DELIVERED = "DELIVERED"

    val timeline = listOf(PLACED, PACKED, OUT_FOR_DELIVERY, DELIVERED)

    fun next(current: String): String {
        val index = timeline.indexOf(current)
        if (index == -1 || index == timeline.lastIndex) return current
        return timeline[index + 1]
    }
}
