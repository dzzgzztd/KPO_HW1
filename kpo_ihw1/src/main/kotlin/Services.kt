import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class CinemaHall(private val rows: Int, val seatsPerRow: Int) {
    val seats: Array<Array<Ticket?>> = Array(rows) { Array(seatsPerRow) { null } }

    // Метод для отметки места как занятого
    fun markSeatAsOccupied(row: Int, seatNumber: Int, ticket: Ticket) {
        seats[row][seatNumber] = ticket
    }

    // Метод для отображения информации о местах в зале
    fun displaySeats() {
        for (i in 0 until rows) {
            for (j in 0 until seatsPerRow) {
                val status = if (seats[i][j] == null) "Свободно" else "Занято"
                println("Ряд ${i + 1}, Место ${j + 1}: $status")
            }
        }
    }
}

class CinemaManager {
    private val movies: MutableList<Movie> = mutableListOf()
    private val showtimes: MutableList<Showtime> = mutableListOf()
    private val tickets: MutableList<Ticket> = mutableListOf()

    fun getMovies(): MutableList<Movie> = movies

    fun getShowtimes(): MutableList<Showtime> = showtimes

    fun saveData() {
        val json = Json { prettyPrint = true }
        val moviesJson = json.encodeToString(movies)
        val showtimesJson = json.encodeToString(showtimes)
        val ticketsJson = json.encodeToString(tickets)

        File("movies.json").writeText(moviesJson)
        File("showtimes.json").writeText(showtimesJson)
        File("tickets.json").writeText(ticketsJson)
    }

    fun loadData() {
        if (File("movies.json").exists()) {
            val json = Json
            val moviesJson = File("movies.json").readText()
            val showtimesJson = File("showtimes.json").readText()
            val ticketsJson = File("tickets.json").readText()

            movies.addAll(json.decodeFromString<List<Movie>>(moviesJson))
            showtimes.addAll(json.decodeFromString<List<Showtime>>(showtimesJson))
            tickets.addAll(json.decodeFromString<List<Ticket>>(ticketsJson))
        }
    }

    fun addShowtime(showtime: Showtime) {
        showtimes.add(showtime)
        println("Сеанс добавлен.")
    }

    fun sellTicket(showtimeId: Int, seatNumber: Int) {
        val showtime = showtimes.find { it.id == showtimeId }

        if (showtime != null) {
            val hall = showtime.cinemaHall

            val existingTicket = tickets.find { it.showtimeId == showtimeId && it.seatNumber == seatNumber }
            if (existingTicket != null) {
                if (!existingTicket.isSold) {
                    existingTicket.isSold = true
                    hall.markSeatAsOccupied(
                        seatNumber / hall.seatsPerRow,
                        seatNumber % hall.seatsPerRow,
                        existingTicket
                    )
                } else {
                    println("Место $seatNumber уже продано.")
                }
            } else {
                val newTicketId = tickets.maxOfOrNull { it.id }?.plus(1) ?: 1
                val newTicket = Ticket(newTicketId, showtimeId, seatNumber, true, false)
                tickets.add(newTicket)
                hall.markSeatAsOccupied(seatNumber / hall.seatsPerRow, seatNumber % hall.seatsPerRow, newTicket)
                println("Билет на место $seatNumber успешно продан для сеанса $showtimeId с ID билета $newTicketId")
            }
        } else {
            println("Сеанс с ID $showtimeId не найден или зал для него не инициализирован.")
        }
    }

    // Метод для добавления нового фильма
    fun addMovie(movie: Movie) {
        movies.add(movie)
    }

    // Метод для редактирования информации о фильме по его ID
    fun editMovie(movieId: Int, updatedMovie: Movie) {
        val index = movies.indexOfFirst { it.id == movieId }
        if (index != -1) {
            movies[index] = updatedMovie
            println("Фильм с ID $movieId обновлен.")
        } else {
            println("Фильм с ID $movieId не найден.")
        }
    }

