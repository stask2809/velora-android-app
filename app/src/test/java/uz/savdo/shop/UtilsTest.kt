package uz.savdo.shop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.savdo.shop.data.CartLine
import uz.savdo.shop.data.Product
import uz.savdo.shop.util.Security
import uz.savdo.shop.util.Validators
import uz.savdo.shop.util.formatSom

class UtilsTest {
    private fun product(price: Long) = Product(name = "Test", category = "X", description = "",
        price = price, stock = 10, emoji = "🧪")

    @Test fun formatSom_addsSpaces() {
        assertEquals("650 000 so'm", formatSom(650_000))
        assertEquals("2 400 000 so'm", formatSom(2_400_000))
    }

    @Test fun cartTotal_isSumOfLines() {
        val lines = listOf(CartLine(product(100_000), 2), CartLine(product(50_000), 3))
        assertEquals(350_000L, lines.sumOf { it.lineTotal })
    }

    @Test fun email_validation() {
        assertTrue(Validators.isValidEmail("aziza@example.uz"))
        assertFalse(Validators.isValidEmail("aziza@"))
        assertFalse(Validators.isValidEmail("no spaces@mail.uz"))
    }

    @Test fun password_rules() {
        assertTrue(Validators.isStrongPassword("savdo2026"))
        assertFalse(Validators.isStrongPassword("short"))
        assertFalse(Validators.isStrongPassword("nodigitshere"))
    }

    @Test fun hash_isStable_andSalted() {
        val a = Security.hashPassword("secret1", "a@x.uz")
        assertEquals(a, Security.hashPassword("secret1", "a@x.uz"))
        assertNotEquals(a, Security.hashPassword("secret1", "b@x.uz"))
        assertEquals(64, a.length)
    }
}
