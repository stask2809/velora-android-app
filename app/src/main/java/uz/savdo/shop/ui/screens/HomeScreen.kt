package uz.savdo.shop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.savdo.shop.data.Product
import uz.savdo.shop.data.SampleData
import uz.savdo.shop.ui.theme.SavdoShopTheme
import uz.savdo.shop.ui.ShopViewModel
import uz.savdo.shop.util.formatSom

@Composable
fun HomeScreen(vm: ShopViewModel, onProductClick: (Long) -> Unit) {
    val products by vm.products.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val selected by vm.category.collectAsStateWithLifecycle()
    val user by vm.currentUser.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text(
            "Salom, ${user?.fullName?.substringBefore(' ') ?: "guest"}!",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = query,
            onValueChange = { vm.query.value = it },
            placeholder = { Text("Search products") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                FilterChip(selected = selected == null, onClick = { vm.category.value = null },
                    label = { Text("All") })
            }
            items(categories) { c ->
                FilterChip(selected = selected == c, onClick = { vm.category.value = c },
                    label = { Text(c) })
            }
        }
        if (products.isEmpty()) {
            Text("Nothing found 😕", modifier = Modifier.padding(24.dp))
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(products, key = { it.id }) { p -> ProductCard(p) { onProductClick(p.id) } }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(16.dp)) {
        Box(
            Modifier.fillMaxWidth().height(110.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Text(product.emoji, fontSize = 52.sp) }
        Column(Modifier.padding(12.dp)) {
            Text(product.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("★ ${product.rating}", style = MaterialTheme.typography.bodySmall)
            Text(formatSom(product.price), color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold)
        }
    }
}

// Shows the card in Android Studio's Split/Design view without running the app
@Preview(showBackground = true, widthDp = 180)
@Composable
private fun ProductCardPreview() {
    SavdoShopTheme { ProductCard(SampleData.products.first()) {} }
}
