package com.ampnet.mailservice.service.impl.mail

import com.ampnet.mailservice.config.ApplicationProperties
import com.ampnet.mailservice.enums.MailType
import com.ampnet.mailservice.service.CmsService
import com.ampnet.mailservice.service.LinkResolverService
import org.springframework.mail.javamail.JavaMailSender

class DepositRequestMail(
    linkResolver: LinkResolverService,
    mailSender: JavaMailSender,
    applicationProperties: ApplicationProperties,
    cmsService: CmsService
) : AbstractMail(linkResolver, mailSender, applicationProperties, cmsService) {

    override val mailType = MailType.DEPOSIT_REQUEST_MAIL

    fun setTemplateData(amount: Long) = apply { templateData = AmountData(amount.toMailFormat()) }
}

data class AmountData(val amount: String)
