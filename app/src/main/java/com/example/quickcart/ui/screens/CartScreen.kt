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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quickcart.data.CartItem

@Composable
fun CartScreen(
    cart: List<CartItem>,
    onIncrease: (CartItem) -> Unit,
    onDecrease: (CartItem) -> Unit,
    onCheckout: () -> Unit
) {
    val total = cart.sumOf { it.price * it.quantity }

    if (cart.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your cart is empty", style = MaterialTheme.typography.titleLarge)
            Text("Add products from Home")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Cart", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(cart, key = { it.productId }) { item ->
                CartItemRow(item = item, onIncrease = { onIncrease(item) }, onDecrease = { onDecrease(item) })
            }
        }
        Text("Total: Rs $total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Button(
            onClick = onCheckout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Text("Proceed to Checkout")
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.name, fontWeight = FontWeight.SemiBold)
                Text("Rs ${item.price} x ${item.quantity}")
            }
            Row {
                Button(onClick = onDecrease) { Text("-") }
                Text(
                    text = item.quantity.toString(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onIncrease, modifier = Modifier.padding(start = 6.dp)) { Text("+") }
            }
        }
    }
}
