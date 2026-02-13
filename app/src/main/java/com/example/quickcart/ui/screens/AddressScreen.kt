package com.example.quickcart.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quickcart.data.Address

@Composable
fun AddressScreen(
    address: Address,
    onAddressChange: (Address) -> Unit,
    onContinueToPayment: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Delivery Address", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(value = address.fullName, onValueChange = { onAddressChange(address.copy(fullName = it)) }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address.phone, onValueChange = { onAddressChange(address.copy(phone = it)) }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address.line1, onValueChange = { onAddressChange(address.copy(line1 = it)) }, label = { Text("House / Street") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address.line2, onValueChange = { onAddressChange(address.copy(line2 = it)) }, label = { Text("Landmark") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address.city, onValueChange = { onAddressChange(address.copy(city = it)) }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address.pincode, onValueChange = { onAddressChange(address.copy(pincode = it)) }, label = { Text("Pincode") }, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onContinueToPayment, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Continue to Payment")
            }
        }
    }
}
