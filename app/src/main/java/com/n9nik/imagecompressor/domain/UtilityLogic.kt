package com.n9nik.imagecompressor.domain

/** Legacy wrapper for tests / template compat - delegates to ImageCompressor for string case */
object UtilityLogic {
    fun transform(input: String): String = input.trim().replace(Regex("\\s+"), " ")
}
