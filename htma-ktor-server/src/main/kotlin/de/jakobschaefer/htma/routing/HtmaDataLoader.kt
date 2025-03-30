package de.jakobschaefer.htma.routing

class HtmaDataLoader(
  val name: String,
  val canonicalPath: String,
  val load: HtmaDataLoadFunction
) {
  val canonicalPathSegments = canonicalPath.split(".")
}
