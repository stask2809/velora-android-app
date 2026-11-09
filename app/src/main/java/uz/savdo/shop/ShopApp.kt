package uz.savdo.shop

import android.app.Application
import android.content.Context
import uz.savdo.shop.data.AppDatabase
import uz.savdo.shop.data.ShopRepository

/** Created once when the app starts. Holds objects the whole app shares. */
class ShopApp : Application() {
    val repository: ShopRepository by lazy {
        ShopRepository(
            db = AppDatabase.get(this),
            prefs = getSharedPreferences("session", Context.MODE_PRIVATE)
        )
    }
}
