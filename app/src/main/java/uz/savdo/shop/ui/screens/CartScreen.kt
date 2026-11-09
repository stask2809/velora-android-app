package uz.savdo.shop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.savdo.shop.data.CartLine
import uz.savdo.shop.ui.ShopViewModel
import uz.savdo.shop.util.formatSom

private val paymentMethods = listOf("Cash", "Card", "Payme", "Click")

@Composable
fun CartScreen(vm: ShopViewModel, onOrderPlaced: (Long) -> Unit) {
    val lines by vm.cart.collectAsStateWithLifecycle()
    val total by vm.cartTotal.collectAsStateWithLifecycle()
    var address by rememberSaveable { mutableStateOf("") }
    var payment by rememberSaveable { mutableStateOf(paymentMethods.first()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    if (lines.isEmpty()) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🛒", fontSize = 64.sp)
            Text("Your cart is empty", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("My cart", style = MaterialTheme.typography.headlineSmall) }
        items(lines, key = { it.product.id }) { line ->
            CartRow(
                line = line,
                onPlus = { vm.setQuantity(line.product.id, line.quantity + 1) },
                onMinus = { vm.setQuantity(line.product.id, line.quantity - 1) },
                onRemove = { vm.setQuantity(line.product.id, 0) }
            )
        }
        item {
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                Text(formatSom(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(address, { address = it }, label = { Text("Delivery address") },
                modifier = Modifier.fillMaxWidth())
            Text("Payment method", modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                paymentMethods.forEach { m ->
                    FilterChip(selected = payment == m, onClick = { payment = m }, label = { Text(m) })
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    vm.checkout(address, payment) { result ->
                        result.onSuccess { id -> error = null; address = ""; onOrderPlaced(id) }
                            .onFailure { error = it.message }
                    }
                },
                enabled = address.length >= 5,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp)
            ) { Text("Place order · ${formatSom(total)}") }
        }
    }
}

@Composable
private fun CartRow(line: CartLine, onPlus: () -> Unit, onMinus: () -> Unit, onRemove: () -> Unit) {
    Card {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(line.product.emoji, fontSize = 32.sp)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(line.product.name, fontWeight = FontWeight.SemiBold)
                Text(formatSom(line.lineTotal), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onMinus) { Text("−", fontSize = 22.sp) }
            Text("${line.quantity}", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onPlus) { Icon(Icons.Filled.Add, contentDescription = "More") }
            IconButton(onClick = onRemove) { Icon(Icons.Filled.Delete, contentDescription = "Remove") }
        }
    }
}
