package com.ampnet.mailservice.service.pojo

data class Attachment(
    val name: String,
    val file: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Attachment

        if (name != other.name) return false
        if (!file.contentEquals(other.file)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + file.contentHashCode()
        return result
    }
}

const val TERMS_OF_SERVICE = "Terms_of_service.pdf"
