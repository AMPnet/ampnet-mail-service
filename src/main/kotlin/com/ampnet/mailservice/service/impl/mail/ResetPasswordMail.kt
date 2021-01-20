package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateTranslationService
import org.springframework.mail.javamail.JavaMailSender

class ResetPasswordMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateTranslationService: TemplateTranslationService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateTranslationService) {

    override val templateName = "forgotPasswordTemplate"
    override val title = "resetPasswordTitle"

    fun setData(token: String, coop: String): ResetPasswordMail {
        data = ResetPasswordData(linkResolver.getResetPasswordLink(token, coop))
        return this
    }

    fun setTemplate(language: String): ResetPasswordMail {
        template = TemplateRequestData(language, templateName, title)
        return this
    }
}

data class ResetPasswordData(val link: String)
