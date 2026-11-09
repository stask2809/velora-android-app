package uz.savdo.shop.util

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 650000 -> "650 000 so'm" */
fun formatSom(amount: Long): String =
    String.format(Locale.US, "%,d", amount).replace(',', ' ') + " so'm"

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(millis))

object Validators {
    private val emailRegex = Regex("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")
    fun isValidEmail(email: String) = emailRegex.matches(email.trim())
    fun isStrongPassword(password: String) = password.length >= 6 && password.any { it.isDigit() }
}

object Security {
    /** SHA-256 hash with the e-mail as salt. Good for learning; use Firebase Auth / bcrypt in production. */
    fun hashPassword(password: String, salt: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest("$salt:$password".toByteArray())
            .joinToString("") { "%02x".format(it) }
}
