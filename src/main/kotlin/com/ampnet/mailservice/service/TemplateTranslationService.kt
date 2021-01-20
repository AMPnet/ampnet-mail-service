package com.ampnet.mailservice.service

import com.ampnet.mailservice.service.impl.mail.AbstractMail
import com.ampnet.mailservice.service.pojo.TemplateTranslationResponse

interface TemplateTranslationService {
    fun getTemplateData(request: AbstractMail.TemplateRequestData): TemplateTranslationResponse
}
