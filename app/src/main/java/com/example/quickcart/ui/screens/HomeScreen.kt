package com.example.quickcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.quickcart.data.Product

private val BlinkitYellow = Color(0xFFFFE141)
private val BlinkitGreen = Color(0xFF0B8D48)

@Composable
fun HomeScreen(
    categories: List<String>,
    selectedCategory: String,
    products: List<Product>,
    cartItemsCount: Int,
    onSelectCategory: (String) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val visible = products.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        HeaderSection(cartItemsCount = cartItemsCount, searchQuery = searchQuery, onSearch = { searchQuery = it })

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                CategoryStrip(categories = categories, selectedCategory = selectedCategory, onSelectCategory = onSelectCategory)
            }
            item {
                PromoBanners()
            }
            item {
                Text(
                    text = "Bestsellers",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                if (visible.isEmpty()) {
                    Text(
                        text = "No products found",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(560.dp)
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(visible, key = { it.id }) { product ->
                            ProductCard(product = product, onAddToCart = onAddToCart)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    cartItemsCount: Int,
    searchQuery: String,
    onSearch: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BlinkitYellow)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text("Delivering in 10 minutes", fontWeight = FontWeight.Bold)
        Text("Home - QuickCart Street", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            placeholder = { Text("Search 'milk', 'chips', 'fruits'...") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))
        Text("$cartItemsCount item(s) in cart", color = BlinkitGreen, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CategoryStrip(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(top = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            val selected = selectedCategory == category
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) BlinkitGreen else Color.White)
                    .border(1.dp, if (selected) BlinkitGreen else Color(0xFFE2E2E2), RoundedCornerShape(18.dp))
                    .clickable { onSelectCategory(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(category, color = if (selected) Color.White else Color.Black)
            }
        }
    }
}

@Composable
private fun PromoBanners() {
    LazyRow(
        modifier = Modifier.padding(top = 14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(3) { idx ->
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .height(90.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (idx) {
                        0 -> Color(0xFFE8F9E8)
                        1 -> Color(0xFFFFF4DB)
                        else -> Color(0xFFE8F2FF)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = when (idx) {
                            0 -> "Fresh Fruits Sale"
                            1 -> "Daily Essentials"
                            else -> "Snacks Party Combo"
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Up to 30% OFF")
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, onAddToCart: (Product) -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF2F2F2)),
                contentAlignment = Alignment.Center
            ) {
                Text(product.category, color = Color.DarkGray)
            }
            Spacer(Modifier.height(8.dp))
            Text(product.name, fontWeight = FontWeight.SemiBold)
            Text(product.qtyLabel, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Rs ${product.price}", fontWeight = FontWeight.Bold)
                if (product.strikePrice != null) {
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Rs ${product.strikePrice}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }
            Text(
                text = if (product.inStock) "In Stock" else "Out of Stock",
                color = if (product.inStock) Color(0xFF0B8D48) else Color(0xFFD32F2F),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onAddToCart(product) },
                modifier = Modifier.fillMaxWidth(),
                enabled = product.inStock,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (product.inStock) "ADD" else "OUT")
            }
        }
    }
}
