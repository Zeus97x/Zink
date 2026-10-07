package eu.kanade.presentation.library

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.Locale

class DuplicateTitleKeyTest {
    @Test
    fun normalizesCaseSpacingAndPunctuation() {
        assertEquals(duplicateTitleKey("A Big Shot Actually"), duplicateTitleKey("  A BIG Shot--Actually! "))
        assertEquals(duplicateTitleKey("Title 2"), duplicateTitleKey("Ｔｉｔｌｅ ２"))
    }

    @Test
    fun keepsEditionsAndDifferentTitlesSeparate() {
        assertNotEquals(duplicateTitleKey("Above Ten Thousand People"), duplicateTitleKey("Above Ten Thousand People (Colored)"))
        assertNotEquals(duplicateTitleKey("Title 1"), duplicateTitleKey("Title 2"))
        assertNotEquals(duplicateTitleKey("Lord of Mysteries"), duplicateTitleKey("Lord of the Mysteries"))
        assertEquals("", duplicateTitleKey(" -- "))
    }

    @Test
    fun isIndependentOfDeviceLocale() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"))
            assertEquals("title", duplicateTitleKey("TITLE"))
        } finally {
            Locale.setDefault(previous)
        }
    }
}
