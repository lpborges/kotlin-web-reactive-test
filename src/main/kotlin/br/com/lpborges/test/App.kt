package br.com.lpborges.test

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * Created by leandropg on 10/20/16.
 *
 * Test Application for Kotlin with Spring Web Reactive
 */
@SpringBootApplication
open class App

fun main(args: Array<String>) {
    SpringApplication.run(App::class.java, *args)
}

data class Test(var message: String)

@RestController("/test")
class TestController {

    @PostMapping(consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun test(@RequestBody test: Mono<Test>): Mono<String>
            = test.map(Test::message).doOnNext(::println)

    @ExceptionHandler(org.springframework.web.server.ServerWebInputException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun exceptionHandler(e: Exception): String = e.message ?: "Invalid Input Error"
}
