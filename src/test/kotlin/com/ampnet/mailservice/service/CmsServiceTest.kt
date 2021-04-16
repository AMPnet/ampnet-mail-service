package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.Lang
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.exception.InternalException
import com.ampnet.mailservice.exception.ResourceNotFoundException
import com.ampnet.mailservice.service.impl.CmsServiceImpl
import com.ampnet.mailservice.service.pojo.MailListResponse
import com.ampnet.mailservice.service.pojo.MailResponse
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

@ExtendWith(SpringExtension::class)
@Import(ApplicationProperties::class, ObjectMapper::class)
class CmsServiceTest : TestBase() {

    private lateinit var mockWebServer: MockWebServer

    @Autowired
    private lateinit var applicationProperties: ApplicationProperties

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var webClient: WebClient

    private val testContext = TestContext()
    private val coop = "ampnet-test"
    private val defaultLanguage = Lang.EN

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private val cmsService: CmsServiceImpl by lazy {
        webClient = WebClient.create()
        applicationProperties.cms.baseUrl = String.format("http://localhost:%s", mockWebServer.port)
        CmsServiceImpl(applicationProperties, webClient)
    }

    @Test
    fun mustReturnCorrectMail() {
        suppose("Cms service returns mail list response") {
            val mail = generateMailResponse(coop, MailType.MAIL_CONFIRMATION_MAIL)
            testContext.mailList = MailListResponse(listOf(mail))
            mockWebServer.enqueue(
                MockResponse().setBody(objectMapper.writeValueAsString(testContext.mailList))
                    .addHeader("Content-Type", "application/json")
            )
        }

        verify("Subscriber consumes the response") {
            val response = cmsService.getMail(coop, MailType.MAIL_CONFIRMATION_MAIL, defaultLanguage)
            StepVerifier.create(response)
                .expectSubscription()
                .expectNext(testContext.mailList)
                .verifyComplete()
        }
        verify("Mock server has received the correct request") {
            val recordedRequest = mockWebServer.takeRequest()
            assertThat(recordedRequest.method).isEqualTo("GET")
            assertThat(recordedRequest.path)
                .isEqualTo("/mail/$coop?type=${MailType.MAIL_CONFIRMATION_MAIL}&lang=$defaultLanguage")
        }
    }

    @Test
    fun mustThrowExceptionForEmptyMailList() {
        suppose("Cms service returns empty list") {
            mockWebServer.enqueue(
                MockResponse().setBody(objectMapper.writeValueAsString(MailListResponse(listOf())))
                    .addHeader("Content-Type", "application/json")
            )
        }

        verify("Resource not found exception is thrown") {
            val response = cmsService.getMail(coop, MailType.MAIL_CONFIRMATION_MAIL, defaultLanguage)
            StepVerifier.create(response).expectSubscription().expectError(ResourceNotFoundException::class.java).verify()
        }
    }

    @Test
    fun mustThrowExceptionForServerError() {
        suppose("Cms service returns internal server error") {
            mockWebServer.enqueue(MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        }

        verify("Internal exception is thrown") {
            val response = cmsService.getMail("coop", MailType.MAIL_CONFIRMATION_MAIL, defaultLanguage)
            StepVerifier.create(response).expectSubscription().expectError(InternalException::class.java).verify()
        }
    }

    private fun generateMailResponse(
        coop: String,
        type: MailType,
        id: Int? = null,
        lang: Lang = defaultLanguage
    ): MailResponse {
        val requiredFields = type.getRequiredFields().map { it.value }
        return MailResponse(
            id, coop, type.name, requiredFields.joinToString(),
            type, requiredFields, lang
        )
    }

    private class TestContext {
        lateinit var mailList: MailListResponse
    }
}
