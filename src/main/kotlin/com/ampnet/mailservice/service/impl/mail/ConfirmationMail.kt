package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateTranslationService
import org.springframework.mail.javamail.JavaMailSender

class ConfirmationMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateTranslationService: TemplateTranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateTranslationService) {

    override val templateName = "mailConfirmationTemplate"
    override val title = "confirmationTitle"

    fun setData(token: String, coop: String): ConfirmationMail {
        data = MailConfirmationData(linkResolver.getConfirmationLink(token, coop))
        return this
    }

    fun setTemplate(language: String): ConfirmationMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}

data class MailConfirmationData(val link: String)
