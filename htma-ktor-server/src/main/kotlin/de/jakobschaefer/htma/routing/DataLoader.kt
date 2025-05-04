package de.jakobschaefer.htma.routing

data class DataLoader(
  val canonicalPath: String
)

class DataLoaderBuilder(
  val canonicalPath: String,
) : HtmaRoutingDslBuilder<DataLoader> {

  override fun build(): DataLoader {
    return DataLoader(canonicalPath)
  }
}
