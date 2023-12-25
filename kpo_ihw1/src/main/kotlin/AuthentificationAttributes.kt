import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest

@Serializable
data class UserData(val username: String, val password: String)

class Authenticator {
    private val users: MutableMap<String, String> = HashMap()

    fun loadUser(username: String, password: String) {
        users[username] = password
    }

    fun registerUser(username: String, password: String) {
        val hashedPassword = hashPassword(password)
        users[username] = hashedPassword
    }

    fun authenticateUser(username: String, password: String): Boolean {
        val hashedPassword = hashPassword(password)
        return users[username] == hashedPassword
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getAllUsers(): MutableMap<String, String> = users
}

class AuthUI(private val authenticator: Authenticator) {

    private val usersFile = File("users.json")

    init {
        loadUsers()
    }

    private fun loadUsers() {
        if (usersFile.exists()) {
            val json = Json
            val usersJson = usersFile.readText()
            val usersDataList = json.decodeFromString<List<UserData>>(usersJson)

            for (userData in usersDataList) {
                authenticator.loadUser(userData.username, userData.password)
            }
        }
    }

    private fun saveUsers() {
        val json = Json
        val usersDataList = authenticator.getAllUsers().map { UserData(it.key, it.value) }
        val usersJson = json.encodeToString(usersDataList)
        usersFile.writeText(usersJson)
    }

    fun start(): Boolean {
        println("Добро пожаловать!")

        var authenticated = false

        while (!authenticated) {
            displayMainMenu()
            val userInput = readlnOrNull()?.toIntOrNull() ?: 0

            when (userInput) {
                1 -> registerUser()
                2 -> authenticated = loginUser()
                else -> println("Некорректный ввод. Попробуйте еще раз.")
            }
        }

        saveUsers()
        return true // Пользователь успешно авторизован
    }

    private fun displayMainMenu() {
        println("\nВыберите действие:")
        println("1. Зарегистрироваться")
        println("2. Войти")
    }

    private fun registerUser() {
        println("Введите имя пользователя:")
        val username = readlnOrNull() ?: ""

        println("Введите пароль:")
        val password = readlnOrNull() ?: ""

        authenticator.registerUser(username, password)
        println("Пользователь $username успешно зарегистрирован.")
    }

    private fun loginUser(): Boolean {
        println("Введите имя пользователя:")
        val username = readlnOrNull() ?: ""

        println("Введите пароль:")
        val password = readlnOrNull() ?: ""

        if (authenticator.authenticateUser(username, password)) {
            println("Пользователь $username успешно вошел в систему.")
            return true
        } else {
            println("Ошибка входа. Неверное имя пользователя или пароль.")
        }
        return false
    }
}
