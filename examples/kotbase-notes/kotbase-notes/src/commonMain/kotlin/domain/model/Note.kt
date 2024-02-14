package domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val modified: Instant = Clock.System.now(),
    val edited: String = modified.toLocalizedString()
)

val sampleNotes = listOf(
    Note(
        id = "1",
        title = "Grocery List",
        text = "Eggs\nMilk\nBread\nApples",
        edited = "2:30pm"
    ),
    Note(
        id = "2",
        title = "Meeting Agenda",
        text = "Review quarterly goals\nAssign action items\nDiscuss budget allocations",
        edited = "Feb 5"
    ),
    Note(
        id = "3",
        title = "Birthday Party Ideas",
        text = "Venue: Park or backyard\nTheme: Superheroes\nActivities: Face painting, balloon animals",
        edited = "Jan 20"
    ),
    Note(
        id = "4",
        title = "Book Recommendations",
        text = "\"The Alchemist\" by Paulo Coelho\n\"The Lord of the Rings\" by J.R.R. Tolkien\n\"The Great Gatsby\" by F. Scott Fitzgerald",
        edited = "Jan 15"
    ),
    Note(
        id = "5",
        title = "Vacation Plans",
        text = "Destination: Hawaii\nActivities: Snorkeling, hiking, luau\nDates: July 10-17",
        edited = "Dec 10, 2022"
    ),
)

expect fun Instant.toLocalizedString(): String
