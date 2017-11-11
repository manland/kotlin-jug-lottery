package com.tgirard12.kotlinjuglottery


data class Pagination(val object_count: Int,
                      val page_number: Int,
                      val page_size: Int,
                      val page_count: Int)

data class Attendeed(val name: String?,
                     val first_name: String?,
                     val last_name: String?)

data class Attendees(val pagination: Pagination, val attendees: List<Attendeed>)

data class Event(val id: String)
data class EventDescription(val id: String, val nbAttendee: Int)

data class EventPage(val pagination: Pagination, val events: List<Event>)