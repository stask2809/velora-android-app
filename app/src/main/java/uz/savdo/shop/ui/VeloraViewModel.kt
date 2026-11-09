package uz.savdo.shop.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.savdo.shop.data.VeloraApi

class VeloraViewModel : ViewModel() {
    private val _categories = MutableStateFlow<List<VeloraApi.Category>>(emptyList())
    val categories: StateFlow<List<VeloraApi.Category>> = _categories.asStateFlow()
    private val _products = MutableStateFlow<List<VeloraApi.Product>>(emptyList())
    val products: StateFlow<List<VeloraApi.Product>> = _products.asStateFlow()
    private val _selected = MutableStateFlow<VeloraApi.ProductDetails?>(null)
    val selected: StateFlow<VeloraApi.ProductDetails?> = _selected.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _cart = MutableStateFlow<List<CartEntry>>(emptyList())
    val cart: StateFlow<List<CartEntry>> = _cart.asStateFlow()
    private val _orderMessage = MutableStateFlow<String?>(null)
    val orderMessage: StateFlow<String?> = _orderMessage.asStateFlow()
    private var query = ""
    private var categoryId: Int? = null

    data class CartEntry(val product: VeloraApi.Product, val variant: VeloraApi.Variant, val quantity: Int)

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _loading.value = true; _error.value = null
        try {
            _categories.value = VeloraApi.categories()
            _products.value = VeloraApi.products(query, categoryId)
        } catch (e: Exception) { _error.value = e.message ?: "Не удалось загрузить каталог" }
        finally { _loading.value = false }
    }

    fun search(value: String) { query = value; refresh() }
    fun filter(id: Int?) { categoryId = id; refresh() }

    fun closeProduct() { _selected.value = null }

    fun openProduct(id: Int) = viewModelScope.launch {
        _loading.value = true; _error.value = null
        try { _selected.value = VeloraApi.product(id) }
        catch (e: Exception) { _error.value = e.message ?: "Не удалось загрузить товар" }
        finally { _loading.value = false }
    }

    fun addToCart(product: VeloraApi.Product, variant: VeloraApi.Variant) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.variant.id == variant.id }
        if (index >= 0) current[index] = current[index].copy(quantity = current[index].quantity + 1)
        else current.add(CartEntry(product, variant, 1))
        _cart.value = current
        _orderMessage.value = "Добавлено в корзину"
        _selected.value = null
    }

    fun changeQuantity(variantId: Int, quantity: Int) {
        _cart.value = _cart.value.mapNotNull { entry ->
            if (entry.variant.id != variantId) entry
            else if (quantity <= 0) null else entry.copy(quantity = quantity)
        }
    }

    fun checkout(name: String, email: String, phone: String, city: String, payment: String) = viewModelScope.launch {
        _loading.value = true; _error.value = null; _orderMessage.value = null
        try {
            val entries = _cart.value
            if (entries.isEmpty()) throw IllegalStateException("Корзина пуста")
            // Existing backend accepts multiple variant lines in one transactional request.
            val result = VeloraApi.placeOrder(name, email, phone, city,
                entries.map { it.variant.id to it.quantity }, payment)
            _cart.value = emptyList()
            _orderMessage.value = "Заказ №${result.orderId} оформлен. Сумма: ${"%.0f".format(result.total)} сум"
        } catch (e: Exception) { _error.value = e.message ?: "Ошибка оформления заказа" }
        finally { _loading.value = false }
    }

    fun clearMessages() { _error.value = null; _orderMessage.value = null }
}
