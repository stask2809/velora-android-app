package uz.savdo.shop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.savdo.shop.ui.ShopViewModel
import uz.savdo.shop.util.formatDate
import uz.savdo.shop.util.formatSom

@Composable
fun OrdersScreen(vm: ShopViewModel, onLoggedOut: () -> Unit) {
    val orders by vm.orders.collectAsStateWithLifecycle()
    val user by vm.currentUser.collectAsStateWithLifecycle()

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(user?.fullName ?: "", style = MaterialTheme.typography.headlineSmall)
            Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = { vm.logout(); onLoggedOut() }, modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Log out")
            }
            Text("My orders (${orders.size})", style = MaterialTheme.typography.titleMedium)
        }
        if (orders.isEmpty()) item { Text("No orders yet.") }
        items(orders, key = { it.id }) { o ->
            Card {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Order #${o.id}", fontWeight = FontWeight.SemiBold)
                        Text(formatDate(o.createdAt), style = MaterialTheme.typography.bodySmall)
                        Text("${o.paymentMethod} · ${o.address}", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatSom(o.total), fontWeight = FontWeight.Bold)
                        AssistChip(onClick = {}, label = { Text(o.status) })
                    }
                }
            }
        }
    }
}
