package com.example.cryptotracker.data.util

import org.junit.Assert.*
import org.junit.Test

class ResultTest {

    @Test
    fun `Success result has correct data`() {
        // Arrange
        val testData = "Test Data"
        
        // Act
        val result = Result.Success(testData)
        
        // Assert
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertFalse(result.isLoading)
        assertEquals(testData, result.data)
        assertEquals(testData, result.getOrNull())
    }
    
    @Test
    fun `Error result has correct message and exception`() {
        // Arrange
        val errorMessage = "Error message"
        val exception = RuntimeException("Test exception")
        
        // Act
        val result = Result.Error(errorMessage, exception)
        
        // Assert
        assertTrue(result.isError)
        assertFalse(result.isSuccess)
        assertFalse(result.isLoading)
        assertEquals(errorMessage, result.message)
        assertEquals(exception, result.exception)
        assertNull(result.getOrNull())
    }
    
    @Test
    fun `Loading result has correct state`() {
        // Act
        val result = Result.Loading
        
        // Assert
        assertTrue(result.isLoading)
        assertFalse(result.isSuccess)
        assertFalse(result.isError)
        assertNull(result.getOrNull())
    }
    
    @Test
    fun `getOrNull returns null for Error and Loading results`() {
        // Arrange
        val successResult = Result.Success("Test")
        val errorResult = Result.Error("Error")
        val loadingResult = Result.Loading
        
        // Assert
        assertEquals("Test", successResult.getOrNull())
        assertNull(errorResult.getOrNull())
        assertNull(loadingResult.getOrNull())
    }
}
