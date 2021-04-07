package com.ampnet.mailservice.enums

enum class Lang {
    EN,
    ES,
    EL,
    DE,
    IT,
    FR;

    companion object {
        private val map = values().associateBy { it.name }
        fun langOrDefault(language: String) = map[language.toUpperCase()] ?: EN
    }
}
