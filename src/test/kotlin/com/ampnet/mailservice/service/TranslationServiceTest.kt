package com.ampnet.mailservice.service

import com.ampnet.mailservice.TestBase
import com.ampnet.mailservice.config.JsonConfig
import com.ampnet.mailservice.exception.InternalException
import com.ampnet.mailservice.service.impl.TranslationServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@Import(JsonConfig::class)
@ExtendWith(SpringExtension::class)
class TranslationServiceTest : TestBase() {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val translationService by lazy {
        TranslationServiceImpl(objectMapper)
    }

    @Test
    fun mustRemoveTemplateTranslationIfBlank() {
        verify("Get templates method will remove translation with blank value") {
            val depositRequestTemplate = "depositRequestTemplate"
            val templates = translationService.getTemplateTranslations(depositRequestTemplate)
            assertThat(templates.keys).hasSize(2)
        }
    }

    @Test
    fun mustRemoveTitleTranslationIfBlank() {
        verify("Get titles method will remove translation with blank value") {
            val depositInfoTitle = "depositInfoTitle"
            val titles = translationService.getTitleTranslations(depositInfoTitle)
            assertThat(titles.keys).hasSize(2)
        }
    }

    @Test
    fun mustThrowExceptionIfNoTemplateFound() {
        verify("Invalid template name") {
            val invalidTemplate = "InvalidTemplate"
            assertThrows<InternalException> { translationService.getTemplateTranslations(invalidTemplate) }
        }
    }

    @Test
    fun mustThrowExceptionIfNoTitleFound() {
        verify("Invalid title name") {
            val invalidTitle = "InvalidTitle"
            assertThrows<InternalException> { translationService.getTitleTranslations(invalidTitle) }
        }
    }
}
