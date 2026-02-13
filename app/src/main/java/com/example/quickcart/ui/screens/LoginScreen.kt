package com.example.quickcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    selectedLoginType: String,
    phoneInput: String,
    otpInput: String,
    emailInput: String,
    passwordInput: String,
    verificationRequested: Boolean,
    loading: Boolean,
    error: String?,
    onSelectLoginType: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onEmailLogin: () -> Unit,
    onUseDemoLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("QuickCart", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFEFEF), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RoleChip("USER", selectedLoginType == "USER", onSelectLoginType)
            RoleChip("VENDOR", selectedLoginType == "VENDOR", onSelectLoginType)
            RoleChip("ADMIN", selectedLoginType == "ADMIN", onSelectLoginType)
        }

        Spacer(Modifier.height(16.dp))

        if (selectedLoginType == "USER") {
            OutlinedTextField(
                value = phoneInput,
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone (+countrycode)") },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            if (verificationRequested) {
                OutlinedTextField(
                    value = otpInput,
                    onValueChange = onOtpChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("OTP") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onVerifyOtp, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
                    Text("Verify OTP")
                }
            } else {
                Button(onClick = onSendOtp, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
                    Text("Send OTP")
                }
            }
        } else {
            OutlinedTextField(
                value = emailInput,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = passwordInput,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onEmailLogin, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
                Text("Login as $selectedLoginType")
            }
        }

        if (loading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = onUseDemoLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Use Demo Login")
        }
    }
}

@Composable
private fun RowScope.RoleChip(label: String, selected: Boolean, onSelect: (String) -> Unit) {
    val bg = if (selected) Color(0xFF1F8D49) else Color.White
    val text = if (selected) Color.White else Color.Black
    Text(
        text = label,
        color = text,
        modifier = Modifier
            .weight(1f)
            .background(bg, RoundedCornerShape(10.dp))
            .clickable { onSelect(label) }
            .padding(vertical = 10.dp),
        fontWeight = FontWeight.SemiBold
    )
}
