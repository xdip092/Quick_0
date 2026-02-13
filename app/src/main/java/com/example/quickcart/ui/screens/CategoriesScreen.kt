package com.example.quickcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quickcart.data.Product

@Composable
fun CategoriesScreen(
    products: List<Product>,
    onAddToCart: (Product) -> Unit
) {
    val grouped = products.groupBy { it.category }.toList()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(12.dp)
    ) {
        Text("Shop by Category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(grouped, key = { it.first }) { group ->
                val category = group.first
                val item = group.second.firstOrNull()
                val quickAddProduct = group.second.firstOrNull { it.inStock }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(category, fontWeight = FontWeight.Bold)
                        Text("${group.second.size} products", color = Color.Gray)
                        if (item != null) {
                            Text("From Rs ${item.price}", modifier = Modifier.padding(top = 6.dp))
                            Button(
                                onClick = { if (quickAddProduct != null) onAddToCart(quickAddProduct) },
                                enabled = quickAddProduct != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) { Text(if (quickAddProduct != null) "Quick Add" else "Out of Stock") }
                        }
                    }
                }
            }
        }
    }
}
