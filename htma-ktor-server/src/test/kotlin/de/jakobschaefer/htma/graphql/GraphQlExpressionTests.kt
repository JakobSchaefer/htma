package de.jakobschaefer.htma.graphql

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GraphQlExpressionTests : FunSpec({
  test("simple assignments") {
    val expr = "variable=~{service::test}"
    val result = GraphQlExpressionGrammar.parseToEnd(expr)
    result.assignments["variable"] shouldBe GraphQlOperationRef(
      serviceName = "service",
      operationName = "test",
      variables = emptyMap()
    )
  }

  test("multiple assignments are possible") {
    val result = GraphQlExpressionGrammar.parseToEnd("k1=~{service1::op1},k2=~{service2::op2},k3=~{service3::op3}")
    result.assignments["k1"] shouldBe GraphQlOperationRef(
      serviceName = "service1",
      operationName = "op1",
      variables = emptyMap()
    )
    result.assignments["k2"] shouldBe GraphQlOperationRef(
      serviceName = "service2",
      operationName = "op2",
      variables = emptyMap()
    )
    result.assignments["k3"] shouldBe GraphQlOperationRef(
      serviceName = "service3",
      operationName = "op3",
      variables = emptyMap()
    )
  }

  test("spaces are allowed in expressions") {
//    val result = GraphQlExpressionGrammar.parseToEnd("k1  = ~{ service1  :: op1 }    ,   k2 =    ~{  service2 ::    op2}  ")
    val result = GraphQlExpressionGrammar.parseToEnd(" k1  = ~{ service1  :: op1 }    ,   k2 =    ~{  service2 ::    op2}  ")
    result.assignments["k1"] shouldBe GraphQlOperationRef(
      serviceName = "service1",
      operationName = "op1",
      variables = emptyMap()
    )
    result.assignments["k2"] shouldBe GraphQlOperationRef(
      serviceName = "service2",
      operationName = "op2",
      variables = emptyMap()
    )
  }

  test("operations can have an empty variables block") {
    val result = GraphQlExpressionGrammar.parseToEnd("var = ~{ graphql :: TestQuery() }")
    result.assignments["var"] shouldBe GraphQlOperationRef(
      serviceName = "graphql",
      operationName = "TestQuery",
      variables = emptyMap()
    )
  }

  test("operations can have variables") {
    val result = GraphQlExpressionGrammar.parseToEnd("var = ~{ graphql :: TestQuery(name=null) }")
    result.assignments["var"] shouldBe GraphQlOperationRef(
      serviceName = "graphql",
      operationName = "TestQuery",
      variables = mapOf("name" to "null")
    )
  }

  test("variables are thymeleaf's standard expressions") {
    val result = GraphQlExpressionGrammar.parseToEnd("var = ~{ graphql :: TestQuery(ref = null, greeting = 'Hello World!', age = \${person.age}, name = #{person.name}, url = @{/person/url}) }")
    result.assignments["var"] shouldBe GraphQlOperationRef(
      serviceName = "graphql",
      operationName = "TestQuery",
      variables = mapOf(
        "ref" to "null",
        "greeting" to "'Hello World!'",
        "age" to "\${person.age}",
        "name" to "#{person.name}",
        "url" to "@{/person/url}"
      )
    )
  }
})
