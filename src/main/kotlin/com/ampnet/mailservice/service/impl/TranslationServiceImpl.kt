package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.exception.InternalException
import com.ampnet.mailservice.service.TranslationService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.stereotype.Service
import java.io.StringReader

@Service
class TranslationServiceImpl(
    private val objectMapper: ObjectMapper
) : TranslationService {

    private val translations by lazy {
        val json = javaClass.classLoader.getResource("translations.json")?.readText()
            ?: throw InternalException("Could not find translations.json")
        objectMapper.readValue<Map<String, Map<String, String>>>(json)
    }

    override fun getTemplateTranslations(templateName: String): Map<String, Mustache> {
        val templates = translations[templateName]?.filter { it.value.isNotBlank() }
            ?: throw InternalException("Could not find template: $templateName")
        return templates.mapValues { generateMustache(it.value, templateName) }
    }

    override fun getTitleTranslations(titleKey: String): Map<String, String> {
        return translations[titleKey]?.filter { it.value.isNotBlank() }
            ?: throw InternalException("Could not find titleKey: $titleKey")
    }

    private fun generateMustache(template: String, name: String): Mustache =
        DefaultMustacheFactory().compile(StringReader(template), name)
}
