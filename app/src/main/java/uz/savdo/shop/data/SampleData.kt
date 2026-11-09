package uz.savdo.shop.data

// Inserted the first time the app starts (when the products table is empty)
object SampleData {
    val products = listOf(
        Product(name = "Atlas silk dress", category = "Clothing", price = 650_000, stock = 15, emoji = "👗",
            rating = 4.8, description = "Hand-woven Margilan atlas pattern, sizes S-XL."),
        Product(name = "Cotton shirt", category = "Clothing", price = 180_000, stock = 40, emoji = "👕",
            rating = 4.3, description = "100% Uzbek cotton, slim fit."),
        Product(name = "Leather sneakers", category = "Shoes", price = 420_000, stock = 25, emoji = "👟",
            rating = 4.6, description = "Genuine leather, sizes 39-45."),
        Product(name = "Classic boots", category = "Shoes", price = 560_000, stock = 12, emoji = "🥾",
            rating = 4.4, description = "Waterproof winter boots."),
        Product(name = "Argan hair oil", category = "Beauty", price = 95_000, stock = 60, emoji = "🧴",
            rating = 4.5, description = "50 ml natural hair oil."),
        Product(name = "Rose lipstick", category = "Beauty", price = 75_000, stock = 80, emoji = "💄",
            rating = 4.1, description = "Long-lasting matte colour."),
        Product(name = "Silver earrings", category = "Jewelry", price = 310_000, stock = 20, emoji = "💎",
            rating = 4.7, description = "925 silver with turquoise."),
        Product(name = "Gold ring 585", category = "Jewelry", price = 2_400_000, stock = 5, emoji = "💍",
            rating = 4.9, description = "585 gold, 2.1 g, gift box included."),
        Product(name = "Plov set for 4", category = "Food", price = 160_000, stock = 30, emoji = "🍛",
            rating = 4.9, description = "Tashkent wedding plov, delivered hot."),
        Product(name = "Samsa (6 pcs)", category = "Food", price = 48_000, stock = 100, emoji = "🥟",
            rating = 4.6, description = "Tandoor samsa with beef and onion.")
    )
}
