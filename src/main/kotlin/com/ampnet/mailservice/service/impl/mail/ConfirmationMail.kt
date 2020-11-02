package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.springframework.mail.javamail.JavaMailSender

class ConfirmationMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {
    override val title: String = "Confirm your email"
    override val template: Mustache = DefaultMustacheFactory().compile("mustache/mail-confirmation-template.mustache")

    fun setData(token: String): ConfirmationMail {
        data = MailConfirmationData(linkResolver.getConfirmationLink(token))
        return this
    }
}

data class MailConfirmationData(val link: String)
