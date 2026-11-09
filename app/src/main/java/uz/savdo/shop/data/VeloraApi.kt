package uz.savdo.shop.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** VELORA cloud API. The Android app talks only to the existing Express API;
 * database credentials never live in the APK. */
object VeloraApi {
    const val BASE_URL = "https://velora-bigdata-project.onrender.com"

    data class Category(val id: Int, val name: String)
    data class Product(
        val id: Int, val name: String, val description: String,
        val price: Double, val imageUrl: String, val category: String, val stock: Int
    )
    data class Variant(val id: Int, val size: String, val color: String, val stock: Int)
    data class Review(val author: String, val rating: Int, val comment: String)
    data class ProductDetails(val product: Product, val variants: List<Variant>, val reviews: List<Review>, val avgRating: Double?)
    data class OrderResult(val orderId: Int, val total: Double)

    suspend fun categories(): List<Category> = (request("/api/categories") as JSONArray).let { arr ->
        (0 until arr.length()).map { i -> arr.getJSONObject(i).let { Category(it.optInt("category_id"), it.optString("name")) } }
    }

    suspend fun products(query: String = "", categoryId: Int? = null): List<Product> {
        val params = buildList {
            if (query.isNotBlank()) add("q=" + java.net.URLEncoder.encode(query, "UTF-8"))
            if (categoryId != null) add("category=$categoryId")
        }.joinToString("&")
        val arr = request("/api/products" + if (params.isNotEmpty()) "?$params" else "") as JSONArray
        return (0 until arr.length()).map { i -> parseProduct(arr.getJSONObject(i)) }
    }

    suspend fun product(id: Int): ProductDetails {
        val obj = request("/api/products/$id") as JSONObject
        val variantsJson = obj.optJSONArray("variants") ?: JSONArray()
        val reviewsJson = obj.optJSONArray("reviews") ?: JSONArray()
        val variants = (0 until variantsJson.length()).map { i -> variantsJson.getJSONObject(i).let {
            Variant(it.optInt("variant_id"), it.optString("size"), it.optString("color"), it.optInt("stock"))
        } }
        val reviews = (0 until reviewsJson.length()).map { i -> reviewsJson.getJSONObject(i).let {
            Review(it.optString("author", "Guest"), it.optInt("rating"), it.optString("comment"))
        } }
        return ProductDetails(parseProduct(obj), variants, reviews,
            if (obj.isNull("avgRating")) null else obj.optDouble("avgRating"))
    }

    suspend fun placeOrder(
        fullName: String, email: String, phone: String, city: String,
        items: List<Pair<Int, Int>>, paymentMethod: String
    ): OrderResult {
        val body = JSONObject().apply {
            put("customer", JSONObject().apply {
                put("fullName", fullName); put("email", email); put("phone", phone); put("city", city)
            })
            put("items", JSONArray().apply { items.forEach { (variantId, quantity) ->
                put(JSONObject().put("variantId", variantId).put("quantity", quantity))
            } })
            put("paymentMethod", paymentMethod)
        }
        val result = request("/api/orders", "POST", body) as JSONObject
        return OrderResult(result.optInt("orderId"), result.optDouble("total"))
    }

    suspend fun health(): JSONObject = request("/api/health") as JSONObject

    private fun parseProduct(obj: JSONObject) = Product(
        id = obj.optInt("product_id"), name = obj.optString("name"),
        description = obj.optString("description"), price = obj.optDouble("price"),
        imageUrl = obj.optString("image_url"), category = obj.optString("category"),
        stock = obj.optInt("stock")
    )

    private suspend fun request(path: String, method: String = "GET", body: JSONObject? = null): Any =
        withContext(Dispatchers.IO) {
            val connection = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 20000
                readTimeout = 25000
                setRequestProperty("Accept", "application/json")
                if (body != null) {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                }
            }
            try {
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (code !in 200..299) {
                    val message = runCatching { JSONObject(text).optString("error") }.getOrNull()
                    throw IllegalStateException(message?.takeIf { it.isNotBlank() } ?: "Server error ($code)")
                }
                when (text.trim().firstOrNull()) {
                    '[' -> JSONArray(text)
                    '{' -> JSONObject(text)
                    else -> text
                }
            } finally { connection.disconnect() }
        }
}
