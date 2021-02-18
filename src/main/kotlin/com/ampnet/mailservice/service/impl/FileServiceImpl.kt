package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.exception.ResourceNotFoundException
import com.ampnet.mailservice.service.FileService
import mu.KLogging
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

@Service
class FileServiceImpl : FileService {

    companion object : KLogging()

    private val termsOfService: MutableMap<String, ByteArray> = mutableMapOf()

    @Throws(ResourceNotFoundException::class)
    override fun getTermsOfService(url: String): ByteArray {
        termsOfService[url]?.let {
            logger.debug { "Terms of service is already downloaded from url: $url" }
            return it
        }
        val byteArray = getFileContent(url)
        termsOfService[url] = byteArray
        return byteArray
    }

    private fun getFileContent(url: String): ByteArray {
        try {
            logger.debug { "Downloading terms of service from url: $url" }
            val connection = getURLFromString(url).openConnection() as HttpURLConnection
            connection.inputStream.use { input ->
                ByteArrayOutputStream().use { output ->
                    input.copyTo(output)
                    return output.toByteArray()
                }
            }
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
