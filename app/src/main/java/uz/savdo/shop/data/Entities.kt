package uz.savdo.shop.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Each @Entity class becomes one table in the SQLite database.

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,          // never store the plain password!
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val description: String,
    val price: Long,                   // in so'm (whole numbers, no decimals)
    val stock: Int,
    val emoji: String,                 // simple offline "image"
    val rating: Double = 0.0
)

@Entity(
    tableName = "cart_items",
    primaryKeys = ["userId", "productId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Product::class, parentColumns = ["id"], childColumns = ["productId"])
    ],
    indices = [Index("productId")]
)
data class CartItem(
    val userId: Long,
    val productId: Long,
    val quantity: Int
)

@Entity(
    tableName = "orders",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index("userId")]
)
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val total: Long,
    val address: String,
    val paymentMethod: String,
    val status: String = "NEW",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_items",
    primaryKeys = ["orderId", "productId"],
    foreignKeys = [ForeignKey(entity = Order::class, parentColumns = ["id"], childColumns = ["orderId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index("productId")]
)
data class OrderItem(
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Long
)

// Not a table: the result of a JOIN between cart_items and products
data class CartLine(
    @Embedded val product: Product,
    val quantity: Int
) {
    val lineTotal: Long get() = product.price * quantity
}
