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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quickcart.data.Product

@Composable
fun VendorDashboardScreen(
    products: List<Product>,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit
) {
    var editingId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var qtyLabel by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var strikePrice by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var inStock by remember { mutableStateOf(true) }

    fun clearForm() {
        editingId = ""
        name = ""
        category = ""
        qtyLabel = ""
        price = ""
        strikePrice = ""
        imageUrl = ""
        inStock = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Vendor Dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = qtyLabel, onValueChange = { qtyLabel = it }, label = { Text("Qty Label (e.g. 1 kg)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = price, onValueChange = { price = it.filter { ch -> ch.isDigit() } }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = strikePrice, onValueChange = { strikePrice = it.filter { ch -> ch.isDigit() } }, label = { Text("Strike Price (optional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL (optional)") }, modifier = Modifier.fillMaxWidth())

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = inStock, onCheckedChange = { inStock = it })
            Text("In Stock")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    onSaveProduct(
                        Product(
                            id = editingId,
                            name = name.trim(),
                            category = category.trim(),
                            qtyLabel = qtyLabel.trim(),
                            price = price.toIntOrNull() ?: 0,
                            strikePrice = strikePrice.toIntOrNull(),
                            imageUrl = imageUrl.trim(),
                            inStock = inStock
                        )
                    )
                    clearForm()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (editingId.isBlank()) "Add Product" else "Update Product")
            }
            Button(onClick = { clearForm() }, modifier = Modifier.weight(1f)) {
                Text("Clear")
            }
        }

        HorizontalDivider()
        Text("Existing Products", fontWeight = FontWeight.SemiBold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(products, key = { it.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.name, fontWeight = FontWeight.SemiBold)
                        Text("${item.category} | ${item.qtyLabel}")
                        Text("Rs ${item.price} ${if (item.inStock) "(In Stock)" else "(Out of Stock)"}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    editingId = item.id
                                    name = item.name
                                    category = item.category
                                    qtyLabel = item.qtyLabel
                                    price = item.price.toString()
                                    strikePrice = item.strikePrice?.toString().orEmpty()
                                    imageUrl = item.imageUrl
                                    inStock = item.inStock
                                }
                            ) { Text("Edit") }
                            Button(onClick = { onDeleteProduct(item.id) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
