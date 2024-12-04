package de.jakobschaefer.htma.graphql

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

// <VariableName> = ~{ <ServiceName> :: <OperationName>(<Parameters>)}
data class GraphQlExpression(
  val assignments: Map<String, GraphQlOperationRef>
)

val GraphQlExpressionGrammar = object : Grammar<GraphQlExpression>() {
  // IMPORTANT: https://github.com/h0tk3y/better-parse?tab=readme-ov-file#tokens
  // The tokens order matters in some cases,
  // because the tokenizer tries to match them in exactly this order. For instance, if literalToken("a") is
  // listed before literalToken("aa"), the latter will never be matched. Be careful with keyword tokens!
  // If you match them with regexes, a word boundary \b in the end may help against ambiguity.
  val thStringLit by regexToken("'.*?'")
  val thVarExprLit by regexToken("\\\$\\{.*?\\}")
  val thUrlExprLit by regexToken("@\\{.*?\\}")
  val thMsgExprLit by regexToken("#\\{.*?\\}")
  val thNullLit by literalToken("null")
  @Suppress("unused")
  val space by regexToken("\\s+", ignore = true)
  val eq by literalToken("=")
  val comma by literalToken(",")
  val openingBracket by literalToken("(")
  val closingBracket by literalToken(")")
  val tilde by literalToken("~")
  val openingCurlyBracket by literalToken("{")
  val closingCurlyBracket by literalToken("}")
  val serviceSeparator by literalToken("::")
  val id by regexToken("[a-zA-Z_]\\w*")

  val identifier by parser(this::id) map { it.text }

  val thString by thStringLit map { it.text }
  val thNull = thNullLit asJust "null"
  val thVarExpr = thVarExprLit map { it.text }
  val thUrlExpr = thUrlExprLit map { it.text }
  val thMsgExpr = thMsgExprLit map { it.text }
  val thExpr by thNull or
      thString or
      thVarExpr or
      thUrlExpr or
      thMsgExpr

  val variableAssignment by identifier * -eq * thExpr map { it.t1 to it.t2 }
  val variableAssignments by separated(variableAssignment, comma, true) map { it.terms.toMap() }
  val variablesBlock by -openingBracket * variableAssignments * -closingBracket

  val gqlExpr by -tilde * -openingCurlyBracket * identifier * -serviceSeparator * identifier * optional(variablesBlock) * -closingCurlyBracket map {
        GraphQlOperationRef(
          serviceName = it.t1, operationName = it.t2, variables = it.t3 ?: emptyMap()
        )
      }

  val gqlAssignment by identifier * -eq * gqlExpr map { it.t1 to it.t2 }
  val gqlAssignments by separated(gqlAssignment, comma, true) map { it.terms.toMap() }
  val impl by gqlAssignments map { GraphQlExpression(
    assignments = it
  )}

  override val rootParser: Parser<GraphQlExpression> by impl
}
