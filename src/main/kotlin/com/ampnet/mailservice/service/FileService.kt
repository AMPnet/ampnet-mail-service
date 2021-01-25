package com.ampnet.mailservice.service

import java.io.InputStream

interface FileService {
    fun getTermsOfService(url: String): InputStream
}
