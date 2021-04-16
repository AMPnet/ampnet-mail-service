package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.exception.ResourceNotFoundException
import com.ampnet.mailservice.service.impl.FileServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

@Disabled("Not for automated testing")
@ExtendWith(SpringExtension::class)
class FileServiceTest : TestBase() {

    private val pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
    private val invalidUrl = "https://www.w3.org/invalid.pdf"

    private val fileService by lazy {
        FileServiceImpl()
    }

    @Test
    fun mustGetInputStream() {
        val inputStream = fileService.getTermsOfService(pdfUrl)
        assertThat(inputStream).isNotNull
    }

    @Test
    fun mustThrowExceptionForInvalidUrl() {
        assertThrows<ResourceNotFoundException> { fileService.getTermsOfService(invalidUrl) }
    }

    @Test
    fun mustReturnCorrectInputStreamFromLocalTermsOfService() {
        val inputStream = fileService.getTermsOfService(pdfUrl)
        val inputStreamLocal = fileService.getTermsOfService(pdfUrl)
        assertThat(inputStream).isEqualTo(inputStreamLocal)
    }
}
