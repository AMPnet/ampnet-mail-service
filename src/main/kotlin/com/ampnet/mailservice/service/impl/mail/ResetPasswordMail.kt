package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.service.HeadlessCmsService
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

class ResetPasswordMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    headlessCmsService: HeadlessCmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, headlessCmsService) {

    override val mailType = MailType.RESET_PASSWORD_MAIL

    fun setTemplateData(token: String, coop: String) = apply {
        this.coop = coop
        templateData = ResetPasswordData(linkResolver.getResetPasswordLink(token, coop))
    }
}

data class ResetPasswordData(val link: String)
