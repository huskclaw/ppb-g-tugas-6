package com.example.usdidrcurrencyconverter

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.usdidrcurrencyconverter.ui.theme.USDIDRCurrencyConverterTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

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
    var usdToIdrRate by remember { mutableDoubleStateOf(16800.0) } // Default rate
    var usdInput by remember { mutableStateOf("") }
    var idrInput by remember { mutableStateOf("") }
    var lastChanged by remember { mutableStateOf("USD") }
    var currentDate by remember { mutableStateOf("15 04 2025") }
    var isRefreshing by remember { mutableStateOf(false) }

    // Coroutine scope for button
    val scope = rememberCoroutineScope()

    // Update fields in real-time
    LaunchedEffect(usdInput, idrInput, lastChanged, usdToIdrRate) {
        if (lastChanged == "USD") {
            val usdValue = usdInput.toDoubleOrNull()
            idrInput = if (usdValue != null) {
                String.format(Locale.US, "%.0f", usdValue * usdToIdrRate)
            } else {
                ""
            }
        } else if (lastChanged == "IDR") {
            val idrValue = idrInput.toDoubleOrNull()
            usdInput = if (idrValue != null) {
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

            Text(text = "Date: $currentDate")

            Button(
                onClick = {
                    isRefreshing = true
                    scope.launch {
                        fetchExchangeRate { rate ->
                            usdToIdrRate = rate
                            isRefreshing = false
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(if (isRefreshing) "Refreshing..." else "Refresh Rate")
            }
        }
    }
}


@OptIn(DelicateCoroutinesApi::class)
private fun fetchExchangeRate(onRateFetched: (Double) -> Unit) {
    // Launch a background task to fetch the exchange rate
    kotlinx.coroutines.GlobalScope.launch {
        try {
            val url = URL("https://api.exchangerate-api.com/v4/latest/USD")
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connect()

            val inputStreamReader = InputStreamReader(urlConnection.inputStream)
            val response = inputStreamReader.readLines().joinToString("\n")
            val jsonResponse = JSONObject(response)

            // Extract exchange rate for IDR
            val rate = jsonResponse.getJSONObject("rates").getDouble("IDR")

            // Return to the main thread to update the rate
            withContext(Dispatchers.Main) {
                onRateFetched(rate)
            }
        } catch (e: Exception) {
            Log.e("CurrencyConverter", "Error fetching exchange rate", e)
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
