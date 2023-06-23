package com.example

import org.http4k.client.OkHttp
import org.http4k.core.Method.GET
import org.http4k.core.Method
import org.http4k.core.*
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.lens.WebForm
import org.junit.jupiter.api.Test

class AppTest {
    @Test
    fun loadHomePage(){
        val response = app(
            Request(Method.GET, "http://localhost:9000/home")
        )
        assert(response.bodyString().contains("Welcome to Chitter"))
    }

   @Test
   fun `can post a peep`(){
       val response = app(
           Request(Method.POST, "http://localhost:9000/home")
       )
       assert(response.bodyString().contains("<p>"))
   }

    @Test
    fun `sign up form loads`(){
        val form = WebForm(mapOf(
            "fName" to listOf("fName"),
            "lName" to listOf("lName"),
            "email" to listOf("email"),
            "password" to listOf("password")
        ))

        val response = app(
            Request(Method.GET, "http://localhost:9000/signup").with(
                requiredSignUpForm of form
            )
        )
        assert(response.bodyString().contains("Welcome To Chitter"))
    }

    @Test
    fun `user is signed in`(){
        val response = app(
            Request(Method.POST,"http://localhost:9000/signed-in" )
        )
        assert(response.status == (Status.OK))
    }
}
