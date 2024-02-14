package presentation

sealed interface Screen {
    data object Splash : Screen
    data object Login : Screen
    data object Main : Screen
    data class Edit(val noteId: String) : Screen
}
