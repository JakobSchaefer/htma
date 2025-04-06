package de.jakobschaefer.htma.routing

@HtmaRoutingDsl
interface HtmaRoutingDslBuilder<T> {
  fun build(): T
}
