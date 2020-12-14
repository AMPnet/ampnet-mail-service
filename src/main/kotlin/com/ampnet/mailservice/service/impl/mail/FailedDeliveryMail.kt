package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import org.springframework.mail.javamail.JavaMailSender

class FailedDeliveryMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    override val languageData = listOf(
        LanguageData(
            "en", "Email delivery failed",
            DefaultMustacheFactory().compile("mustache/failed-delivery-message-template.mustache")
        )
    )

    fun setData(emails: List<String>): FailedDeliveryMail {
        FailedDeliveryRecipients(emails.joinToString { ", " })
        return this
    }
}

data class FailedDeliveryRecipients(val failedRecipients: String)
