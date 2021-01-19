package com.ampnet.mailservice.service

import com.ampnet.mailservice.service.impl.mail.AbstractMail
import com.ampnet.mailservice.service.pojo.TemplateTranslationResponse

interface TemplateService {
    fun getTemplateData(request: AbstractMail.TemplateRequestData): TemplateTranslationResponse
}
