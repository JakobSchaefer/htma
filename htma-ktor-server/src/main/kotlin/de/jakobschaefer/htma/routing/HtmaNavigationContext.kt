package de.jakobschaefer.htma.routing

import kotlinx.serialization.Serializable

@Serializable
data class HtmaNavigationContext(val target: String)
