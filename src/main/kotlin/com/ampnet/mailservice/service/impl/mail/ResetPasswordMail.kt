package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.github.mustachejava.DefaultMustacheFactory
import org.springframework.mail.javamail.JavaMailSender

class ResetPasswordMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService
) : AbstractMail(mailSender, applicationProperties, linkResolver) {

    override val languageData = listOf(
        LanguageData(
            EN_LANGUAGE, "Reset password",
            DefaultMustacheFactory().compile("mustache/forgot-password-template.mustache")
        )
    )

    fun setData(token: String, coop: String): ResetPasswordMail {
        data = ResetPasswordData(linkResolver.getResetPasswordLink(token, coop))
        return this
    }
}

data class ResetPasswordData(val link: String)
