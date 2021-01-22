package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.config.RestTemplateConfig
import com.ampnet.mailservice.exception.ResourceNotFoundException
import com.ampnet.mailservice.service.impl.FileServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestTemplate

@Import(RestTemplateConfig::class)
@ExtendWith(SpringExtension::class)
class FileServiceTest : TestBase() {

    private val pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
    private val invalidUrl = "https://www.w3.org/invalid.pdf"

    @Autowired
    private lateinit var restTemplate: RestTemplate

    private val fileService by lazy {
        FileServiceImpl(restTemplate)
    }

    @Test
    @Disabled("Not for automated testing")
    fun mustGetInputStream() {
        val inputStream = fileService.getInputStream(pdfUrl)
        assertThat(inputStream).isNotNull()
    }

    @Test
    fun mustThrowExceptionForInvalidUrl() {
        assertThrows<ResourceNotFoundException> { fileService.getInputStream(invalidUrl) }
    }
}
