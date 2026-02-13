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
import com.example.quickcart.data.Address
import com.example.quickcart.data.Order

@Composable
fun ProfileScreen(
    phone: String,
    address: Address,
    currentOrder: Order?,
    onOpenOrders: () -> Unit,
    onOpenVendor: (() -> Unit)?,
    onOpenAdmin: (() -> Unit)?,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Phone: $phone")
                Text("Address: ${address.line1.ifBlank { "Not set" }}")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Current Order", fontWeight = FontWeight.SemiBold)
                if (currentOrder == null) {
                    Text("No active orders")
                } else {
                    Text("#${currentOrder.id.take(8)}")
                    Text("Status: ${currentOrder.status}")
                }
                Button(onClick = onOpenOrders, modifier = Modifier.padding(top = 8.dp)) {
                    Text("View Orders")
                }
            }
        }

        if (onOpenVendor != null) {
            Button(onClick = onOpenVendor, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Catalog")
            }
        }

        if (onOpenAdmin != null) {
            Button(onClick = onOpenAdmin, modifier = Modifier.fillMaxWidth()) {
                Text("Admin Panel")
            }
        }

        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Logout")
        }
    }
}
