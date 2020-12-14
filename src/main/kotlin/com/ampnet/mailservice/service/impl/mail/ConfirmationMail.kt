package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import org.springframework.mail.javamail.JavaMailSender

class ConfirmationMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    override val languageData = listOf(
        LanguageData(
            "en", "Confirm your email",
            DefaultMustacheFactory().compile("mustache/mail-confirmation-template.mustache")
        )
    )

    fun setData(token: String, coop: String): ConfirmationMail {
        data = MailConfirmationData(linkResolver.getConfirmationLink(token, coop))
        return this
    }
}

data class MailConfirmationData(val link: String)
