package com.ampnet.mailservice.service

interface FileService {
    fun getTermsOfService(url: String): ByteArray
}
