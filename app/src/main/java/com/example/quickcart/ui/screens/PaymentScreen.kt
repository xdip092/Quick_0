package com.example.quickcart.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quickcart.data.PaymentMethod
import com.example.quickcart.data.PaymentSession

@Composable
fun PaymentScreen(
    total: Int,
    selectedMethod: String,
    paymentSession: PaymentSession?,
    paymentInProgress: Boolean,
    onSelectMethod: (String) -> Unit,
    onCreateSession: () -> Unit,
    onOpenCheckout: (String) -> Unit,
    onVerifyAndPlaceOrder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Payment", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Total payable: Rs $total")

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(PaymentMethod.all) { method ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onSelectMethod(method) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(selected = selectedMethod == method, onClick = null)
                        Text("  $method")
                    }
                }
            }
        }

        if (selectedMethod == PaymentMethod.COD) {
            Button(
                onClick = onVerifyAndPlaceOrder,
                enabled = !paymentInProgress,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (paymentInProgress) "Processing..." else "Place Order (COD)")
            }
            return
        }

        if (paymentSession == null) {
            Button(
                onClick = onCreateSession,
                enabled = !paymentInProgress,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (paymentInProgress) "Creating Session..." else "Create Secure Payment Session")
            }
        } else {
            Text("Session: ${paymentSession.sessionId}")
            Button(
                onClick = { onOpenCheckout(paymentSession.checkoutUrl) },
                enabled = !paymentInProgress,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open ${paymentSession.gateway} Checkout")
            }
            Button(
                onClick = onVerifyAndPlaceOrder,
                enabled = !paymentInProgress,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (paymentInProgress) "Verifying..." else "I Paid, Verify and Place Order")
            }
        }
    }
}
