package br.com.lpborges.webreactive

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Created by leandropg on 10/20/16.
 *
 * Test Application for Kotlin with Spring Web Reactive
 */
@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

data class Req(val value: Int)

data class Res(val message: String)

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class ServiceException(message: String?) : Exception(message)

/**
 * Reactive Controller
 */
@RestController("/test")
class TestController(val testService: TestService) {

    private val logger = LoggerFactory.getLogger(TestController::class.java)!!

    @PostMapping
    fun test(@RequestBody request: Mono<Req>): Mono<Res> {

        return request.doOnNext { req -> logger.info("Request: {}", req) } // log the request
                .map { res -> res.value } // adapter to transform the request to service parameter
                .flatMap { value -> testService.processValue(value) } // async call to service
                .map { message -> Res(message) } // adapter to transform the service return to response
                .onErrorMap(ServiceException::class.java) { e ->
                    // handle Service Error
                    ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
                }
                .doOnNext { res -> logger.info("Response: {}", res) } // log the response
    }
}

/**
 * Reactive Service
 */
@Service
class TestService(val repository: BlockedTestRepository) {

    fun processValue(value: Int): Mono<String> {

        validateValue(value)

        /**
         * Wrap a sync call.
         * Note that subscribeOn does not subscribe to the Mono.
         * It specifies what kind of Scheduler to use when a subscribe call happens.
         *
         * @see <a href="http://projectreactor.io/docs/core/release/reference/#faq.wrap-blocking"/>
         */
        return Mono.fromCallable { repository.getDataMessage(value) }.subscribeOn(Schedulers.elastic())
    }

    /**
     * Simulate a error when value == 400
     */
    private fun validateValue(value: Int) {
        if (value == 400) {
            throw ServiceException("Invalid value")
        }
    }
}

/**
 * Blocking Sync Repository
 */
@Repository
class BlockedTestRepository {
    fun getDataMessage(value: Int): String = "Your value is $value"
}
