package org.jetbrains.ktor.tests.http

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.features.*
import org.jetbrains.ktor.features.http.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.testing.*
import org.jetbrains.ktor.tests.*
import org.junit.*
import java.time.*
import kotlin.test.*

class HSTSTest {
    @Test
    fun testHttp() {
        withTestApplication {
            application.testApp()

            handleRequest(HttpMethod.Get, "/").let { call ->
                assertNull(call.response.headers[HttpHeaders.StrictTransportSecurity])
            }
        }
    }

    @Test
    fun testHttps() {
        withTestApplication {
            application.testApp()

            handleRequest(HttpMethod.Get, "/", {
                addHeader("X-Forwarded-Proto", "https")
                addHeader("X-Forwarded-Host", "some")
            }).let { call ->
                assertEquals("max-age=10; includeSubDomains; preload; some=\"va=lue\"", call.response.headers[HttpHeaders.StrictTransportSecurity])
            }

            handleRequest(HttpMethod.Get, "/", {
                addHeader("X-Forwarded-Proto", "https")
            }).let { call ->
                assertEquals("max-age=10; includeSubDomains; preload; some=\"va=lue\"", call.response.headers[HttpHeaders.StrictTransportSecurity])
            }
        }
    }

    @Test
    fun testCustomPort() {
        withTestApplication {
            application.testApp()

            handleRequest(HttpMethod.Get, "/", {
                addHeader("X-Forwarded-Proto", "https")
                addHeader("X-Forwarded-Host", "some:8443")
            }).let { call ->
                assertNull(call.response.headers[HttpHeaders.StrictTransportSecurity])
            }
        }
    }

    private fun Application.testApp() {
        install(HSTS) {
            maxAge = Duration.ofSeconds(10L)
            includeSubDomains = true
            preload = true
            customDirectives["some"] = "va=lue"
        }

        routing {
            get("/") {
                call.respond("ok")
            }
        }
    }
}
