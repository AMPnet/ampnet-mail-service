package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateTranslationService
import org.springframework.mail.javamail.JavaMailSender

class DepositRequestMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateTranslationService: TemplateTranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateTranslationService) {

    override val templateName = "depositRequestTemplate"
    override val title = "depositInfoTitle"

    fun setData(amount: Long): DepositRequestMail {
        data = AmountData(amount.toMailFormat())
        return this
    }

    fun setTemplate(language: String): DepositRequestMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}

data class AmountData(val amount: String)
