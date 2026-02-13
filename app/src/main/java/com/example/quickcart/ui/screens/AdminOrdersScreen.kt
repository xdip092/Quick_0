package com.example.quickcart.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quickcart.data.AdminOrder
import com.example.quickcart.data.OrderStatus

@Composable
fun AdminOrdersScreen(
    orders: List<AdminOrder>,
    onAdvanceStatus: (AdminOrder) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Admin Orders", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (orders.isEmpty()) {
            Text("No orders yet")
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(orders, key = { "${it.userId}_${it.id}" }) { order ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Order: ${order.id.take(8)}")
                        Text("User: ${order.customerName}")
                        Text("Payment: ${order.paymentMethod}")
                        Text("Total: Rs ${order.total}")
                        Text("Status: ${order.status}", fontWeight = FontWeight.SemiBold)

                        val next = OrderStatus.next(order.status)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onAdvanceStatus(order) },
                                enabled = next != order.status
                            ) {
                                Text(if (next == order.status) "Completed" else "Mark $next")
                            }
                        }
                    }
                }
            }
        }
    }
}
