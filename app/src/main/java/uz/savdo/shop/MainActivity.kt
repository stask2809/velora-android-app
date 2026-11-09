package uz.savdo.shop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import uz.savdo.shop.data.VeloraApi
import uz.savdo.shop.ui.VeloraViewModel

private val Ink = androidx.compose.ui.graphics.Color(0xFF171717)
private val Paper = androidx.compose.ui.graphics.Color(0xFFF7F6F2)
private val Muted = androidx.compose.ui.graphics.Color(0xFF77736E)
private val Line = androidx.compose.ui.graphics.Color(0xFFE6E3DC)
private val Accent = androidx.compose.ui.graphics.Color(0xFF9B8062)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { VeloraTheme { VeloraApp() } }
    }
}

@Composable
private fun VeloraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Ink, onPrimary = androidx.compose.ui.graphics.Color.White,
            background = Paper, surface = androidx.compose.ui.graphics.Color.White, onSurface = Ink,
            secondary = Accent, outline = Line),
        typography = Typography(), content = content
    )
}

@Composable
private fun VeloraApp(vm: VeloraViewModel = viewModel()) {
    val products by vm.products.collectAsState()
    val categories by vm.categories.collectAsState()
    val selected by vm.selected.collectAsState()
    val cart by vm.cart.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val orderMessage by vm.orderMessage.collectAsState()
    var tab by remember { mutableStateOf("shop") }
    var search by remember { mutableStateOf("") }
    var chosenCategory by remember { mutableStateOf<Int?>(null) }
    var checkoutOpen by remember { mutableStateOf(false) }

    Scaffold(containerColor = Paper, topBar = {
        Surface(color = androidx.compose.ui.graphics.Color.White, shadowElevation = 1.dp) {
            Row(Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("VELORA", fontSize = 21.sp, fontWeight = FontWeight.Bold, letterSpacing = 5.sp)
                Spacer(Modifier.weight(1f))
                Text("SHOP", modifier = Modifier.clickable { tab = "shop" }.padding(10.dp), fontSize = 12.sp, letterSpacing = 1.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tab = "cart" }.padding(8.dp)) {
                    Text("BAG", fontSize = 11.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.width(5.dp)); Text("CART ${cart.sumOf { it.quantity }}", fontSize = 12.sp)
                }
            }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                selected != null -> ProductDetailsScreen(selected!!, onBack = { vm.closeProduct() }, onAdd = { p, v -> vm.addToCart(p, v); tab = "cart" })
                tab == "cart" -> CartScreen(cart, onQty = vm::changeQuantity, onCheckout = { checkoutOpen = true })
                else -> ShopScreen(products, categories, search, onSearch = { search = it; vm.search(it) },
                    chosenCategory, onCategory = { chosenCategory = it; vm.filter(it) }, onProduct = vm::openProduct)
            }
            if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Ink)
            if (error != null) {
                Surface(Modifier.align(Alignment.BottomCenter).padding(12.dp), color = Ink, shape = RoundedCornerShape(10.dp)) {
                    Text(error!!, color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.padding(14.dp), fontSize = 13.sp)
                }
            }
            if (orderMessage != null && !checkoutOpen) {
                Surface(Modifier.align(Alignment.BottomCenter).padding(12.dp), color = Ink, shape = RoundedCornerShape(10.dp)) {
                    Text(orderMessage!!, color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.padding(14.dp), fontSize = 13.sp)
                }
            }
        }
    }
    if (checkoutOpen) CheckoutDialog(onDismiss = { checkoutOpen = false }, onSubmit = { n,e,p,c,pay ->
        vm.checkout(n,e,p,c,pay); checkoutOpen = false
    })
}

