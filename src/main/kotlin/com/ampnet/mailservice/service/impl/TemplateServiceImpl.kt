package com.ampnet.mailservice.service.impl

import com.ampnet.mailservice.exception.InternalException
import com.ampnet.mailservice.service.TemplateService
import com.ampnet.mailservice.service.impl.mail.AbstractMail
import com.ampnet.mailservice.service.impl.mail.EN_LANGUAGE
import com.ampnet.mailservice.service.pojo.TemplateTranslationResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service

@Service
class TemplateServiceImpl(
    private val objectMapper: ObjectMapper
) : TemplateService {

    private val templates by lazy {
        val json = javaClass.classLoader.getResource("mustache/translations.json")?.readText()
            ?: throw InternalException("Could not find translations.json")
        objectMapper.readValue<Map<String, Map<String, String>>>(json)
    }

    override fun getTemplateData(request: AbstractMail.TemplateRequestData): TemplateTranslationResponse {
        val template = templates[request.name]?.get(request.language) ?: templates[request.name]?.get(EN_LANGUAGE)
            ?: throw InternalException("Could not find default[en] template")
        val title = templates[request.name]?.get(request.language) ?: templates[request.name]?.get(EN_LANGUAGE)
            ?: throw InternalException("Could not find default[en] title for template: $request.name")
        return TemplateTranslationResponse(template, title)
    }
}
