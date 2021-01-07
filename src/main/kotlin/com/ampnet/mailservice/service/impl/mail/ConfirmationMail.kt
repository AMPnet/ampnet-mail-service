package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

const val CONFIRMATION_TEMPLATE = "mail-confirmation-template.mustache"

class ConfirmationMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, CONFIRMATION_TEMPLATE, "Confirm your email"),
        generateLanguageData(EL_LANGUAGE, CONFIRMATION_TEMPLATE, "Επιβεβαιώστε το email σας")
    )

    fun setData(token: String, coop: String): ConfirmationMail {
        data = MailConfirmationData(linkResolver.getConfirmationLink(token, coop))
        return this
    }
}

data class MailConfirmationData(val link: String)