@Composable
private fun ShopScreen(
    products: List<VeloraApi.Product>, categories: List<VeloraApi.Category>, search: String,
    onSearch: (String) -> Unit, selectedCategory: Int?, onCategory: (Int?) -> Unit,
    onProduct: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(300.dp)) {
            AsyncImage("https://images.unsplash.com/photo-1529139574466-a303027c1d8b?w=1400", null,
                Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color(0x6600181A)))
            Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 26.dp)) {
                Text("NEW SEASON · 2026", color = androidx.compose.ui.graphics.Color.White, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Wear your\neveryday story.", color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 39.sp, lineHeight = 43.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Modern essentials. Thoughtful details.\nMade for your everyday.", color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp, lineHeight = 19.sp)
                Spacer(Modifier.height(17.dp))
                Surface(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.clickable { onCategory(null) }) {
                    Text("Explore collection", modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .5.sp)
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 27.dp)) {
            Text("THE VELORA EDIT", color = Accent, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Text("Shop the collection", fontFamily = FontFamily.Serif, fontSize = 29.sp, color = Ink, modifier = Modifier.padding(top = 5.dp, bottom = 15.dp))
            OutlinedTextField(value = search, onValueChange = onSearch, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search clothing…", fontSize = 13.sp) }, singleLine = true,
                shape = RoundedCornerShape(0.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, unfocusedBorderColor = Line))
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                FilterChip(selected = selectedCategory == null, onClick = { onCategory(null) }, label = { Text("All") })
                categories.forEach { c -> FilterChip(selected = selectedCategory == c.id, onClick = { onCategory(c.id) }, label = { Text(c.name, maxLines = 1) }) }
            }
            if (products.isEmpty()) Text("No products found", color = Muted, modifier = Modifier.padding(18.dp))
            products.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                    row.forEach { product -> ProductCard(product, Modifier.weight(1f)) { onProduct(product.id) } }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: VeloraApi.Product, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(2.dp)).background(androidx.compose.ui.graphics.Color.White).clickable(onClick = onClick)) {
        AsyncImage(product.imageUrl.ifBlank { "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=700" },
            contentDescription = product.name, modifier = Modifier.fillMaxWidth().height(205.dp), contentScale = ContentScale.Crop)
        Column(Modifier.padding(10.dp)) {
            Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(product.category, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
            Text(formatPrice(product.price), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 7.dp))
        }
    }
}

