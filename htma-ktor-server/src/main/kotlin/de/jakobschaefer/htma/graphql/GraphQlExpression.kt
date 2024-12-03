package de.jakobschaefer.htma.graphql

import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

data class GraphQlExpression(
  val assignments: Map<String, GraphQlOperationRef>
)

// <VariableName> = ~{ <ServiceName> :: <OperationName>(<Parameters>)}
val GraphQlExpressionGrammar = object : Grammar<GraphQlExpression>() {
  val identifier by regexToken("[A-Za-z_][A-Za-z0-9_]*")
  val paramStart by literalToken("(")
  val paramEnd by literalToken(")")
  val operationStart by literalToken("~{")
  val operationEnd by literalToken("}")
  val serviceSeparator by literalToken("::")
  override val rootParser: Parser<GraphQlExpression>
    get() = TODO("Not yet implemented")

}
