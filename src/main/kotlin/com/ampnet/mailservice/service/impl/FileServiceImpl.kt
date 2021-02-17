package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.exception.ResourceNotFoundException
import com.ampnet.mailservice.service.FileService
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

@Service
class FileServiceImpl : FileService {

    private val termsOfService: MutableMap<String, ByteArray> = mutableMapOf()

    @Throws(ResourceNotFoundException::class)
    override fun getTermsOfService(url: String): ByteArray {
        termsOfService[url]?.let { return it }
        val byteArray = getFileContent(url)
        termsOfService[url] = byteArray
        return byteArray
    }

    private fun getFileContent(url: String): ByteArray {
        try {
            val connection = getURLFromString(url).openConnection() as HttpURLConnection
            val inputStream = connection.inputStream
            val outputStream = ByteArrayOutputStream()
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            return outputStream.toByteArray()
        } catch (ex: IOException) {
            throw ResourceNotFoundException("Error while reading resource from $url", ex)
        }
    }

    private fun getURLFromString(attachmentUrl: String): URL {
        try {
            return URL(attachmentUrl)
        } catch (ex: MalformedURLException) {
            throw ResourceNotFoundException("$attachmentUrl is not a valid url", ex)
        }
    }
}
