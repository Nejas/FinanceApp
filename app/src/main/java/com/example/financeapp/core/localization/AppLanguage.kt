package com.example.financeapp.core.localization

enum class AppLanguage(val languageTag: String) {
    RUSSIAN("ru"),
    ENGLISH("en"),
    GERMAN("de"),
    FRENCH("fr"),
    SPANISH("es");

    companion object {
        fun fromLanguageTag(languageTag: String): AppLanguage? {
            return entries.firstOrNull { language ->
                language.languageTag.equals(languageTag, ignoreCase = true)
            }
        }

        val AppLanguage.flag: String
            get() = let {
                when (this)
                {
                    RUSSIAN -> "🇷🇺"
                    ENGLISH -> "🇺🇸"
                    GERMAN -> "🇩🇪"
                    FRENCH -> "🇫🇷"
                    SPANISH -> "🇪🇸"
                }
            }

        val AppLanguage.displayName: String
            get() = let{
                when (this)
                {
                    RUSSIAN -> "Русский"
                    ENGLISH -> "English"
                    GERMAN -> "Deutsch"
                    FRENCH -> "Français"
                    SPANISH -> "Español"
                }
            }
    }
}