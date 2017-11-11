package com.tgirard12.kotlinjuglottery

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

val attendees = Attendees(
        pagination = Pagination(object_count = 5, page_number = 1, page_size = 5, page_count = 1),
        attendees = listOf(
                Attendeed("olivier", "n", "o"),
                Attendeed("arnaud", "c", "a"),
                Attendeed("romain", "m", "r"),
                Attendeed("thomas", "g", "t"),
                Attendeed("quentin", "r", "q"),
                Attendeed("François", "t", "f")
        )
)


fun Application.main() {
    install(DefaultHeaders)
    install(Compression)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Routing) {
        get("/winners") {
            val nb = context.request.queryParameters["nb"]
            call.respondText("Hello, world! $nb", ContentType.Text.Html)
        }
    }
}

//
