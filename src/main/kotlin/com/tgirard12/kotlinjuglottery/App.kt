package com.tgirard12.kotlinjuglottery

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import java.util.*
import com.google.gson.GsonBuilder
import io.ktor.features.*
import okhttp3.OkHttpClient
import okhttp3.Request
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.io.InputStreamReader


val token = "5O5ICDI5I4LUFADWOMEL" // TODO System.getenv("EVENTBRITE_TOKEN")
val orgaID = "1464915124" // TODO System.getenv("ORGANIZER_TOKEN")

val attendeed = listOf(
        Attendeed("olivier", "n", "o"),
        Attendeed("arnaud", "c", "a"),
        Attendeed("romain", "m", "r"),
        Attendeed("thomas", "g", "t"),
        Attendeed("quentin", "r", "q"),
        Attendeed("François", "t", "f")
)

val attendees = Attendees(
        pagination = Pagination(object_count = 5, page_number = 1, page_size = 5, page_count = 1),
        attendees = attendeed
)

val gson = GsonBuilder().create()
val client = OkHttpClient()
val rand = Random(System.currentTimeMillis())
var cache = Array(5) { attendeed[it] }
var listAttendeed = mutableListOf<Attendeed>()

fun getLastEvent(): Event {
    val request = Request.Builder()
            .url("https://www.eventbriteapi.com/v3/events/search/?token=$token&organizer.id=$orgaID")
            .build()
    val response = client.newCall(request).execute()
    println("EVENT : ${response.body()?.string()}")
    val events = gson.fromJson(InputStreamReader(response.body()?.byteStream()), Events::class.java)
    return events.events[0]
}

fun getAllAttendeed() = Flux.create<Attendeed> { flux ->
    val event = getLastEvent()

    var paginationCount = 1
    var paginationNumber = 1
    var total = 9

    while (paginationCount * paginationNumber < total) {
        val (pagination, attendees1) = getAttendeed(event.id, 0)
        paginationCount = pagination.page_count
        paginationNumber = pagination.page_number
        total = pagination.object_count

        attendees1.forEach { flux.next(it) }
    }
    flux.complete()
}

fun getAttendeed(eventId: String, page: Int): Attendees {
    val request = Request.Builder()
            .url("https://www.eventbriteapi.com/v3/events/$eventId/attendees/?page=$page&token=$token")
            .build()
    val response = client.newCall(request).execute()
    return gson.fromJson(InputStreamReader(response.body()?.byteStream()), Attendees::class.java)
}

fun updateWinnersCache() {
    getAllAttendeed()
            .subscribeOn(Schedulers.newSingle("http"))
            .doOnSubscribe { listAttendeed.clear() }
            .log("Update")
            .subscribe({
                listAttendeed.add(it)
            }, { }, {
                (0..5).forEach {
                    cache[it] = listAttendeed[rand.nextInt(listAttendeed.size)]
                }
            })
}

fun Application.main() {

//    Flux.interval(Duration.ofMinutes(1))
//            .log("cacheUpdate")
//            .subscribe { updateWinnersCache() }

    updateWinnersCache()

    install(DefaultHeaders)
    install(Compression)
    install(CallLogging)
    install(StatusPages)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Routing) {
        get("/") {
            call.respondText("GET /winners?nb=4", ContentType.Text.Html)
        }
        get("/winners") {
            context.request.queryParameters["nb"]?.toIntOrNull()?.let { nb ->
                call.respond(Array(nb) { cache[it] })

            } ?: call.respond(HttpStatusCode.BadRequest, "nb:Int parameter not found")
        }
    }
}

//
