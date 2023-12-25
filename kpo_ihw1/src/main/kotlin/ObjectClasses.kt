import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: Int,
    val title: String,
    val description: String
)

@Serializable
data class Showtime(
    val id: Int,
    val movieId: Int,
    val startTime: String,
    @Contextual
    val cinemaHall: CinemaHall
)

@Serializable
data class Ticket(
    val id: Int,
    val showtimeId: Int,
    val seatNumber: Int,
    var isSold: Boolean = false,
    var isRefunded: Boolean = false
)