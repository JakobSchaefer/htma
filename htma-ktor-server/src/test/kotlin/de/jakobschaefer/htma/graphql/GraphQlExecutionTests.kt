package de.jakobschaefer.htma.graphql

import com.apollographql.apollo.api.Optional
import de.jakobschaefer.htma.graphql.type.SetNameOptions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.thymeleaf.context.Context
import org.thymeleaf.context.IContext
import java.util.concurrent.ConcurrentHashMap

class GraphQlExecutionTests : FunSpec({
  test("simple operation instantiation") {
    val execution = buildTestExecution(
      operation = GraphQlOperationRef(
        serviceName = "graphql",
        operationName = "TestQuery",
        variables = mapOf(
          "name" to "Testing"
        )
      ),
      context = Context(),
    )

    val operation = execution.buildOperation<TestQuery>()
    operation shouldBe TestQuery(
      name = "Testing"
    )
  }

  test("parameters can be apollo optionals") {
    val execution = buildTestExecution(
      operation = GraphQlOperationRef(
        serviceName = "graphql",
        operationName = "OptionalTestQuery",
        variables = mapOf(
          "age" to 30
        )
      ),
      context = Context(),
    )
    val operation = execution.buildOperation<OptionalTestQuery>()
    operation shouldBe OptionalTestQuery(
      name = Optional.absent(),
      age = Optional.present(30)
    )
  }

  test("parameters can be complex input types") {
    val execution = buildTestExecution(
      operation = GraphQlOperationRef(
        serviceName = "graphql",
        operationName = "ComplexTestQuery",
        variables = mapOf(
          "name" to "complex test query",
        )
      ),
      context = Context(),
    )
    val operation = execution.buildOperation<ComplexTestQuery>()
    operation shouldBe ComplexTestQuery(
      name = "complex test query",
      options = Optional.absent(),
    )
  }
})

private fun buildTestExecution(operation: GraphQlOperationRef, context: IContext): GraphQlExecution {
  return GraphQlExecution(
    operation = operation,
    context = context,
    services = emptyMap(),
    cache = GraphQlExecutionCache(entries = ConcurrentHashMap()),
    packageName = "de.jakobschaefer.htma"
  )
}

data class TestQuery(
  val name: String
)

data class OptionalTestQuery(
  val name: Optional<String?> = Optional.Absent,
  val age: Optional<Int?> = Optional.Absent,
)

data class ComplexTestQuery(
  val name: String,
  val options: Optional<SetNameOptions?> = Optional.Absent,
)
