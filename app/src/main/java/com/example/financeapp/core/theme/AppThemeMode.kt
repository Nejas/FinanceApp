package com.example.financeapp.core.theme

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        val SelectionOrder: List<AppThemeMode> = listOf(
            LIGHT,
            DARK,
            SYSTEM
        )

        fun fromName(name: String): AppThemeMode? {
            return entries.firstOrNull { mode ->
                mode.name.equals(name, ignoreCase = true)
            }
        }
    }
}
