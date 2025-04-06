package de.jakobschaefer.htma.examples.basic

import kotlinx.serialization.Serializable

@Serializable
data class Session(val count: Int = 0)
