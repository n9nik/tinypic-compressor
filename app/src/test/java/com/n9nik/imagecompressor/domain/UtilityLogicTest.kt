package com.n9nik.imagecompressor.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilityLogicTest {
    @Test
    fun transform_trimsAndCollapsesWhitespace() {
        assertEquals("hello world", UtilityLogic.transform("  hello   world  "))
    }

    @Test
    fun imageCompressor_formatBytes_works() {
        assertEquals("500 B", ImageCompressor.formatBytes(500))
        assertTrue(ImageCompressor.formatBytes(2048).contains("KB"))
        assertTrue(ImageCompressor.formatBytes(3 * 1024 * 1024).contains("MB"))
    }
}
