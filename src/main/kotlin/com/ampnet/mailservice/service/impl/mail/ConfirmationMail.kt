package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.service.CmsService
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

class ConfirmationMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    cmsService: CmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, cmsService) {

    override val mailType = MailType.MAIL_CONFIRMATION_MAIL

    fun setTemplateData(token: String, coop: String) = apply {
        this.coop = coop
        templateData = MailConfirmationData(linkResolver.getConfirmationLink(token, coop))
    }
}

data class MailConfirmationData(val link: String)