    // Метод для удаления фильма по его ID
    fun deleteMovie(movieId: Int) {
        val movieToRemove = movies.find { it.id == movieId }
        if (movieToRemove != null) {
            movies.remove(movieToRemove)
        } else {
            println("Фильм с ID $movieId не найден.")
        }
    }

    // Метод для редактирования времени сеанса по его ID
    fun editShowtime(showtimeId: Int, updatedShowtime: Showtime) {
        val index = showtimes.indexOfFirst { it.movieId == showtimeId }
        if (index != -1) {
            showtimes[index] = updatedShowtime
            println("Время сеанса для фильма с ID $showtimeId обновлено.")
        } else {
            println("Сеанс с ID $showtimeId не найден.")
        }
    }

    // Метод для удаления сеанса по ID фильма
    fun deleteShowtime(movieId: Int) {
        showtimes.removeAll { it.movieId == movieId }
    }

    // Метод для возврата билета по его id
    fun refundTicket(ticketId: Int) {
        val ticketToRefund = tickets.find { it.id == ticketId }

        if (ticketToRefund != null && !ticketToRefund.isRefunded) {
            ticketToRefund.isRefunded = true
            ticketToRefund.isSold = false

            val showtime = showtimes.find { it.id == ticketToRefund.showtimeId }
            val hall = showtime?.cinemaHall

            if (showtime != null && hall != null) {
                val row = ticketToRefund.seatNumber / hall.seatsPerRow
                val col = ticketToRefund.seatNumber % hall.seatsPerRow
                hall.seats[row][col] = null
                println("Билет с ID $ticketId возвращен.")
            } else {
                println("Сеанс для билета с ID $ticketId не найден или зал для него не инициализирован.")
            }
        } else {
            println("Билет с ID $ticketId не найден или уже возвращен.")
        }
    }

    // Метод для отображения свободных и проданных мест для выбранного сеанса
    fun displaySeatsForShowtime(showtimeId: Int) {
        val showtime = showtimes.find { it.id == showtimeId }

        if (showtime != null) {
            showtime.cinemaHall.displaySeats()
        } else {
            println("Сеанс с ID $showtimeId не найден.")
        }
    }
}

class CinemaUI(private val cinemaManager: CinemaManager) {
    fun start() {
        cinemaManager.loadData()
        println("Добро пожаловать в кинотеатр!")

        var running = true
        while (running) {
            displayMainMenu()
            val userInput = readlnOrNull()?.toIntOrNull() ?: 0

            when (userInput) {
                1 -> showMovies()
                2 -> addMovie()
                3 -> showShowtimes()
                4 -> addShowtime()
                5 -> sellTicket()
                6 -> refundTicket()
                7 -> displaySeatsForShowtime()
                8 -> additionalActions()
                0 -> running = false
                else -> println("Некорректный ввод. Попробуйте еще раз.")
            }
        }

        cinemaManager.saveData()
    }

    private fun additionalActions() {
        var running = true
        while (running) {
            additionalActionsMenu()
            val userInput = readlnOrNull()?.toIntOrNull() ?: 0

            when (userInput) {
                1 -> editMovie()
                2 -> deleteMovie()
                3 -> editShowtime()
                4 -> deleteShowtime()
                5 -> running = false
                else -> println("Некорректный ввод. Попробуйте еще раз.")
            }
        }
    }

    private fun displayMainMenu() {
        println("\nВыберите действие:")
        println("1. Показать список фильмов")
        println("2. Добавить фильм")
        println("3. Показать расписание сеансов")
        println("4. Добавить сеанс")
        println("5. Продать билет")
        println("6. Вернуть билет")
        println("7. Отобразить места для сеанса")
        println("8. Редактировать данные фильмов / сеансов")
        println("0. Выйти из программы")
    }

    private fun additionalActionsMenu() {
        println("\nВыберите действие:")
        println("1. Изменить данные о фильме")
        println("2. Удалить фильм из списка")
        println("3. Изменить время сеанса")
        println("4. Отменить сеанс")
        println("5. Вернуться в главное меню")
    }

    private fun showMovies() {
        val movies = cinemaManager.getMovies()
        println("Список фильмов:")
        movies.forEach { println("${it.id}. ${it.title}") }
    }

