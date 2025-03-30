package de.jakobschaefer.htma.routing

import io.ktor.server.routing.*

typealias HtmaDataLoadFunction = suspend RoutingContext.() -> Any?

