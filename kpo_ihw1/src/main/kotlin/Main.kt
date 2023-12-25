fun main() {
    // Запускаем интерфейс аутентификатора
    val authenticator = Authenticator()
    val authUI = AuthUI(authenticator)
    val cinemaManager = CinemaManager()
    // Повторно запрашиваем вход, пока пользователь не залогинится
    var isAuthenticated = false
    while (!isAuthenticated) {
        isAuthenticated = authUI.start()
    }
    // Запускаем интерфейс кинотеатра
    val cinemaUI = CinemaUI(cinemaManager)
    cinemaUI.start()
}