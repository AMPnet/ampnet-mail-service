package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateService
import org.springframework.mail.javamail.JavaMailSender

class FailedDeliveryMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateService: TemplateService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateService) {

    override val templateName = "failedDeliveryMessageTemplate"
    override val title = "failedDeliveryTitle"

    fun setData(emails: List<String>): FailedDeliveryMail {
        FailedDeliveryRecipients(emails.joinToString { ", " })
        return this
    }

    fun setTemplate(language: String): FailedDeliveryMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}

data class FailedDeliveryRecipients(val failedRecipients: String)
