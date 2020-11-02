package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class FailedDeliveryMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {
    override val title: String = "Email delivery failed"
    override val template: Mustache =
        DefaultMustacheFactory().compile("mustache/failed-delivery-message-template.mustache")

    fun setData(emails: List<String>): FailedDeliveryMail {
        FailedDeliveryRecipients(emails.joinToString { ", " })
        return this
    }
}

data class FailedDeliveryRecipients(val failedRecipients: String)
