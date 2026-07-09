package com.example.financeapp.domain.model

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
    }
}
