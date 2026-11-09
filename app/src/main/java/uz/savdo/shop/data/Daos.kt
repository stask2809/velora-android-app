package uz.savdo.shop.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// A DAO (Data Access Object) lists the SQL queries the app can run.
// Flow<...> = live data: the UI updates automatically when the table changes.
// suspend   = runs in a coroutine, never blocks the screen.

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun findById(id: Long): User?
}

@Dao
interface ProductDao {
    @Query(
        """SELECT * FROM products
           WHERE (:category IS NULL OR category = :category)
             AND name LIKE '%' || :query || '%'
           ORDER BY name"""
    )
    fun search(query: String, category: String?): Flow<List<Product>>

    @Query("SELECT DISTINCT category FROM products ORDER BY category")
    fun categories(): Flow<List<String>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observe(id: Long): Flow<Product?>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(products: List<Product>)
}

@Dao
interface CartDao {
    @Query(
        """SELECT p.*, c.quantity FROM cart_items c
           JOIN products p ON p.id = c.productId
           WHERE c.userId = :userId ORDER BY p.name"""
    )
    fun observeCart(userId: Long): Flow<List<CartLine>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun find(userId: Long, productId: Long): CartItem?

    @Upsert                      // insert, or update if the primary key already exists
    suspend fun upsert(item: CartItem)

    @Query("DELETE FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun remove(userId: Long, productId: Long)
}

class OutOfStockException(productName: String) : Exception("Not enough \"$productName\" in stock")

@Dao
abstract class OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    abstract fun observeOrders(userId: Long): Flow<List<Order>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    abstract suspend fun itemsOf(orderId: Long): List<OrderItem>

    @Query(
        """SELECT p.*, c.quantity FROM cart_items c
           JOIN products p ON p.id = c.productId WHERE c.userId = :userId"""
    )
    protected abstract suspend fun cartLines(userId: Long): List<CartLine>

    @Insert
    protected abstract suspend fun insertOrder(order: Order): Long

    @Insert
    protected abstract suspend fun insertItems(items: List<OrderItem>)

    // returns number of changed rows: 0 means "not enough stock"
    @Query("UPDATE products SET stock = stock - :qty WHERE id = :productId AND stock >= :qty")
    protected abstract suspend fun decreaseStock(productId: Long, qty: Int): Int

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    protected abstract suspend fun clearCart(userId: Long)

    /** Checkout: ALL steps succeed, or (if an exception is thrown) NOTHING is saved. */
    @Transaction
    open suspend fun placeOrder(userId: Long, address: String, paymentMethod: String): Long {
        val lines = cartLines(userId)
        require(lines.isNotEmpty()) { "Cart is empty" }
        val orderId = insertOrder(
            Order(userId = userId, total = lines.sumOf { it.lineTotal },
                address = address, paymentMethod = paymentMethod)
        )
        for (line in lines) {
            if (decreaseStock(line.product.id, line.quantity) == 0) {
                throw OutOfStockException(line.product.name)   // rolls back the whole order
            }
        }
        insertItems(lines.map {
            OrderItem(orderId, it.product.id, it.product.name, it.quantity, it.product.price)
        })
        clearCart(userId)
        return orderId
    }
}
