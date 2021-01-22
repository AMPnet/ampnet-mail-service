package com.ampnet.mailservice.service

import java.io.InputStream

interface FileService {
    fun getInputStream(url: String): InputStream
}
