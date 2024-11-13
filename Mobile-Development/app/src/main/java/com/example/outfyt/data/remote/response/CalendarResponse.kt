package com.example.outfyt.data.remote.response

import com.google.gson.annotations.SerializedName

data class CalendarResponse(

	@field:SerializedName("success")
	val success: Boolean,

	@field:SerializedName("events")
	val events: List<EventsItem>
)

data class Reminders(

	@field:SerializedName("useDefault")
	val useDefault: Boolean
)

data class End(

	@field:SerializedName("dateTime")
	val dateTime: String,

	@field:SerializedName("timeZone")
	val timeZone: String
)

data class EventsItem(

	@field:SerializedName("summary")
	val summary: String,

	@field:SerializedName("reminders")
	val reminders: Reminders,

	@field:SerializedName("creator")
	val creator: Creator,

	@field:SerializedName("kind")
	val kind: String,

	@field:SerializedName("htmlLink")
	val htmlLink: String,

	@field:SerializedName("created")
	val created: String,

	@field:SerializedName("iCalUID")
	val iCalUID: String,

	@field:SerializedName("start")
	val start: Start,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("eventType")
	val eventType: String,

	@field:SerializedName("sequence")
	val sequence: Int,

	@field:SerializedName("organizer")
	val organizer: Organizer,

	@field:SerializedName("etag")
	val etag: String,

	@field:SerializedName("location")
	val location: String,

	@field:SerializedName("end")
	val end: End,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("updated")
	val updated: String,

	@field:SerializedName("status")
	val status: String
)

data class Organizer(

	@field:SerializedName("self")
	val self: Boolean,

	@field:SerializedName("email")
	val email: String
)

data class Creator(

	@field:SerializedName("self")
	val self: Boolean,

	@field:SerializedName("email")
	val email: String
)

data class Start(

	@field:SerializedName("dateTime")
	val dateTime: String,

	@field:SerializedName("timeZone")
	val timeZone: String
)
