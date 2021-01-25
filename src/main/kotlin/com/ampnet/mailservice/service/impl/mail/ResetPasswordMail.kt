package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TranslationService
import org.springframework.mail.javamail.JavaMailSender

class ResetPasswordMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    translationService: TranslationService
) : AbstractMail(linkResolver, mailSender, applicationProperties, translationService) {

    override val templateName = "forgotPasswordTemplate"
    override val titleKey = "resetPasswordTitle"

    fun setTemplateData(token: String, coop: String) = apply {
        templateData = ResetPasswordData(linkResolver.getResetPasswordLink(token, coop))
    }
}

data class ResetPasswordData(val link: String)
