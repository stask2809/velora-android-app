package uz.savdo.shop.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.savdo.shop.ShopApp
import uz.savdo.shop.data.CartLine
import uz.savdo.shop.data.Order
import uz.savdo.shop.data.Product
import uz.savdo.shop.data.ShopRepository
import uz.savdo.shop.data.User

/** Holds screen state and survives screen rotation. Screens call its functions. */
@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModel(private val repo: ShopRepository) : ViewModel() {

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()     // true after start-up work

    val currentUser: StateFlow<User?> = repo.currentUser

    // search + filter inputs from the Home screen
    val query = MutableStateFlow("")
    val category = MutableStateFlow<String?>(null)

    val products: StateFlow<List<Product>> =
        combine(query, category) { q, c -> q to c }
            .flatMapLatest { (q, c) -> repo.products(q, c) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<String>> = repo.categories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cart: StateFlow<List<CartLine>> = currentUser
        .flatMapLatest { u -> if (u == null) flowOf(emptyList()) else repo.cart(u.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cartTotal: StateFlow<Long> = cart.map { lines -> lines.sumOf { it.lineTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val orders: StateFlow<List<Order>> = currentUser
        .flatMapLatest { u -> if (u == null) flowOf(emptyList()) else repo.orders(u.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repo.seedIfEmpty()
            repo.restoreSession()
            _ready.value = true
        }
    }

    fun product(id: Long): Flow<Product?> = repo.product(id)

    // onResult(null) = success, onResult("text") = error message to show
    fun login(email: String, password: String, onResult: (String?) -> Unit) = viewModelScope.launch {
        onResult(repo.login(email, password).exceptionOrNull()?.message)
    }

    fun register(name: String, email: String, password: String, onResult: (String?) -> Unit) =
        viewModelScope.launch {
            onResult(repo.register(name, email, password).exceptionOrNull()?.message)
        }

    fun logout() = repo.logout()

    fun addToCart(productId: Long) = viewModelScope.launch { repo.addToCart(productId) }

    fun setQuantity(productId: Long, quantity: Int) =
        viewModelScope.launch { repo.setQuantity(productId, quantity) }

    fun checkout(address: String, paymentMethod: String, onResult: (Result<Long>) -> Unit) =
        viewModelScope.launch {
            onResult(runCatching { repo.checkout(address, paymentMethod) })
        }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ShopApp
                ShopViewModel(app.repository)
            }
        }
    }
}
