package com.ualachallenge.ui.screens

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OrientationLogicTest {

    @Test
    fun `Given landscape dimensions, When checking orientation, Then should return landscape`() {
        // Given
        val screenWidthDp = 800
        val screenHeightDp = 400

        // When
        val isLandscape = screenWidthDp > screenHeightDp

        // Then
        assertTrue(isLandscape)
    }

    @Test
    fun `Given portrait dimensions, When checking orientation, Then should return portrait`() {
        // Given
        val screenWidthDp = 400
        val screenHeightDp = 800

        // When
        val isLandscape = screenWidthDp > screenHeightDp

        // Then
        assertFalse(isLandscape)
    }

    @Test
    fun `Given square dimensions, When checking orientation, Then should return portrait`() {
        // Given
        val screenWidthDp = 600
        val screenHeightDp = 600

        // When
        val isLandscape = screenWidthDp > screenHeightDp

        // Then
        assertFalse(isLandscape)
    }

    @Test
    fun `Given landscape orientation, When calculating max items, Then should return 5`() {
        // Given
        val isLandscape = true

        // When
        val maxItems = if (isLandscape) 5 else 12

        // Then
        assertEquals(5, maxItems)
    }

    @Test
    fun `Given portrait orientation, When calculating max items, Then should return 12`() {
        // Given
        val isLandscape = false

        // When
        val maxItems = if (isLandscape) 5 else 12

        // Then
        assertEquals(12, maxItems)
    }

    @Test
    fun `Given landscape orientation, When calculating visible letters range, Then should center around current`() {
        // Given
        val availableLetters =
            listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
        val currentLetterIndex = 12 // "M"
        val maxItems = 5

        // When
        val startIndex = maxOf(0, currentLetterIndex - (maxItems / 2)) // 12 - 2 = 10
        val endIndex = minOf(availableLetters.size, startIndex + maxItems) // min(26, 10 + 5) = 15
        val adjustedStartIndex = maxOf(0, endIndex - maxItems) // max(0, 15 - 5) = 10
        val visibleLetters = availableLetters.subList(adjustedStartIndex, endIndex)

        // Then
        assertEquals(5, visibleLetters.size)
        assertEquals("K", visibleLetters[0])
        assertEquals("L", visibleLetters[1])
        assertEquals("M", visibleLetters[2])
        assertEquals("N", visibleLetters[3])
        assertEquals("O", visibleLetters[4])
    }

    @Test
    fun `Given portrait orientation, When calculating visible letters range, Then should center around current`() {
        // Given
        val availableLetters =
            listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
        val currentLetterIndex = 12 // "M"
        val maxItems = 12

        // When
        val startIndex = maxOf(0, currentLetterIndex - (maxItems / 2)) // 12 - 6 = 6
        val endIndex = minOf(availableLetters.size, startIndex + maxItems) // min(26, 6 + 12) = 18
        val adjustedStartIndex = maxOf(0, endIndex - maxItems) // max(0, 18 - 12) = 6
        val visibleLetters = availableLetters.subList(adjustedStartIndex, endIndex)

        // Then
        assertEquals(12, visibleLetters.size)
        assertEquals("G", visibleLetters[0])
        assertEquals("H", visibleLetters[1])
        assertEquals("I", visibleLetters[2])
        assertEquals("J", visibleLetters[3])
        assertEquals("K", visibleLetters[4])
        assertEquals("L", visibleLetters[5])
        assertEquals("M", visibleLetters[6])
        assertEquals("N", visibleLetters[7])
        assertEquals("O", visibleLetters[8])
        assertEquals("P", visibleLetters[9])
        assertEquals("Q", visibleLetters[10])
        assertEquals("R", visibleLetters[11])
    }

    @Test
    fun `Given current letter at start, When calculating visible letters range, Then should show first items`() {
        // Given
        val availableLetters =
            listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
        val currentLetterIndex = 0 // "A"
        val maxItems = 5

        // When
        val startIndex = maxOf(0, currentLetterIndex - (maxItems / 2)) // 0 - 2 = 0
        val endIndex = minOf(availableLetters.size, startIndex + maxItems) // min(26, 0 + 5) = 5
        val adjustedStartIndex = maxOf(0, endIndex - maxItems) // max(0, 5 - 5) = 0
        val visibleLetters = availableLetters.subList(adjustedStartIndex, endIndex)

        // Then
        assertEquals(5, visibleLetters.size)
        assertEquals("A", visibleLetters[0])
        assertEquals("B", visibleLetters[1])
        assertEquals("C", visibleLetters[2])
        assertEquals("D", visibleLetters[3])
        assertEquals("E", visibleLetters[4])
    }

    @Test
    fun `Given current letter at end, When calculating visible letters range, Then should show last items`() {
        // Given
        val availableLetters =
            listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
        val currentLetterIndex = 25 // "Z"
        val maxItems = 5

        // When
        val startIndex = maxOf(0, currentLetterIndex - (maxItems / 2)) // 25 - 2 = 23
        val endIndex = minOf(availableLetters.size, startIndex + maxItems) // min(26, 23 + 5) = 26
        val adjustedStartIndex = maxOf(0, endIndex - maxItems) // max(0, 26 - 5) = 21
        val visibleLetters = availableLetters.subList(adjustedStartIndex, endIndex)

        // Then
        assertEquals(5, visibleLetters.size)
        assertEquals("V", visibleLetters[0])
        assertEquals("W", visibleLetters[1])
        assertEquals("X", visibleLetters[2])
        assertEquals("Y", visibleLetters[3])
        assertEquals("Z", visibleLetters[4])
    }

    @Test
    fun `Given current letter not found, When calculating visible letters range, Then should show first items`() {
        // Given
        val availableLetters =
            listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
        val currentLetterIndex = -1 // Not found
        val maxItems = 5

        // When
        val visibleLetters = if (currentLetterIndex == -1) {
            availableLetters.take(maxItems)
        } else {
            val startIndex = maxOf(0, currentLetterIndex - (maxItems / 2))
            val endIndex = minOf(availableLetters.size, startIndex + maxItems)
            val adjustedStartIndex = maxOf(0, endIndex - maxItems)
            availableLetters.subList(adjustedStartIndex, endIndex)
        }

        // Then
        assertEquals(5, visibleLetters.size)
        assertEquals("A", visibleLetters[0])
        assertEquals("B", visibleLetters[1])
        assertEquals("C", visibleLetters[2])
        assertEquals("D", visibleLetters[3])
        assertEquals("E", visibleLetters[4])
    }
}
