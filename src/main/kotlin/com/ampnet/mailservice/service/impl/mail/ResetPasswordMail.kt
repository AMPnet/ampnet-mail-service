package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

class ResetPasswordMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    private val templateName = "forgot-password-template.mustache"

    override val languageData = listOf(
        generateLanguageData(EN_LANGUAGE, templateName, "Reset password"),
        generateLanguageData(EL_LANGUAGE, templateName, "Επαναφορά κωδικού πρόσβασης")
    )

    fun setData(token: String, coop: String): ResetPasswordMail {
        data = ResetPasswordData(linkResolver.getResetPasswordLink(token, coop))
        return this
    }
}

data class ResetPasswordData(val link: String)
