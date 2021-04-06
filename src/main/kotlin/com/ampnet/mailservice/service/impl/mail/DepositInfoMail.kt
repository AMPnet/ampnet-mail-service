package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.service.HeadlessCmsService
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

class DepositInfoMail(
    override val mailType: MailType,
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    headlessCmsService: HeadlessCmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, headlessCmsService) {

    fun setTemplateData(
        coop: String
    ) = apply {
        this.coop = coop
        templateData = when (mailType) {
            MailType.DEPOSIT_INFO_MAIL -> DepositInfo(coop, linkResolver.getProjectOffersLink(coop))
            MailType.DEPOSIT_INFO_NO_PROJECT_TO_INVEST_MAIL -> DepositInfo(coop, null)
            else -> null
        }
    }
}

data class DepositInfo(
    val coop: String,
    val link: String?
)
