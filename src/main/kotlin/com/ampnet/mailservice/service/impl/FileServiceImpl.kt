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
import java.io.InputStream

@Service
class FileServiceImpl(private val restTemplate: RestTemplate) : FileService {

    private val termsOfService: MutableMap<String, InputStream> = mutableMapOf()

    @Throws(ResourceNotFoundException::class)
    override fun getTermsOfService(url: String): InputStream {
        termsOfService[url]?.let { return it }
        try {
            val responseEntity =
                restTemplate.exchange<Resource>(url, HttpMethod.GET, HttpEntity.EMPTY)
            val inputStream = readInputStream(responseEntity, url)
            termsOfService[url] = inputStream
            return inputStream
        } catch (ex: RestClientException) {
            throw ResourceNotFoundException("Error while reading response from $url", ex)
        }
    }

    private fun readInputStream(responseEntity: ResponseEntity<Resource>, url: String): InputStream {
        try {
            return responseEntity.body?.inputStream ?: throw ResourceNotFoundException("Empty response from $url")
        } catch (ex: IOException) {
            throw ResourceNotFoundException("Resource could not be opened", ex)
        }
    }
}
