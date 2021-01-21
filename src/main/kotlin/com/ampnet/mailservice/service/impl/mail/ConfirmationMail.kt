package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import org.springframework.mail.javamail.JavaMailSender

class ConfirmationMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "mailConfirmationTemplate"
    override val titleKey = "confirmationTitle"

    fun setData(token: String, coop: String) = apply {
        data = MailConfirmationData(linkResolver.getConfirmationLink(token, coop))
    }
}

data class MailConfirmationData(val link: String)