    private fun addMovie() {
        println("Введите название фильма:")
        val title = readlnOrNull() ?: ""
        println("Введите описание фильма:")
        val description = readlnOrNull() ?: ""

        val newMovie = if (cinemaManager.getMovies().isEmpty()) {
            Movie(1, title, description) // Если список пуст, устанавливаем ID фильма в 1
        } else {
            val nextId = cinemaManager.getMovies().maxOf { it.id } + 1 // Получаем следующий ID
            Movie(nextId, title, description)
        }

        cinemaManager.addMovie(newMovie)
        println("Фильм \"$title\" добавлен.")
    }

    private fun showShowtimes() {
        val showtimes = cinemaManager.getShowtimes()
        println("Расписание сеансов:")
        showtimes.forEach { println("ID сеанса: ${it.id}, Фильм: \"${cinemaManager.getMovies()[it.movieId - 1].title}\", Время: ${it.startTime}") }
    }

    private fun addShowtime() {
        println("Введите ID фильма:")
        val movieId = readlnOrNull()?.toIntOrNull() ?: -1
        println("Введите время сеанса (например, \"12:00\"):")
        val startTime = readlnOrNull() ?: ""

        val newShowtimeId = cinemaManager.getShowtimes().maxOfOrNull { it.id }?.plus(1) ?: 1
        val newShowtime = Showtime(newShowtimeId, movieId, startTime, CinemaHall(10, 10))

        cinemaManager.addShowtime(newShowtime)
    }

    private fun sellTicket() {
        println("Введите ID сеанса:")
        val showtimeId = readlnOrNull()?.toIntOrNull() ?: -1

        println("Введите номер места:")
        val seatNumber = readlnOrNull()?.toIntOrNull() ?: -1

        if (showtimeId != -1 && seatNumber in 0..99) {
            cinemaManager.sellTicket(showtimeId, seatNumber)
        } else {
            println("Неверный сеанс или неверно указанное место")
        }
    }

    private fun refundTicket() {
        println("Введите ID билета:")
        val ticketId = readlnOrNull()?.toIntOrNull() ?: -1

        if (ticketId != -1) {
            cinemaManager.refundTicket(ticketId)
        }
    }

    private fun displaySeatsForShowtime() {
        println("Введите ID фильма:")
        val movieId = readlnOrNull()?.toIntOrNull() ?: -1
        cinemaManager.displaySeatsForShowtime(movieId)
    }

    private fun editMovie() {
        println("Введите ID фильма для изменения:")
        val movieId = readlnOrNull()?.toIntOrNull() ?: -1
        val movieToUpdate = cinemaManager.getMovies().find { it.id == movieId }

        if (movieToUpdate != null) {
            println("Введите новое название фильма:")
            val title = readlnOrNull() ?: ""
            println("Введите новое описание фильма:")
            val description = readlnOrNull() ?: ""

            val updatedMovie = Movie(movieToUpdate.id, title, description)
            cinemaManager.editMovie(movieId, updatedMovie)
        }
    }

    private fun deleteMovie() {
        println("Введите ID фильма для удаления:")
        val movieId = readlnOrNull()?.toIntOrNull() ?: -1
        cinemaManager.deleteMovie(movieId)
    }

    private fun editShowtime() {
        println("Введите ID сеанса для изменения времени:")
        val showtimeId = readlnOrNull()?.toIntOrNull() ?: -1
        val showtimeToUpdate = cinemaManager.getShowtimes().find { it.id == showtimeId }

        if (showtimeToUpdate != null) {
            println("Введите новое время сеанса (например, \"12:00\"):")
            val startTime = readlnOrNull() ?: ""

            val updatedShowtime = showtimeToUpdate.copy(startTime = startTime)
            cinemaManager.editShowtime(showtimeId, updatedShowtime)
        }
    }

    private fun deleteShowtime() {
        println("Введите ID фильма для отмены сеанса:")
        val movieId = readlnOrNull()?.toIntOrNull() ?: -1
        cinemaManager.deleteShowtime(movieId)
    }
}