@Composable
private fun ProductDetailsScreen(details: VeloraApi.ProductDetails, onBack: () -> Unit, onAdd: (VeloraApi.Product, VeloraApi.Variant) -> Unit) {
    val firstVariant = details.variants.firstOrNull()
    var selectedSize by remember(details.product.id) { mutableStateOf(firstVariant?.size.orEmpty()) }
    var selectedColor by remember(details.product.id) { mutableStateOf(firstVariant?.color.orEmpty()) }
    val sizes = details.variants.map { it.size }.distinct()
    val colors = details.variants.map { it.color }.distinct()
    val selectedVariant = details.variants.firstOrNull { it.size == selectedSize && it.color == selectedColor }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onBack).padding(vertical = 8.dp)) {
            Icon(Icons.Default.ArrowBack, null); Text("  Back to shop", fontSize = 13.sp)
        }
        AsyncImage(details.product.imageUrl, details.product.name, Modifier.fillMaxWidth().height(390.dp), contentScale = ContentScale.Crop)
        Text(details.product.category.uppercase(), color = Accent, fontSize = 10.sp, letterSpacing = 2.sp, modifier = Modifier.padding(top = 18.dp))
        Text(details.product.name, fontFamily = FontFamily.Serif, fontSize = 30.sp, modifier = Modifier.padding(top = 5.dp))
        Text(formatPrice(details.product.price), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 7.dp))
        Text(details.product.description, color = Muted, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 14.dp))
        Text("CHOOSE SIZE AND COLOR", fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("SIZE", fontSize = 10.sp, letterSpacing = 1.sp, color = Muted, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 7.dp))
                sizes.forEach { size ->
                    val availableForSize = details.variants.any { it.size == size && it.color == selectedColor && it.stock > 0 }
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp).clip(RoundedCornerShape(3.dp))
                            .clickable {
                                selectedSize = size
                                if (details.variants.none { it.size == size && it.color == selectedColor }) {
                                    selectedColor = details.variants.firstOrNull { it.size == size }?.color.orEmpty()
                                }
                            },
                        color = if (selectedSize == size) Line else androidx.compose.ui.graphics.Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedSize == size) Accent else Line)
                    ) {
                        Text(size, modifier = Modifier.padding(horizontal = 10.dp, vertical = 11.dp), fontSize = 12.sp)
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                Text("COLOR", fontSize = 10.sp, letterSpacing = 1.sp, color = Muted, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 7.dp))
                colors.forEach { color ->
                    val availableForColor = details.variants.any { it.color == color && it.size == selectedSize && it.stock > 0 }
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp).clip(RoundedCornerShape(3.dp))
                            .clickable {
                                selectedColor = color
                                if (details.variants.none { it.color == color && it.size == selectedSize }) {
                                    selectedSize = details.variants.firstOrNull { it.color == color }?.size.orEmpty()
                                }
                            },
                        color = if (selectedColor == color) Line else androidx.compose.ui.graphics.Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedColor == color) Accent else Line)
                    ) {
                        Text(color, modifier = Modifier.padding(horizontal = 10.dp, vertical = 11.dp), fontSize = 12.sp)
                    }
                }
            }
        }
        if (selectedVariant != null) {
            Text(
                if (selectedVariant.stock > 0) "In stock: ${selectedVariant.stock}" else "Out of stock",
                color = if (selectedVariant.stock > 0) Muted else Accent,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
        } else {
            Text("This size and color combination is unavailable", color = Accent, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Button(onClick = { selectedVariant?.let { onAdd(details.product, it) } }, enabled = selectedVariant != null && selectedVariant.stock > 0,
            modifier = Modifier.fillMaxWidth().padding(top = 15.dp), colors = ButtonDefaults.buttonColors(containerColor = Ink), shape = RoundedCornerShape(0.dp)) {
            Text("ADD TO CART", letterSpacing = 1.sp)
        }
        Text("CUSTOMER REVIEWS", fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 28.dp, bottom = 8.dp))
        details.reviews.forEach { r ->
            Text("${"★".repeat(r.rating)}  ${r.author}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(r.comment, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(bottom = 10.dp))
        }
        if (details.reviews.isEmpty()) Text("No reviews yet", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun CartScreen(cart: List<VeloraViewModel.CartEntry>, onQty: (Int, Int) -> Unit, onCheckout: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("YOUR CART", color = Accent, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Text("Shopping bag", fontFamily = FontFamily.Serif, fontSize = 30.sp, modifier = Modifier.padding(bottom = 20.dp))
        if (cart.isEmpty()) Text("Your bag is currently empty.", color = Muted, fontSize = 14.sp)
        cart.forEach { entry ->
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(entry.product.imageUrl, null, Modifier.size(88.dp).clip(RoundedCornerShape(2.dp)), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(entry.product.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("${entry.variant.size} · ${entry.variant.color}", color = Muted, fontSize = 11.sp)
                    Text(formatPrice(entry.product.price * entry.quantity), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("−", Modifier.clickable { onQty(entry.variant.id, entry.quantity - 1) }.padding(8.dp))
                        Text("${entry.quantity}")
                        Text("+", Modifier.clickable { if (entry.quantity < entry.variant.stock) onQty(entry.variant.id, entry.quantity + 1) }.padding(8.dp))
                    }
                }
                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.clickable { onQty(entry.variant.id, 0) }.padding(8.dp))
            }
            HorizontalDivider(color = Line)
        }
        val total = cart.sumOf { it.product.price * it.quantity }
        Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal", fontSize = 14.sp); Text(formatPrice(total), fontWeight = FontWeight.Bold)
        }
        Button(onClick = onCheckout, enabled = cart.isNotEmpty(), modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink), shape = RoundedCornerShape(0.dp)) { Text("PROCEED TO CHECKOUT") }
    }
}

@Composable
private fun CheckoutDialog(onDismiss: () -> Unit, onSubmit: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }; var city by remember { mutableStateOf("Tashkent") }
    var payment by remember { mutableStateOf("cash") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Checkout", fontFamily = FontFamily.Serif, fontSize = 25.sp) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Full name") }, singleLine = true)
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(city, { city = it }, label = { Text("City") }, singleLine = true)
                Text("Payment method", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                listOf("cash" to "Cash", "card" to "Card", "payme" to "Payme", "click" to "Click").forEach { (value, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(payment == value, { payment = value }); Text(label, fontSize = 13.sp) }
                }
            }
        }, confirmButton = { TextButton(onClick = { if (name.isNotBlank() && email.contains("@")) onSubmit(name, email, phone, city, payment) }) { Text("PLACE ORDER") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } })
}

private fun formatPrice(value: Double): String = "${"%,.0f".format(value)} сум"
