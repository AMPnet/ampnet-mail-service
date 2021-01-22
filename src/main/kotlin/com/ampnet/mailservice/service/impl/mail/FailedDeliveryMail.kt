package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import org.springframework.mail.javamail.JavaMailSender

class FailedDeliveryMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "failedDeliveryMessageTemplate"
    override val titleKey = "failedDeliveryTitle"

    fun setTemplateData(emails: List<String>) = apply {
        templateData = FailedDeliveryRecipients(emails.joinToString { ", " })
    }
}

data class FailedDeliveryRecipients(val failedRecipients: String)
