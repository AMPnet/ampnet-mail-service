package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.exception.ResourceNotFoundException
import com.ampnet.mailservice.service.FileService
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.io.IOException

@Service
class FileServiceImpl(private val restTemplate: RestTemplate) : FileService {

    private val termsOfService: MutableMap<String, ByteArray> = mutableMapOf()

    @Throws(ResourceNotFoundException::class)
    override fun getTermsOfService(url: String): ByteArray {
        termsOfService[url]?.let { return it }
        try {
            val responseEntity =
                restTemplate.exchange<Resource>(url, HttpMethod.GET, HttpEntity.EMPTY)
            val byteArray = readInputStream(responseEntity, url)
            termsOfService[url] = byteArray
            return byteArray
        } catch (ex: RestClientException) {
            throw ResourceNotFoundException("Error while reading response from $url", ex)
        }
    }

    private fun readInputStream(responseEntity: ResponseEntity<Resource>, url: String): ByteArray {
        try {
            return responseEntity.body?.inputStream?.readAllBytes()
                ?: throw ResourceNotFoundException("Empty response from $url")
        } catch (ex: IOException) {
            throw ResourceNotFoundException("Resource could not be opened", ex)
        }
    }
}
