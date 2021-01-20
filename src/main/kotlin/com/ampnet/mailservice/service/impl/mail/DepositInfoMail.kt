package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateTranslationService
import org.springframework.mail.javamail.JavaMailSender

class DepositInfoMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateTranslationService: TemplateTranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateTranslationService) {

    override val templateName = "depositTemplate"
    override val title = "depositInfoTitle"

    fun setData(minted: Boolean): DepositInfoMail {
        data = DepositInfo(minted)
        return this
    }

    fun setTemplate(language: String): DepositInfoMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}

data class DepositInfo(val minted: Boolean)
