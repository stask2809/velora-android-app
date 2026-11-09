package uz.savdo.shop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Change these 3 colours to give your app its own brand
private val Brand = Color(0xFF1F3A5F)
private val Accent = Color(0xFFE67E22)
private val Soft = Color(0xFFE8EEF6)

private val LightColors = lightColorScheme(
    primary = Brand, secondary = Accent, primaryContainer = Soft, onPrimaryContainer = Brand
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF9EC1F0), secondary = Accent
)

@Composable
fun SavdoShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
