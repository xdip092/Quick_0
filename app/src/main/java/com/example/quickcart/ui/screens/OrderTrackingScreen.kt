package com.example.quickcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quickcart.data.Order
import com.example.quickcart.data.OrderStatus

@Composable
fun OrderTrackingScreen(order: Order?) {
    if (order == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Loading order status...")
        }
        return
    }

    val timeline = OrderStatus.timeline
    val currentIndex = timeline.indexOf(order.status).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Order #${order.id.take(8)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Status: ${order.status}")
        Text("Payment: ${order.paymentMethod} (${order.paymentStatus})")
        Text("Total: Rs ${order.total}")

        Text("Live Timeline", fontWeight = FontWeight.SemiBold)
        timeline.forEachIndexed { index, status ->
            val done = index <= currentIndex
            val bg = if (done) Color(0xFFD5F5E3) else Color(0xFFF0F0F0)
            Row(
                modifier = Modifier
                    .background(bg)
                    .padding(10.dp)
            ) {
                Text(if (done) "[x] $status" else "[ ] $status")
            }
        }

        Text("Deliver to: ${order.address.fullName}")
        Text("${order.address.line1}, ${order.address.city} - ${order.address.pincode}")

        Text("Items", fontWeight = FontWeight.SemiBold)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(order.items) { item ->
                Card {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.name, fontWeight = FontWeight.SemiBold)
                        Text("${item.qtyLabel} | Rs ${item.price} x ${item.quantity}")
                    }
                }
            }
        }
    }
}
