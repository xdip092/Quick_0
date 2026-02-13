package com.example.quickcart.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quickcart.data.Order

@Composable
fun OrdersScreen(
    order: Order?,
    onTrackOrder: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Your Orders", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (order == null) {
            Text("No orders yet")
            return
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Order #${order.id.take(8)}", fontWeight = FontWeight.SemiBold)
                Text("Items: ${order.items.sumOf { it.quantity }}")
                Text("Total: Rs ${order.total}")
                Text("Status: ${order.status}")
                Button(onClick = { onTrackOrder(order.id) }, modifier = Modifier.padding(top = 6.dp)) {
                    Text("Track Order")
                }
            }
        }
    }
}
