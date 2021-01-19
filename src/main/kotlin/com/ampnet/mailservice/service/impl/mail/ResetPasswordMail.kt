package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.service.LinkResolverService
import com.ampnet.mailservice.service.TemplateService
import org.springframework.mail.javamail.JavaMailSender

class ResetPasswordMail(
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    linkResolver: LinkResolverService,
    templateService: TemplateService
) : AbstractMail(mailSender, applicationProperties, linkResolver, templateService) {

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
