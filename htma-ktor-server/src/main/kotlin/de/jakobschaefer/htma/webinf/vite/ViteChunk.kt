package de.jakobschaefer.htma.webinf.vite

import kotlinx.serialization.Serializable

@Serializable
data class ViteChunk(
    val file: String,
    val src: String,
    val isEntry: Boolean = false,
    val css: List<String> = emptyList()
)
