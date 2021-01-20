package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.config.JsonConfig
import com.ampnet.mailservice.service.impl.TemplateTranslationServiceImpl
import com.ampnet.mailservice.service.impl.mail.AbstractMail
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(JsonConfig::class)
@ExtendWith(SpringExtension::class)
class TemplateTranslationServiceTest : TestBase() {

    private val translations by lazy {
        val json = javaClass.classLoader.getResource("mustache/translations.json")?.readText()
            ?: fail("Could not find translations.json")
        objectMapper.readValue<Map<String, Map<String, String>>>(json)
    }

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val templateTranslationService by lazy {
        TemplateTranslationServiceImpl(objectMapper)
    }

    @Test
    fun mustGenerateTemplateDataInEnglishOnInvalidLanguage() {
        verify("Translation service fallback to english on invalid language") {
            val depositTemplate = "depositTemplate"
            val depositTitle = "depositInfoTitle"
            val request = AbstractMail.TemplateRequestData("invalid", depositTemplate, depositTitle)
            val templateData = templateTranslationService.getTemplateData(request)
            assertThat(templateData.template).isEqualTo(translations[depositTemplate]?.get("en"))
            assertThat(templateData.title).isEqualTo(translations[depositTitle]?.get("en"))
        }
    }
}
