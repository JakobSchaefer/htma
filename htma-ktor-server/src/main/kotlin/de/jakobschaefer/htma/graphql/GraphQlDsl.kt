package de.jakobschaefer.htma.graphql

@DslMarker
annotation class GraphQlDsl

@GraphQlDsl
interface GraphQlDslBuilder<T> {
  fun build(): T
}
