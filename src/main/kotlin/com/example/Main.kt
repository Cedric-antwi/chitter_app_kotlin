package com.example

import okhttp3.ResponseBody.Companion.toResponseBody
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Body
import org.http4k.core.Status.Companion.OK
import org.http4k.lens.Query
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.template.ViewModel
import org.http4k.template.HandlebarsTemplates
import org.http4k.lens.FormField
import org.http4k.lens.*
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.util.*
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.Cookie
import org.http4k.routing.path

val sessionRegistry = mutableMapOf<String, Int>()

//1. Data class container
data class Peep(val user: User?, val peepBody: String?, val peepDate: String, val madeBy: User?)
data class User(val fName: String, val lName: String, val email: String, val password: String, val id: Int?)

data class HomeViewModel(
    val peeps: MutableList<Peep>,
    val signedIn: Boolean,
    val currentUser: User?,
    val users: MutableList<User>
): ViewModel
val peeps = mutableListOf<Peep>()

data class UserFormViewModel(
    val user: User
): ViewModel
val users = mutableListOf<User>()



//2. renderer
//The lens for FormField.required should have a 'name' in the parenthesis which is the same as whatever name was given to the form field
val optionalPeep = FormField.optional("peep")
val requiredFnameLens = FormField.required("fName")
val requiredLnameLens = FormField.required("lName")
val requiredEmailLens = FormField.required("email")
val requiredPasswordLens = FormField.required("password")
//3.
// We created the lens for individual fields above, now we do a lens for the actual Form
//Don't forget .toLens()
val requiredForm = Body.webForm(
    Validator.Strict,
    optionalPeep
).toLens()

val requiredSignUpForm = Body.webForm(
    Validator.Strict,
    requiredFnameLens,
    requiredLnameLens,
    requiredEmailLens,
    requiredPasswordLens
).toLens()

val app: HttpHandler = routes(
    "/" bind GET to { request: Request ->
        Response(OK)
    },
    "/signup" bind GET to {request: Request ->

        val renderer = HandlebarsTemplates().HotReload("src/main/resources")
        val user = User("fname", "lname", "email", "password", generateRandomId(totalCount))
        val viewModel = UserFormViewModel(user)
        Response(OK).body(renderer(viewModel))
    },
    "/signed-in/{id}" bind POST to {request: Request ->
        val id = request.path("id")?.toInt()

        val sessionId = UUID.randomUUID().toString()
        // SORT OUT HERE , manage session id's etc, this seciton may actuall have to be moved to the id param version

//        sessionRegistry.put(sessionId, users[0].id)

        val form = requiredSignUpForm(request) // requiredSignUpForm?
        val fname = requiredFnameLens(form)
        val lname = requiredLnameLens(form)
        val email = requiredEmailLens(form)
        val password = requiredPasswordLens(form)
        val newUser = User(fname, lname, email, password, id)
        val renderer = HandlebarsTemplates().HotReload("src/main/resources")
        if (newUser !in users) users.add(newUser) else println("user exists")


        val viewModel = HomeViewModel(peeps, true, currentUser = newUser, users)
        Response(OK).body(renderer(viewModel))
    },
    "/signed-in/post/{id}" bind POST to {request: Request ->
        val id = request.path("id")?.toInt()

        val form = requiredForm(request)
        val peep = optionalPeep(form)
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val time = LocalDateTime.now()
        val formattedTime = time.format(formatter)
        val thisUser = users.filter { it.id == id }.first()
        val peepMsg = Peep(user = thisUser ,peepBody = peep, peepDate = formattedTime, thisUser)
//        peeps.clear()
        peeps.add(0, peepMsg)
        val viewModel = HomeViewModel(peeps, currentUser = thisUser, signedIn = true, users = users)
        val renderer = HandlebarsTemplates().HotReload("src/main/resources")

        Response(OK).body(renderer(viewModel))
    },

    "/home" bind GET to {request: Request ->
        val renderer = HandlebarsTemplates().HotReload("src/main/resources")
        val viewModel = HomeViewModel(peeps, currentUser = null, signedIn = false, users = users)
        println(peeps)
        Response(OK).body(renderer(viewModel))

    },
    "/home" bind POST to {request: Request ->
        val form = requiredForm(request)
        val peep = optionalPeep(form)
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val time = LocalDateTime.now()
        val formattedTime = time.format(formatter)
        val peepMsg = Peep(user = null ,peepBody = peep, peepDate = formattedTime, madeBy = null)
//        peeps.clear()
        peeps.add(0, peepMsg)
        val viewModel = HomeViewModel(peeps, currentUser = null, signedIn = false, users = users)
        val renderer = HandlebarsTemplates().HotReload("src/main/resources")

        Response(OK).body(renderer(viewModel))
    }

)
// Be able to log out
// Be able to log in - should only be able to log in if account exists
// Should not be able to create an account with the same email - thing about the error messages for this
// Work on the sessions/cookies functionality


fun main() {
    val server = app.asServer(Undertow(9002)).start()

    println("Server started on " + server.port())
}
