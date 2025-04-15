package com.example.usdidrcurrencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.usdidrcurrencyconverter.ui.theme.USDIDRCurrencyConverterTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            USDIDRCurrencyConverterTheme {
                CurrencyConverterScreen()
            }
        }
    }
}

@Composable
fun CurrencyConverterScreen() {
    val usdToIdrRate = 16000.0 // You can change this later
    var usdInput by remember { mutableStateOf("") }
    var idrInput by remember { mutableStateOf("") }
    var lastChanged by remember { mutableStateOf("USD") }

    LaunchedEffect(usdInput, idrInput, lastChanged) {
        if (lastChanged == "USD") {
            val usdValue = usdInput.toDoubleOrNull()
            idrInput = if (usdValue != null) {
                // Make sure to format IDR without scientific notation
                String.format(Locale.US, "%.0f", usdValue * usdToIdrRate)
            } else {
                ""
            }
        } else if (lastChanged == "IDR") {
            val idrValue = idrInput.toDoubleOrNull()
            usdInput = if (idrValue != null) {
                // Format the USD value with 2 decimal places, no scientific notation
                String.format(Locale.US, "%.2f", idrValue / usdToIdrRate)
            } else {
                ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = usdInput,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        usdInput = it
                        lastChanged = "USD"
                    }
                },
                label = { Text("USD") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = idrInput,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        idrInput = it
                        lastChanged = "IDR"
                    }
                },
                label = { Text("IDR") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Currency rate: 1 USD = $usdToIdrRate IDR")
        }
    }
}



@Preview(showBackground = true)
@Composable
fun CurrencyConverterPreview() {
    USDIDRCurrencyConverterTheme {
        CurrencyConverterScreen()
    }
}
