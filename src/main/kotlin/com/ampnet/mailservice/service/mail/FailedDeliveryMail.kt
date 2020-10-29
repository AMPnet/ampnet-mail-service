package com.ampnet.mailservice.service.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class FailedDeliveryMail(
    val emails: List<String>,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "Email delivery failed"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/failed-delivery-message-template.mustache")
    override val data: FailedDeliveryRecipients
        get() = FailedDeliveryRecipients(emails.joinToString { ", " })
}

data class FailedDeliveryRecipients(val failedRecipients: String)
