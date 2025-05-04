package de.jakobschaefer.htma.examples.basic.graphql

import de.jakobschaefer.htma.graphql.GraphQlDataResolver
import de.jakobschaefer.htma.graphql.GraphQlDataResolverEnvironment
import de.jakobschaefer.htma.graphql.inputvalidation.InputTypeHelper
import de.jakobschaefer.htma.graphql.inputvalidation.requireEmail
import io.konform.validation.Validation

data class User(
  val id: String,
  val email: String,
  val password: String,
  val name: String
)

val users = mutableListOf(
  User(
    id = "1",
    email = "peter@pan.de",
    name = "Peter Pan",
    password = "password"
  ),
)

class SignInWithEmailAndPasswordResolver : GraphQlDataResolver<User?> {
  override suspend fun resolve(env: GraphQlDataResolverEnvironment): User? {
    val input = SignInWithEmailAndPasswordInput.fromArguments(env.arguments)
    val user = users.find { it.email == input.email && it.password == input.password }
    return user
  }
}

class SignInWithEmailAndPasswordInput(
  val email: String?,
  val password: String?
) {
  companion object : InputTypeHelper<SignInWithEmailAndPasswordInput>() {
    override val validation = Validation<SignInWithEmailAndPasswordInput> {
      SignInWithEmailAndPasswordInput::email required {
        requireEmail()
      }
    }

    override fun fromMap(data: Map<String, Any>): SignInWithEmailAndPasswordInput {
      return SignInWithEmailAndPasswordInput(
        email = data["email"] as String,
        password = data["password"] as String
      )
    }
  }
}
