package com.ampnet.mailservice.service.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class ConfirmationMail(
    val token: String,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties
) : AbstractMail(mailSender, applicationProperties) {
    override val title: String
        get() = "Confirm your email"
    override val template: Mustache
        get() = mustacheFactory.compile("mustache/mail-confirmation-template.mustache")
    override val data: Any
        get() = MailConfirmationData(linkResolver.getConfirmationLink(token))
}

data class MailConfirmationData(val link: String)
