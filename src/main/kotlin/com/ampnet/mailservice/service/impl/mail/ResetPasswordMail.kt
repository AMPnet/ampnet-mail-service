package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import org.springframework.mail.javamail.JavaMailSender

class ResetPasswordMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    translationService: TranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, translationService) {

    override val templateName = "forgotPasswordTemplate"
    override val titleKey = "resetPasswordTitle"

    fun setData(token: String, coop: String) = apply {
        data = ResetPasswordData(linkResolver.getResetPasswordLink(token, coop))
    }
}

data class ResetPasswordData(val link: String)
